package ofc.bot.databases.entities.records;

import ofc.bot.commands.administration.name_history.NameChangeContext;
import ofc.bot.databases.Repository;
import ofc.bot.databases.entities.INameChangeLog;
import ofc.bot.databases.entities.tables.Nicknames;
import org.jooq.Field;

public class NicknameUpdateRecord extends Repository<Integer, NicknameUpdateRecord> implements INameChangeLog {

    public static final Nicknames NICKNAMES = Nicknames.NICKNAMES;

    public NicknameUpdateRecord() {
        super(NICKNAMES);
    }

    // In this case, we are asking for a "timestamp" to respect Discord's event real timestamp,
    // in order to keep timestamps synced
    public NicknameUpdateRecord(String newValue, String oldValue, long userId, long moderatorId, long timestamp) {
        this();
        set(NICKNAMES.NEW_NICK, newValue);
        set(NICKNAMES.OLD_NICK, oldValue);
        set(NICKNAMES.USER, userId);
        set(NICKNAMES.MODERATOR, moderatorId);
        set(NICKNAMES.CREATED_AT, timestamp);
    }

    @Override
    public Field<Integer> getIdField() {
        return NICKNAMES.ID;
    }

    @Override
    public long getUserId() {
        Long user = get(NICKNAMES.USER);
        return user == null ? 0 : user;
    }

    @Override
    public long getModeratorId() {
        Long moderator = get(NICKNAMES.MODERATOR);
        return moderator == null ? 0 : moderator;
    }

    @Override
    public String getOldValue() {
        return get(NICKNAMES.OLD_NICK);
    }

    @Override
    public String getNewValue() {
        return get(NICKNAMES.NEW_NICK);
    }

    @Override
    public NameChangeContext getContext() {
        return NameChangeContext.NICK;
    }

    @Override
    public long getTimestamp() {
        Long created = get(NICKNAMES.CREATED_AT);
        return created == null ? 0 : created;
    }
}