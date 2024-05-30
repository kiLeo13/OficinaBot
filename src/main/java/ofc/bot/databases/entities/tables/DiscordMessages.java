package ofc.bot.databases.entities.tables;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.DiscordMessageRecord;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.time.OffsetDateTime;
import java.util.List;

import static org.jooq.impl.DSL.name;

public class DiscordMessages extends TableImpl<DiscordMessageRecord> {

    public static final long TARGET_GUILD = 582430782577049600L;

    public static final DiscordMessages DISCORD_MESSAGES = new DiscordMessages();

    public final Field<Long> ID                   = createField(name("id"),                   SQLDataType.BIGINT.notNull());
    public final Field<Long> AUTHOR_ID            = createField(name("author_id"),            SQLDataType.BIGINT.notNull());
    public final Field<Long> CHANNEL_ID           = createField(name("channel_id"),           SQLDataType.BIGINT.notNull());
    public final Field<Long> MESSAGE_REFERENCE_ID = createField(name("message_reference_id"), SQLDataType.BIGINT);
    public final Field<String> CONTENT            = createField(name("content"),              SQLDataType.CHAR);
    public final Field<Long> STICKER_ID           = createField(name("sticker_id"),           SQLDataType.BIGINT);
    // Boolean property (0 = false, 1 = true)
    public final Field<Integer> DELETED           = createField(name("deleted"),              SQLDataType.INTEGER);
    public final Field<Long> DELETION_AUTHOR_ID   = createField(name("deletion_author_id"),   SQLDataType.BIGINT);
    public final Field<Long> CREATED_AT           = createField(name("created_at"),           SQLDataType.BIGINT.notNull());
    // This field represents the timestamp of the last update to the row,
    // not the last time the message was fed with a new user input
    public final Field<Long> UPDATED_AT           = createField(name("updated_at"),           SQLDataType.BIGINT.notNull());

    public DiscordMessages() {
        super(name("discord_messages"));
    }

    public static void upsert(Message message) {

        DSLContext ctx = DBManager.getContext();
        List<StickerItem> stickers = message.getStickers();
        String content = message.getContentRaw();
        StickerItem sticker = stickers.isEmpty() ? null : stickers.get(0);
        MessageReference reference = message.getMessageReference();
        Long referenceId = reference == null ? null : reference.getMessageIdLong();
        OffsetDateTime created = message.getTimeCreated();
        OffsetDateTime updated = message.getTimeEdited();
        long id = message.getIdLong();
        long authorId = message.getAuthor().getIdLong();
        long channelId = message.getChannelIdLong();
        long createdAt = created.toEpochSecond();
        long updatedAt = updated == null ? createdAt : updated.toEpochSecond();

        ctx.insertInto(DISCORD_MESSAGES)
                .set(DISCORD_MESSAGES.ID, id)
                .set(DISCORD_MESSAGES.AUTHOR_ID, authorId)
                .set(DISCORD_MESSAGES.CHANNEL_ID, channelId)
                .set(DISCORD_MESSAGES.MESSAGE_REFERENCE_ID, referenceId)
                .set(DISCORD_MESSAGES.CONTENT, content)
                .set(DISCORD_MESSAGES.STICKER_ID, sticker == null ? null : sticker.getIdLong())
                .set(DISCORD_MESSAGES.DELETED, 0)
                .set(DISCORD_MESSAGES.CREATED_AT, createdAt)
                .set(DISCORD_MESSAGES.UPDATED_AT, updatedAt)
                .onDuplicateKeyUpdate()
                .set(DISCORD_MESSAGES.CONTENT, content)
                .set(DISCORD_MESSAGES.UPDATED_AT, updatedAt)
                .executeAsync();
    }

    public static void softDelete(long messageId, Long deletionAuthorId) {

        DSLContext ctx = DBManager.getContext();
        long timestamp = Bot.unixNow();

        ctx.update(DISCORD_MESSAGES)
                .set(DISCORD_MESSAGES.DELETED, 1)
                .set(DISCORD_MESSAGES.DELETION_AUTHOR_ID, deletionAuthorId)
                .set(DISCORD_MESSAGES.UPDATED_AT, timestamp)
                .where(DISCORD_MESSAGES.ID.eq(messageId))
                .executeAsync();
    }

    public static void bulkSoftDelete(List<Long> ids, long deletionAuthorId) {

        DSLContext ctx = DBManager.getContext();
        long timestamp = Bot.unixNow();

        ctx.update(DISCORD_MESSAGES)
                .set(DISCORD_MESSAGES.DELETED, 1)
                .set(DISCORD_MESSAGES.DELETION_AUTHOR_ID, deletionAuthorId)
                .set(DISCORD_MESSAGES.UPDATED_AT, timestamp)
                .where(DISCORD_MESSAGES.ID.in(ids))
                .executeAsync();
    }

    public static String fetchContentById(long messageId) {

        DSLContext ctx = DBManager.getContext();

        return ctx.select(DISCORD_MESSAGES.CONTENT)
                .from(DISCORD_MESSAGES)
                .where(DISCORD_MESSAGES.ID.eq(messageId))
                .fetchOneInto(String.class);
    }

    @NotNull
    @Override
    public Class<DiscordMessageRecord> getRecordType() {
        return DiscordMessageRecord.class;
    }
}