package ofc.bot.domain.entity;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import ofc.bot.domain.tables.DiscordMessagesTable;
import ofc.bot.util.Bot;
import org.jooq.impl.TableRecordImpl;

import java.util.List;

public class DiscordMessage extends TableRecordImpl<DiscordMessage> {
    private static final DiscordMessagesTable DISCORD_MESSAGES = DiscordMessagesTable.DISCORD_MESSAGES;

    public static final long TARGET_GUILD = 582430782577049600L;

    public DiscordMessage() {
        super(DISCORD_MESSAGES);
    }

    public DiscordMessage(
            long id, long authorId, long channelId,
            String content, Long stickerId, Long messageReferenceId,
            long createdAt, long updatedAt
    ) {
        this();
        set(DISCORD_MESSAGES.ID, id);
        set(DISCORD_MESSAGES.AUTHOR_ID, authorId);
        set(DISCORD_MESSAGES.CHANNEL_ID, channelId);
        set(DISCORD_MESSAGES.CONTENT, content);
        set(DISCORD_MESSAGES.STICKER_ID, stickerId);
        set(DISCORD_MESSAGES.MESSAGE_REFERENCE_ID, messageReferenceId);
        set(DISCORD_MESSAGES.CREATED_AT, createdAt);
        set(DISCORD_MESSAGES.UPDATED_AT, updatedAt);
    }

    public static DiscordMessage fromMessage(Message msg) {
        List<StickerItem> stickers = msg.getStickers();
        Long stickerId = stickers.isEmpty() ? null : stickers.get(0).getIdLong();
        MessageReference ref = msg.getMessageReference();
        Long refId = ref == null ? null : ref.getMessageIdLong();

        return new DiscordMessage(
                msg.getIdLong(),
                msg.getAuthor().getIdLong(),
                msg.getChannelIdLong(),
                msg.getContentRaw(),
                stickerId,
                refId,
                msg.getTimeCreated().toEpochSecond(),
                Bot.unixNow()
        );
    }

    public long getId() {
        return get(DISCORD_MESSAGES.ID);
    }

    public long getAuthorId() {
        return get(DISCORD_MESSAGES.AUTHOR_ID);
    }

    public long getChannelId() {
        return get(DISCORD_MESSAGES.CHANNEL_ID);
    }

    public String getContent() {
        return get(DISCORD_MESSAGES.CONTENT);
    }

    public long getStickerId() {
        Long id = get(DISCORD_MESSAGES.STICKER_ID);
        return id == null ? 0 : id;
    }

    public long getMessageReferenceId() {
        Long id = get(DISCORD_MESSAGES.MESSAGE_REFERENCE_ID);
        return id == null ? 0 : id;
    }

    public boolean isDeleted() {
        Integer deleted = get(DISCORD_MESSAGES.DELETED);
        return deleted != null && deleted != 0;
    }

    public long getDeletionAuthorId() {
        return get(DISCORD_MESSAGES.DELETION_AUTHOR_ID);
    }

    public long getTimeCreated() {
        return get(DISCORD_MESSAGES.CREATED_AT);
    }

    public long getLastUpdated() {
        return get(DISCORD_MESSAGES.UPDATED_AT);
    }

    public String getJumpUrl(long guildId) {
        return String.format(Message.JUMP_URL, guildId, getChannelId(), getId());
    }

    public DiscordMessage setId(long id) {
        set(DISCORD_MESSAGES.ID, id);
        return this;
    }

    public DiscordMessage setAuthorId(long authorId) {
        set(DISCORD_MESSAGES.AUTHOR_ID, authorId);
        return this;
    }

    public DiscordMessage setChannelId(long channelId) {
        set(DISCORD_MESSAGES.CHANNEL_ID, channelId);
        return this;
    }

    public DiscordMessage setContent(String content) {
        set(DISCORD_MESSAGES.CONTENT, content);
        return this;
    }

    public DiscordMessage setStickerId(long stickerId) {
        set(DISCORD_MESSAGES.STICKER_ID, stickerId);
        return this;
    }

    public DiscordMessage setMessageReferenceId(long refId) {
        set(DISCORD_MESSAGES.MESSAGE_REFERENCE_ID, refId);
        return this;
    }

    public DiscordMessage setDeleted(boolean flag) {
        set(DISCORD_MESSAGES.DELETED, flag ? 1 : 0);
        return this;
    }

    public DiscordMessage setDeletionAuthorId(long authorId) {
        set(DISCORD_MESSAGES.DELETION_AUTHOR_ID, authorId);
        return this;
    }

    public DiscordMessage setTimeCreated(long createdAt) {
        set(DISCORD_MESSAGES.CREATED_AT, createdAt);
        return this;
    }

    public DiscordMessage setLastUpdated(long updatedAt) {
        set(DISCORD_MESSAGES.UPDATED_AT, updatedAt);
        return this;
    }

    /**
     * Shortcut for {@link #setLastUpdated(long)}.
     * Here you don't have to manually provide a timestamp value,
     * the field is updated through the {@link Bot#unixNow()} method call.
     *
     * @return The current instance, for chaining convenience.
     */
    public DiscordMessage tickUpdate() {
        return setLastUpdated(Bot.unixNow());
    }
}