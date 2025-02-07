package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.RentStatus;
import ofc.bot.domain.tables.OficinaGroupsTable;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Repository for {@link ofc.bot.domain.entity.OficinaGroup OficinaGroup} entity.
 */
public class OficinaGroupRepository {
    private static final OficinaGroupsTable OFICINA_GROUPS = OficinaGroupsTable.OFICINA_GROUPS;
    private final DSLContext ctx;

    public OficinaGroupRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public OficinaGroup findByRoleId(long roleId) {
        return ctx.selectFrom(OFICINA_GROUPS)
                .where(OFICINA_GROUPS.ROLE_ID.eq(roleId))
                .fetchOne();
    }

    public OficinaGroup findByOwnerId(long userId) {
        return ctx.selectFrom(OFICINA_GROUPS)
                .where(OFICINA_GROUPS.OWNER_ID.eq(userId))
                .fetchOne();
    }

    public OficinaGroup findById(int id) {
        return ctx.selectFrom(OFICINA_GROUPS)
                .where(OFICINA_GROUPS.ID.eq(id))
                .fetchOne();
    }

    public List<OficinaGroup> findByName(String search, int limit) {
        return ctx.selectFrom(OFICINA_GROUPS)
                .where(OFICINA_GROUPS.NAME.like('%' + search + '%'))
                .limit(limit)
                .fetch();
    }

    public boolean existsByOwnerId(long userId) {
        return ctx.fetchExists(OFICINA_GROUPS, OFICINA_GROUPS.OWNER_ID.eq(userId));
    }

    public List<OficinaGroup> findChargeableGroups() {
        return ctx.selectFrom(OFICINA_GROUPS)
                .where(OFICINA_GROUPS.RENT_STATUS.ne(RentStatus.FREE.name()))
                .fetch();
    }

    public List<OficinaGroup> findByRentStatus(RentStatus status) {
        return ctx.selectFrom(OFICINA_GROUPS)
                .where(OFICINA_GROUPS.RENT_STATUS.eq(status.name()))
                .fetch();
    }

    public int updateGroupsStatus(RentStatus newStatus, RentStatus where) {
        long timestamp = Bot.unixNow();
        return ctx.update(OFICINA_GROUPS)
                .set(OFICINA_GROUPS.RENT_STATUS, newStatus.name())
                .set(OFICINA_GROUPS.UPDATED_AT, timestamp)
                .where(OFICINA_GROUPS.RENT_STATUS.eq(where.name()))
                .execute();
    }

    public void upsert(OficinaGroup group) {
        group.changed(OFICINA_GROUPS.CREATED_AT, false);
        ctx.insertInto(OFICINA_GROUPS)
                .set(group.intoMap())
                .onDuplicateKeyUpdate()
                .set(group)
                .execute();
    }

    public void delete(OficinaGroup group) {
        ctx.executeDelete(group, OFICINA_GROUPS.ID.eq(group.getId()));
    }
}