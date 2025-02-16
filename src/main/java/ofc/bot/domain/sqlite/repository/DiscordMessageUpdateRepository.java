package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.DiscordMessageUpdate;
import ofc.bot.domain.tables.DiscordMessageUpdatesTable;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

/**
 * Repository for {@link DiscordMessageUpdate} entity.
 */
public class DiscordMessageUpdateRepository extends Repository<DiscordMessageUpdate> {
    private static final DiscordMessageUpdatesTable DISCORD_MESSAGE_UPDATES = DiscordMessageUpdatesTable.DISCORD_MESSAGE_UPDATES;

    public DiscordMessageUpdateRepository(DSLContext ctx) {
        super(ctx);
    }

    @NotNull
    @Override
    public InitializableTable<DiscordMessageUpdate> getTable() {
        return DISCORD_MESSAGE_UPDATES;
    }
}