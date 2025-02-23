package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.CommandHistory;
import ofc.bot.domain.tables.CommandsHistoryTable;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

/**
 * Repository for {@link CommandHistory} entity.
 */
public class CommandHistoryRepository extends Repository<CommandHistory> {
    private static final CommandsHistoryTable COMMANDS_HISTORY = CommandsHistoryTable.COMMANDS_HISTORY;

    public CommandHistoryRepository(DSLContext ctx) {
        super(ctx);
    }

    @NotNull
    @Override
    public InitializableTable<CommandHistory> getTable() {
        return COMMANDS_HISTORY;
    }
}