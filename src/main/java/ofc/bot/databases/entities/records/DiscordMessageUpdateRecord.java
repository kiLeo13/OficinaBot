package ofc.bot.databases.entities.records;

import ofc.bot.databases.Repository;
import ofc.bot.databases.entities.tables.DiscordMessageUpdates;
import org.jooq.Field;

public class DiscordMessageUpdateRecord extends Repository<Integer, DiscordMessageUpdateRecord> {

    public static final DiscordMessageUpdates DISCORD_MESSAGE_UPDATES = DiscordMessageUpdates.DISCORD_MESSAGE_UPDATES;

    public DiscordMessageUpdateRecord() {
        super(DISCORD_MESSAGE_UPDATES);
    }

    public Field<Integer> getIdField() {
        return DISCORD_MESSAGE_UPDATES.ID;
    }

    public long getMessageId() {
        Long id = get(DISCORD_MESSAGE_UPDATES.MESSAGE_ID);
        return id == null ? 0 : id;
    }

    public String getOldContent() {
        return get(DISCORD_MESSAGE_UPDATES.OLD_CONTENT);
    }

    public String getNewContent() {
        return get(DISCORD_MESSAGE_UPDATES.NEW_CONTENT);
    }

    public long getCreated() {
         Long created = get(DISCORD_MESSAGE_UPDATES.CREATED_AT);
         return created == null ? 0 : created;
    }
}