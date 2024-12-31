package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.UserExclusion;
import ofc.bot.domain.entity.enums.ExclusionType;
import ofc.bot.domain.tables.UsersExclusionsTable;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Repository for {@link ofc.bot.domain.entity.UserExclusion UserExclusion} entity.
 */
public class UserExclusionRepository {
    private static final UsersExclusionsTable USERS_EXCLUSIONS = UsersExclusionsTable.USERS_EXCLUSIONS;
    private final DSLContext ctx;

    public UserExclusionRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public void save(UserExclusion excl) {
        ctx.insertInto(USERS_EXCLUSIONS)
                .set(excl.intoMap())
                .execute();
    }

    public List<Long> findUserIdsByType(ExclusionType type) {
        return ctx.select(USERS_EXCLUSIONS.USER_ID)
                .from(USERS_EXCLUSIONS)
                .where(USERS_EXCLUSIONS.TYPE.eq(type.toString()))
                .fetchInto(long.class);
    }

    public boolean existsByTypeAndUserId(ExclusionType type, long userId) {
        return ctx.fetchExists(USERS_EXCLUSIONS, USERS_EXCLUSIONS.USER_ID.eq(userId).and(USERS_EXCLUSIONS.TYPE.eq(type.toString())));
    }
}