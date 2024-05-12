package ofc.bot.databases.entities.records;

import ofc.bot.commands.administration.name_history.NameChangeContext;
import ofc.bot.databases.RecordEntity;
import ofc.bot.databases.entities.INameChangeLog;
import ofc.bot.databases.entities.tables.UserGlobalNameUpdates;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;

public class UserGlobalNameUpdateRecord extends RecordEntity<Integer, UserGlobalNameUpdateRecord> implements INameChangeLog {

    public static final UserGlobalNameUpdates USER_GLOBAL_NAME_UPDATES = UserGlobalNameUpdates.USER_GLOBAL_NAME_UPDATES;

    public UserGlobalNameUpdateRecord() {
        super(USER_GLOBAL_NAME_UPDATES);
    }

    @NotNull
    @Override
    public Field<Integer> getIdField() {
        return USER_GLOBAL_NAME_UPDATES.ID;
    }

    @Override
    public long getUserId() {
        return get(USER_GLOBAL_NAME_UPDATES.USER_ID);
    }

    @Override
    public String getOldValue() {
        return get(USER_GLOBAL_NAME_UPDATES.OLD_VALUE);
    }

    @Override
    public String getNewValue() {
        return get(USER_GLOBAL_NAME_UPDATES.NEW_VALUE);
    }

    @Override
    public NameChangeContext getContext() {
        return NameChangeContext.GLOBAL_NAME;
    }

    @Override
    public long getTimestamp() {
        Long created = get(USER_GLOBAL_NAME_UPDATES.CREATED_AT);
        return created == null ? 0 : created;
    }
}