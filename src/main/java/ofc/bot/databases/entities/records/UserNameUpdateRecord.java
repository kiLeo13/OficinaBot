package ofc.bot.databases.entities.records;

import ofc.bot.commands.administration.name_history.NameChangeContext;
import ofc.bot.databases.RecordEntity;
import ofc.bot.databases.entities.INameChangeLog;
import ofc.bot.databases.entities.tables.UserNameUpdates;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;

public class UserNameUpdateRecord extends RecordEntity<Integer, UserNameUpdateRecord> implements INameChangeLog {

    public static final UserNameUpdates USER_NAME_UPDATES = UserNameUpdates.USER_NAME_UPDATES;

    public UserNameUpdateRecord() {
        super(USER_NAME_UPDATES);
    }

    @NotNull
    @Override
    public Field<Integer> getIdField() {
        return USER_NAME_UPDATES.ID;
    }

    @Override
    public long getUserId() {
        Long id = get(USER_NAME_UPDATES.USER_ID);
        return id == null ? 0 : id;
    }

    @Override
    public String getOldValue() {
        return get(USER_NAME_UPDATES.OLD_VALUE);
    }

    @Override
    public String getNewValue() {
        return get(USER_NAME_UPDATES.NEW_VALUE);
    }

    @Override
    public NameChangeContext getContext() {
        return NameChangeContext.NAME;
    }

    @Override
    public long getTimestamp() {
        Long created = get(USER_NAME_UPDATES.CREATED_AT);
        return created == null ? 0 : created;
    }
}