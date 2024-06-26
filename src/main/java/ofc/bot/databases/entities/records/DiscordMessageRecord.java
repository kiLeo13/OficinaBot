package ofc.bot.databases.entities.records;

import ofc.bot.databases.RecordEntity;
import ofc.bot.databases.entities.tables.DiscordMessages;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;

public class DiscordMessageRecord extends RecordEntity<Long, DiscordMessageRecord> {

    public static final DiscordMessages DISCORD_MESSAGES = DiscordMessages.DISCORD_MESSAGES;

    public DiscordMessageRecord() {
        super(DISCORD_MESSAGES);
    }

    @NotNull
    @Override
    public Field<Long> getIdField() {
        return DISCORD_MESSAGES.ID;
    }

    public long getAuthorId() {
        Long author = get(DISCORD_MESSAGES.AUTHOR_ID);
        return author == null ? 0 : author;
    }

    public long getChannelId() {
        Long channel = get(DISCORD_MESSAGES.CHANNEL_ID);
        return channel == null ? 0 : channel;
    }

    public String getContent() {
        return get(DISCORD_MESSAGES.CONTENT);
    }

    public long getStickerId() {
        Long id = get(DISCORD_MESSAGES.STICKER_ID);
        return id == null ? 0 : id;
    }

    public Integer getDeleted() {
        return get(DISCORD_MESSAGES.DELETED);
    }

    public long getMessageReferenceId() {
        Long id = get(DISCORD_MESSAGES.MESSAGE_REFERENCE_ID);
        return id == null ? 0 : id;
    }

    public boolean isDeleted() {
        Integer deleted = getDeleted();
        return deleted != null && deleted != 0;
    }

    public long getDeletionAuthorId() {
        Long deletionAuthor = get(DISCORD_MESSAGES.DELETION_AUTHOR_ID);
        return deletionAuthor == null ? 0 : deletionAuthor;
    }

    public long getCreated() {
        Long created = get(DISCORD_MESSAGES.CREATED_AT);
        return created == null ? 0 : created;
    }

    public long getLastUpdated() {
        Long updated = get(DISCORD_MESSAGES.UPDATED_AT);
        return updated == null ? 0 : updated;
    }
}