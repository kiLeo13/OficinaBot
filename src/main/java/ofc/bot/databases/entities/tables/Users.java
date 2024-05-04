package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.UserRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class Users extends TableImpl<UserRecord> {

    public static final Users USERS = new Users();

    public final Field<Long> ID            = createField(name("id"),          SQLDataType.BIGINT.notNull());
    public final Field<String> NAME        = createField(name("name"),        SQLDataType.CHAR.notNull());
    public final Field<String> GLOBAL_NAME = createField(name("global_name"), SQLDataType.CHAR.notNull());
    public final Field<Long> CREATED_AT    = createField(name("created_at"),  SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT    = createField(name("updated_at"),  SQLDataType.BIGINT.notNull());

    public Users() {
        super(name("users"));
    }

    @NotNull
    @Override
    public Class<UserRecord> getRecordType() {
        return UserRecord.class;
    }
}