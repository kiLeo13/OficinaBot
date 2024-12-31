package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.tables.OficinaGroupsTable;
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

    /**
     * Fetches only non-privileged or only privileged groups.
     * <p>
     * This method will never return all groups, you must choose to either
     * fetch all non-privileged or all privileged groups.
     * <p>
     * Again, providing {@code false} in the {@code privileged} parameter
     * is <b><u>NOT</u></u></b> the same as calling {@link #findAll()}.
     *
     * @param privileged {@code true} to return only privileged groups,
     *        {@code false} to return only non-privileged groups.
     * @return a {@link List} of groups matching the provided parameter.
     */
    public List<OficinaGroup> findGroups(boolean privileged) {
        return ctx.selectFrom(OFICINA_GROUPS)
                .where(OFICINA_GROUPS.HAS_FREE_ACCESS.eq(privileged ? 1 : 0))
                .fetch();
    }

    public List<OficinaGroup> findAll() {
        return ctx.fetch(OFICINA_GROUPS);
    }

    public OficinaGroup upsert(OficinaGroup group) {
        group.changed(OFICINA_GROUPS.CREATED_AT, false);
        ctx.insertInto(OFICINA_GROUPS)
                .set(group.intoMap())
                .onDuplicateKeyUpdate()
                .set(group)
                .execute();

        return findByOwnerId(group.getOwnerId());
    }

    public void delete(OficinaGroup group) {
        ctx.executeDelete(group, OFICINA_GROUPS.ID.eq(group.getId()));
    }
}