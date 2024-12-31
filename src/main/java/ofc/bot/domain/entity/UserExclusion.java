package ofc.bot.domain.entity;

import ofc.bot.domain.tables.UsersExclusionsTable;
import ofc.bot.domain.entity.enums.ExclusionType;
import ofc.bot.util.Bot;
import org.jooq.impl.TableRecordImpl;

public class UserExclusion extends TableRecordImpl<UserExclusion> {
    private static final UsersExclusionsTable USERS_EXCLUSIONS = UsersExclusionsTable.USERS_EXCLUSIONS;

    public UserExclusion() {
        super(USERS_EXCLUSIONS);
    }

    public UserExclusion(ExclusionType type, long userId, long createdAt) {
        this();
        set(USERS_EXCLUSIONS.USER_ID, userId);
        set(USERS_EXCLUSIONS.TYPE, type.toString());
        set(USERS_EXCLUSIONS.CREATED_AT, createdAt);
    }

    public UserExclusion(ExclusionType type, long userId) {
        this(type, userId, Bot.unixNow());
    }

    public long getUserId() {
        return get(USERS_EXCLUSIONS.USER_ID);
    }

    public ExclusionType getType() {
        String type = get(USERS_EXCLUSIONS.TYPE);
        return ExclusionType.findByName(type);
    }

    public long getTimeCreated() {
        return get(USERS_EXCLUSIONS.CREATED_AT);
    }

    public UserExclusion setUserId(long userId) {
        set(USERS_EXCLUSIONS.USER_ID, userId);
        return this;
    }

    public UserExclusion setType(ExclusionType type) {
        set(USERS_EXCLUSIONS.TYPE, type.toString());
        return this;
    }

    public UserExclusion setTimeCreated(long createdAt) {
        set(USERS_EXCLUSIONS.CREATED_AT, createdAt);
        return this;
    }
}