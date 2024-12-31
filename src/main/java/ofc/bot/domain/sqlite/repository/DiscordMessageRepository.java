package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.DiscordMessage;
import ofc.bot.domain.tables.DiscordMessagesTable;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Repository for {@link ofc.bot.domain.entity.DiscordMessage DiscordMessage} entity.
 */
public class DiscordMessageRepository {
    private static final DiscordMessagesTable DISCORD_MESSAGES = DiscordMessagesTable.DISCORD_MESSAGES;
    private final DSLContext ctx;

    public DiscordMessageRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public void upsert(DiscordMessage msg) {
        msg.changed(DISCORD_MESSAGES.CREATED_AT, false);
        ctx.insertInto(DISCORD_MESSAGES)
                .set(msg.intoMap())
                .onDuplicateKeyUpdate()
                .set(msg)
                .execute();
    }

    /**
     * Fetches a list of ids of the users who sent at least 1 message in the given
     * {@code period}.
     * <p>
     * If you make this call:
     * <pre>
     *   {@code
     *     List<Long> userIds = repository.fetchRecentlyActiveUserlist(30, TimeUnit.DAYS);
     *   }
     * </pre>
     * Then will be returned a distinct list of user ids who sent at least one message
     * in the last 30 days.
     *
     * @param period the desired period.
     * @param timeUnit the time unit used in the {@code period} parameter.
     * @return a distinct {@link List} of ids who sent a message in the given period.
     */
    public List<Long> fetchRecentlyActiveUserlist(long period, TimeUnit timeUnit) {
        if (period <= 0)
            throw new IllegalArgumentException("Period must be greater than zero");

        long now = Bot.unixNow();
        long startPoint = now - timeUnit.toSeconds(period);

        return ctx.selectDistinct(DISCORD_MESSAGES.AUTHOR_ID)
                .from(DISCORD_MESSAGES)
                .where(DISCORD_MESSAGES.CREATED_AT.gt(startPoint))
                .orderBy(DISCORD_MESSAGES.CREATED_AT.desc())
                .fetchInto(Long.class);
    }

    public int bulkSave(List<DiscordMessage> msgs) {
        if (msgs.isEmpty()) return 0;
        var batch = ctx.batch(
                ctx.insertInto(DISCORD_MESSAGES)
                        .columns(DISCORD_MESSAGES.ID, DISCORD_MESSAGES.AUTHOR_ID,
                                DISCORD_MESSAGES.CHANNEL_ID, DISCORD_MESSAGES.MESSAGE_REFERENCE_ID,
                                DISCORD_MESSAGES.CONTENT, DISCORD_MESSAGES.STICKER_ID,
                                DISCORD_MESSAGES.DELETED, DISCORD_MESSAGES.DELETION_AUTHOR_ID,
                                DISCORD_MESSAGES.CREATED_AT, DISCORD_MESSAGES.UPDATED_AT)
                        .values((Long) null, null, null, null, null, null, null, null, null, null)
        );

        msgs.forEach(msg -> batch.bind(
                msg.getId(),
                msg.getAuthorId(),
                msg.getChannelId(),
                msg.getMessageReferenceId(),
                msg.getContent(),
                msg.getStickerId(),
                msg.isDeleted() ? 1 : 0,
                msg.getDeletionAuthorId(),
                msg.getTimeCreated(),
                msg.getLastUpdated()
        ));
        return Arrays.stream(batch.execute()).sum();
    }

    public DiscordMessage findById(long msgId, DiscordMessage fallback) {
        DiscordMessage msg = findById(msgId);
        return msg == null ? fallback : msg;
    }

    public DiscordMessage findFirstMessageByChannelId(long chanId) {
        return ctx.selectFrom(DISCORD_MESSAGES)
                .where(DISCORD_MESSAGES.CHANNEL_ID.eq(chanId))
                .orderBy(DISCORD_MESSAGES.CREATED_AT.asc())
                .limit(1)
                .fetchOne();
    }

    public int countByChannelId(long chanId) {
        return ctx.fetchCount(DISCORD_MESSAGES, DISCORD_MESSAGES.CHANNEL_ID.eq(chanId));
    }

    public boolean existsByMessageAndAuthorId(long msgId, long authorId) {
        return ctx.fetchExists(DISCORD_MESSAGES, DISCORD_MESSAGES.ID.eq(msgId)
                .and(DISCORD_MESSAGES.AUTHOR_ID.eq(authorId))
        );
    }

    public DiscordMessage findById(long msgId) {
        return ctx.selectFrom(DISCORD_MESSAGES)
                .where(DISCORD_MESSAGES.ID.eq(msgId))
                .fetchOne();
    }

    /**
     * This is not the same as calling {@link #softDeleteByIdAndAuthor(long, Long)}
     * multiple times in a loop.
     * <p>
     * This method is far more efficient by deleting all records with the given ids
     * in a single query.
     *
     * @param ids the id of the messages to be deleted.
     * @param deletionAuthorId the author of the deletion of these messages.
     */
    public void softDeleteByIdsAndAuthor(@NotNull List<Long> ids, long deletionAuthorId) {
        ctx.update(DISCORD_MESSAGES)
                .set(DISCORD_MESSAGES.DELETED, 1)
                .set(DISCORD_MESSAGES.DELETION_AUTHOR_ID, deletionAuthorId)
                .set(DISCORD_MESSAGES.UPDATED_AT, Bot.unixNow())
                .where(DISCORD_MESSAGES.ID.in(ids))
                .execute();
    }

    public void softDeleteByIdAndAuthor(long msgId, @Nullable Long deletionAuthorId) {
        ctx.update(DISCORD_MESSAGES)
                .set(DISCORD_MESSAGES.DELETED, 1)
                .set(DISCORD_MESSAGES.DELETION_AUTHOR_ID, deletionAuthorId)
                .set(DISCORD_MESSAGES.UPDATED_AT, Bot.unixNow())
                .where(DISCORD_MESSAGES.ID.eq(msgId))
                .execute();
    }
}