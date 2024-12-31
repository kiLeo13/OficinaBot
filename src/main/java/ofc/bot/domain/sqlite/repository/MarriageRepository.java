package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.AppUser;
import ofc.bot.domain.entity.Marriage;
import ofc.bot.domain.sqlite.MapperFactory;
import ofc.bot.domain.tables.MarriagesTable;
import ofc.bot.domain.tables.UsersTable;
import ofc.bot.domain.viewmodels.MarriageView;
import org.jooq.*;

import java.util.List;

/**
 * Repository for {@link ofc.bot.domain.entity.Marriage Marriage} entity.
 */
public class MarriageRepository {
    private static final MarriagesTable MARRIAGES = MarriagesTable.MARRIAGES;
    private static final UsersTable USERS = UsersTable.USERS;
    private final DSLContext ctx;

    public MarriageRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Marriage findByUserIds(long spouse1, long spouse2) {
        return ctx.selectFrom(MARRIAGES)
                .where(MARRIAGES.REQUESTER_ID.eq(spouse1).and(MARRIAGES.TARGET_ID.eq(spouse2)))
                .or(MARRIAGES.TARGET_ID.eq(spouse1).and(MARRIAGES.REQUESTER_ID.eq(spouse2)))
                .fetchOne();
    }

    public int countByUserId(long userId) {
        return ctx.fetchCount(MARRIAGES, MARRIAGES.REQUESTER_ID.eq(userId).or(MARRIAGES.TARGET_ID.eq(userId)));
    }

    public boolean existsByUserIds(long spouse1, long spouse2) {
        return ctx.fetchExists(MARRIAGES,
                MARRIAGES.REQUESTER_ID.eq(spouse1)
                        .and(MARRIAGES.TARGET_ID.eq(spouse2))
                        .or(MARRIAGES.REQUESTER_ID.eq(spouse2)
                                .and(MARRIAGES.TARGET_ID.eq(spouse1)))
        );
    }

    public List<MarriageView> viewByUserId(long userId) {
        return viewByUserId(userId, -1);
    }

    public int countWithExclusions(long userId, List<Long> exclIds) {
        return ctx.fetchCount(
                MARRIAGES,
                MARRIAGES.REQUESTER_ID.notIn(exclIds)
                        .and(MARRIAGES.TARGET_ID.notIn(exclIds))
                        .and(MARRIAGES.TARGET_ID.eq(userId).or(MARRIAGES.REQUESTER_ID.eq(userId)))
        );
    }

    @SuppressWarnings("DataFlowIssue")
    public List<MarriageView> viewByUserId(long userId, int limit) {
        Table<AppUser> req = USERS.as("req");
        Table<AppUser> tar = USERS.as("tar");
        Result<?> rels = ctx.select(
                        req.field(USERS.ID).as("req_id"),
                        req.field(USERS.NAME).as("req_name"),
                        req.field(USERS.GLOBAL_NAME).as("req_global_name"),
                        req.field(USERS.CREATED_AT).as("req_created_at"),
                        req.field(USERS.UPDATED_AT).as("req_updated_at"),
                        tar.field(USERS.ID).as("tar_id"),
                        tar.field(USERS.NAME).as("tar_name"),
                        tar.field(USERS.GLOBAL_NAME).as("tar_global_name"),
                        tar.field(USERS.CREATED_AT).as("tar_created_at"),
                        tar.field(USERS.UPDATED_AT).as("tar_updated_at"),
                        MARRIAGES.ID,
                        MARRIAGES.MARRIED_AT,
                        MARRIAGES.CREATED_AT,
                        MARRIAGES.UPDATED_AT
                ).from(MARRIAGES)
                .join(req).on(MARRIAGES.REQUESTER_ID.eq(req.field(USERS.ID)))
                .join(tar).on(MARRIAGES.TARGET_ID.eq(tar.field(USERS.ID)))
                .where(MARRIAGES.REQUESTER_ID.eq(userId).or(MARRIAGES.TARGET_ID.eq(userId)))
                .orderBy(MARRIAGES.MARRIED_AT.asc())
                .limit(limit)
                .fetch();
        return rels.map(MapperFactory::mapMarriage);
    }

    /**
     * This method attempts to insert a new marriage record to the database,
     * if it collides (for some unknown reason), nothing happens.
     *
     * @param mr The marriage record to persist.
     */
    public void save(Marriage mr) {
        ctx.insertInto(MARRIAGES)
                .set(mr.intoMap())
                .onDuplicateKeyIgnore()
                .execute();
    }

    public void delete(Marriage rel) {
        ctx.executeDelete(rel, MARRIAGES.ID.eq(rel.getId()));
    }
}