package ofc.bot.domain.entity;

import ofc.bot.domain.tables.DiscordMessageUpdatesTable;

public class DiscordMessageUpdate extends OficinaRecord<DiscordMessageUpdate> {
    private static final DiscordMessageUpdatesTable DISCORD_MESSAGE_UPDATES = DiscordMessageUpdatesTable.DISCORD_MESSAGE_UPDATES;

    public DiscordMessageUpdate() {
        super(DISCORD_MESSAGE_UPDATES);
    }

    public DiscordMessageUpdate(long messageId, String oldContent, String newContent, long createdAt) {
        this();
        set(DISCORD_MESSAGE_UPDATES.MESSAGE_ID, messageId);
        set(DISCORD_MESSAGE_UPDATES.OLD_CONTENT, oldContent);
        set(DISCORD_MESSAGE_UPDATES.NEW_CONTENT, newContent);
        set(DISCORD_MESSAGE_UPDATES.CREATED_AT, createdAt);
    }

    public long getMessageId() {
        return get(DISCORD_MESSAGE_UPDATES.MESSAGE_ID);
    }

    public String getOldContent() {
        return get(DISCORD_MESSAGE_UPDATES.OLD_CONTENT);
    }

    public String getNewContent() {
        return get(DISCORD_MESSAGE_UPDATES.NEW_CONTENT);
    }

    public long getTimeCreated() {
         return get(DISCORD_MESSAGE_UPDATES.CREATED_AT);
    }

    public DiscordMessageUpdate setMessageId(long msgId) {
        set(DISCORD_MESSAGE_UPDATES.MESSAGE_ID, msgId);
        return this;
    }

    public DiscordMessageUpdate setOldContent(String old) {
        set(DISCORD_MESSAGE_UPDATES.OLD_CONTENT, old);
        return this;
    }

    public DiscordMessageUpdate setNewContent(String now) {
        set(DISCORD_MESSAGE_UPDATES.NEW_CONTENT, now);
        return this;
    }
}