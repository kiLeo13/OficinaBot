package ofc.bot.domain.entity;

import ofc.bot.domain.tables.MembersEmojisTable;
import ofc.bot.util.Bot;
import org.jooq.impl.TableRecordImpl;

public class MemberEmoji extends TableRecordImpl<MemberEmoji> {
    private static final MembersEmojisTable MEMBERS_EMOJIS = MembersEmojisTable.MEMBERS_EMOJIS;

    public MemberEmoji() {
        super(MEMBERS_EMOJIS);
    }

    public long getUserId() {
        return get(MEMBERS_EMOJIS.USER_ID);
    }

    public String getEmoji() {
        return get(MEMBERS_EMOJIS.EMOJI);
    }

    public long getTimeCreated() {
        return get(MEMBERS_EMOJIS.CREATED_AT);
    }

    public long getLastUpdated() {
        return get(MEMBERS_EMOJIS.UPDATED_AT);
    }

    public MemberEmoji setUserId(long userId) {
        set(MEMBERS_EMOJIS.USER_ID, userId);
        return this;
    }

    public MemberEmoji setEmoji(String emoji) {
        set(MEMBERS_EMOJIS.EMOJI, emoji);
        return this;
    }

    public MemberEmoji setTimeCreated(long createdAt) {
        set(MEMBERS_EMOJIS.CREATED_AT, createdAt);
        return this;
    }

    public MemberEmoji setLastUpdated(long updatedAt) {
        set(MEMBERS_EMOJIS.UPDATED_AT, updatedAt);
        return this;
    }

    /**
     * Shortcut for {@link #setLastUpdated(long)}.
     * Here you don't have to manually provide a timestamp value,
     * the field is updated through the {@link Bot#unixNow()} method call.
     *
     * @return The current instance, for chaining convenience.
     */
    public MemberEmoji tickUpdate() {
        return setLastUpdated(Bot.unixNow());
    }
}