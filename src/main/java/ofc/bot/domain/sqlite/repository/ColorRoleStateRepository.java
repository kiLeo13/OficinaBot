package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.ColorRoleState;
import ofc.bot.domain.tables.ColorRolesStateTable;
import org.jooq.DSLContext;

/**
 * Repository for {@link ofc.bot.domain.entity.ColorRoleState ColorRoleState} entity.
 */
public class ColorRoleStateRepository {
    private static final ColorRolesStateTable COLOR_ROLES_STATE = ColorRolesStateTable.COLOR_ROLES_STATES;
    private final DSLContext ctx;

    public ColorRoleStateRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public ColorRoleState findByUserAndRoleId(long userId, long roleId, ColorRoleState fallback) {
        ColorRoleState base = findByUserAndRoleId(userId, roleId);
        return base == null ? fallback : base;
    }

    public ColorRoleState findByUserAndRoleId(long userId, long roleId) {
        return ctx.selectFrom(COLOR_ROLES_STATE)
                .where(COLOR_ROLES_STATE.USER_ID.eq(userId))
                .and(COLOR_ROLES_STATE.ROLE_ID.eq(roleId))
                .fetchOne();
    }

    public void upsert(ColorRoleState state) {
        state.changed(COLOR_ROLES_STATE.CREATED_AT, false);
        ctx.insertInto(COLOR_ROLES_STATE)
                .set(state.intoMap())
                .onDuplicateKeyUpdate()
                .set(state)
                .execute();
    }
}