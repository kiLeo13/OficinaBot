package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.ColorRoleState;
import ofc.bot.domain.entity.CommandHistory;
import ofc.bot.domain.tables.ColorRolesStateTable;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

/**
 * Repository for {@link CommandHistory} entity.
 */
public class CommandHistoryRepository extends Repository<ColorRoleState> {
    private static final ColorRolesStateTable COLOR_ROLES_STATE = ColorRolesStateTable.COLOR_ROLES_STATES;

    public CommandHistoryRepository(DSLContext ctx) {
        super(ctx);
    }

    @NotNull
    @Override
    public InitializableTable<ColorRoleState> getTable() {
        return COLOR_ROLES_STATE;
    }
}