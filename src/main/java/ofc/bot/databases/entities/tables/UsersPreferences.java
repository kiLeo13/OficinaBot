package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.UserPreferencesRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class UsersPreferences extends TableImpl<UserPreferencesRecord> {

    public static final UsersPreferences USERS_PREFERENCES = new UsersPreferences();

    public final Field<Long> USER_ID    = createField(name("user_id"),    SQLDataType.BIGINT.notNull());
    public final Field<String> LOCALE   = createField(name("locale"),     SQLDataType.CHAR.notNull());
    public final Field<Long> CREATED_AT = createField(name("created_at"), SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT = createField(name("updated_at"), SQLDataType.BIGINT.notNull());

    public UsersPreferences() {
        super(name("users_preferences"));
    }

    @NotNull
    @Override
    public Class<UserPreferencesRecord> getRecordType() {
        return UserPreferencesRecord.class;
    }
}