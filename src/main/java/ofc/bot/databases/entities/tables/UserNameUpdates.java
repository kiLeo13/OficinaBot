package ofc.bot.databases.entities.tables;

import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.UserNameUpdateRecord;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class UserNameUpdates extends TableImpl<UserNameUpdateRecord> {

    public static final UserNameUpdates USER_NAME_UPDATES = new UserNameUpdates();

    public final Field<Integer> ID       = createField(name("id"),         SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Long> USER_ID     = createField(name("user_id"),    SQLDataType.BIGINT.notNull());
    public final Field<String> OLD_VALUE = createField(name("old_value"),  SQLDataType.CHAR);
    public final Field<String> NEW_VALUE = createField(name("new_value"),  SQLDataType.CHAR);
    public final Field<Long> CREATED_AT  = createField(name("created_at"), SQLDataType.BIGINT.notNull());

    public UserNameUpdates() {
        super(name("user_name_updates"));
    }

    public static void insert(long userId, String newValue, String oldValue) {

        DSLContext ctx = DBManager.getContext();
        long timestamp = Bot.unixNow();

        ctx.insertInto(USER_NAME_UPDATES)
                .set(USER_NAME_UPDATES.USER_ID, userId)
                .set(USER_NAME_UPDATES.NEW_VALUE, newValue)
                .set(USER_NAME_UPDATES.OLD_VALUE, oldValue)
                .set(USER_NAME_UPDATES.CREATED_AT, timestamp)
                .executeAsync();
    }

    @NotNull
    @Override
    public Class<UserNameUpdateRecord> getRecordType() {
        return UserNameUpdateRecord.class;
    }
}