package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.DiscordMessageUpdate;
import ofc.bot.domain.tables.DiscordMessageUpdatesTable;
import org.jooq.DSLContext;

/**
 * Repository for {@link ofc.bot.domain.entity.DiscordMessageUpdate DiscordMessageUpdate} entity.
 */
public class DiscordMessageUpdateRepository {
    private static final DiscordMessageUpdatesTable DISCORD_MESSAGE_UPDATES = DiscordMessageUpdatesTable.DISCORD_MESSAGE_UPDATES;
    private final DSLContext ctx;

    public DiscordMessageUpdateRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public void save(DiscordMessageUpdate upd) {
        ctx.insertInto(DISCORD_MESSAGE_UPDATES)
                .set(upd)
                // Impossible, the id is an autoincrement and the table has no check constrains,
                // I put this here anyway just to be safe.
                .onDuplicateKeyIgnore()
                .execute();
    }
}