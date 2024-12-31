package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.AppUser;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import static org.jooq.impl.DSL.name;

public class UsersTable extends InitializableTable<AppUser> {
    public static final UsersTable USERS = new UsersTable();

    public final Field<Long> ID            = createField(name("id"),          SQLDataType.BIGINT.notNull());
    public final Field<String> NAME        = createField(name("name"),        SQLDataType.CHAR.notNull());
    public final Field<String> GLOBAL_NAME = createField(name("global_name"), SQLDataType.CHAR);
    public final Field<Long> CREATED_AT    = createField(name("created_at"),  SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT    = createField(name("updated_at"),  SQLDataType.BIGINT.notNull());

    public UsersTable() {
        super("users");
    }

    @NotNull
    @Override
    public Class<AppUser> getRecordType() {
        return AppUser.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields());
    }
}