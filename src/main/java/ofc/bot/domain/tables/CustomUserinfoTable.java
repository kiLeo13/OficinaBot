package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.CustomUserinfo;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import static ofc.bot.domain.tables.UsersTable.USERS;
import static org.jooq.impl.DSL.foreignKey;
import static org.jooq.impl.DSL.name;

public class CustomUserinfoTable extends InitializableTable<CustomUserinfo> {
    public static final CustomUserinfoTable CUSTOM_USERINFO = new CustomUserinfoTable();

    public final Field<Long> USER_ID       = createField(name("user_id"),     SQLDataType.BIGINT.notNull());
    public final Field<Integer> COLOR      = createField(name("color"),       SQLDataType.INTEGER);
    public final Field<String> DESCRIPTION = createField(name("description"), SQLDataType.CHAR);
    public final Field<String> FOOTER      = createField(name("footer"),      SQLDataType.CHAR);
    public final Field<Long> CREATED_AT    = createField(name("created_at"),  SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT    = createField(name("updated_at"),  SQLDataType.BIGINT.notNull());

    public CustomUserinfoTable() {
        super("custom_userinfo");
    }

    @NotNull
    @Override
    public Class<CustomUserinfo> getRecordType() {
        return CustomUserinfo.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(USER_ID)
                .columns(fields())
                .constraint(
                        foreignKey(USER_ID).references(USERS, USERS.ID)
                );
    }
}