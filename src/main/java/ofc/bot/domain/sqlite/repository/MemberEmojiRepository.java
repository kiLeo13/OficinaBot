package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.MemberEmoji;
import ofc.bot.domain.tables.MembersEmojisTable;
import org.jooq.DSLContext;

/**
 * Repository for {@link ofc.bot.domain.entity.MemberEmoji MemberEmoji} entity.
 */
public class MemberEmojiRepository {
    private static final MembersEmojisTable MEMBERS_EMOJIS = MembersEmojisTable.MEMBERS_EMOJIS;
    private final DSLContext ctx;

    public MemberEmojiRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public MemberEmoji findByUserId(long userId) {
        return ctx.selectFrom(MEMBERS_EMOJIS)
                .where(MEMBERS_EMOJIS.USER_ID.eq(userId))
                .fetchOne();
    }
}