package ofc.bot.databases.entities.records;

import ofc.bot.databases.Repository;
import ofc.bot.databases.entities.tables.Users;
import ofc.bot.util.Bot;
import org.jooq.Field;

public class UserRecord extends Repository<Long, UserRecord> {

    public static final Users USERS = Users.USERS;

    public UserRecord() {
        super(USERS);
    }

    public UserRecord(long id, String name, String globalName) {
        this();
        long timestamp = Bot.unixNow();

        set(USERS.ID, id);
        set(USERS.NAME, name);
        set(USERS.GLOBAL_NAME, globalName);
        set(USERS.CREATED_AT, timestamp);
        set(USERS.UPDATED_AT, timestamp);
    }

    @Override
    public Field<Long> getIdField() {
        return USERS.ID;
    }

    public String getName() {
        return get(USERS.NAME);
    }

    public String getGlobalName() {
        return get(USERS.GLOBAL_NAME);
    }

    public long getCreated() {
        Long created = get(USERS.CREATED_AT);
        return created == null ? 0 : created;
    }

    public long getLastUpdated() {
        Long updated = get(USERS.UPDATED_AT);
        return updated == null ? 0 : updated;
    }
}