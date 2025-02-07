package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.LevelRole;
import ofc.bot.domain.tables.LevelsRolesTable;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Repository for {@link LevelRole} entity.
 */
public class LevelRoleRepository {
    private static final LevelsRolesTable LEVELS_ROLES = LevelsRolesTable.LEVELS_ROLES;
    private final DSLContext ctx;

    public LevelRoleRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public List<LevelRole> findAll() {
        return ctx.fetch(LEVELS_ROLES);
    }

    public LevelRole findLastByLevel(int level) {
        return ctx.selectFrom(LEVELS_ROLES)
                .where(LEVELS_ROLES.LEVEL.le(level))
                .orderBy(LEVELS_ROLES.LEVEL.desc())
                .limit(1)
                .fetchOne();
    }
}