package ofc.bot.databases.entities.records;

import ofc.bot.databases.Repository;
import ofc.bot.databases.entities.tables.UsersExclusions;
import ofc.bot.util.Bot;
import ofc.bot.util.exclusions.ExclusionType;
import org.jooq.Field;

public class UserExclusionRecord extends Repository<Integer, UserExclusionRecord> {

    public static final UsersExclusions USERS_EXCLUSIONS = UsersExclusions.USERS_EXCLUSIONS;

    public UserExclusionRecord() {
        super(USERS_EXCLUSIONS);
    }

    public UserExclusionRecord(String type, long userId) {
        this();
        long timestamp = Bot.unixNow();

        set(USERS_EXCLUSIONS.USER_ID, userId);
        set(USERS_EXCLUSIONS.TYPE, type);
        set(USERS_EXCLUSIONS.CREATED_AT, timestamp);
    }

    @Override
    public Field<Integer> getIdField() {
        return USERS_EXCLUSIONS.ID;
    }

    public long getUserId() {
        return get(USERS_EXCLUSIONS.USER_ID);
    }

    public ExclusionType getType() {
        String type = get(USERS_EXCLUSIONS.TYPE);
        return ExclusionType.byName(type);
    }

    public long getTimeCreated() {
        return get(USERS_EXCLUSIONS.CREATED_AT);
    }
}