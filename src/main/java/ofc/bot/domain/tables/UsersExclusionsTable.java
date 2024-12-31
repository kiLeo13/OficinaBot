package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.UserExclusion;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import static ofc.bot.domain.tables.UsersTable.USERS;
import static org.jooq.impl.DSL.foreignKey;
import static org.jooq.impl.DSL.name;

public class UsersExclusionsTable extends InitializableTable<UserExclusion> {
    public static final UsersExclusionsTable USERS_EXCLUSIONS = new UsersExclusionsTable();

    public final Field<Integer> ID      = createField(name("id"),             SQLDataType.INTEGER.identity(true));
    public final Field<Long> USER_ID    = createField(name("user_id"),        SQLDataType.BIGINT.notNull());
    public final Field<String> TYPE     = createField(name("exclusion_type"), SQLDataType.CHAR.notNull());
    public final Field<Long> CREATED_AT = createField(name("created_at"),     SQLDataType.BIGINT.notNull());

    public UsersExclusionsTable() {
        super("users_exclusions");
    }

    @NotNull
    @Override
    public Class<UserExclusion> getRecordType() {
        return UserExclusion.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields())
                .unique(USER_ID, TYPE)
                .constraint(
                        foreignKey(USER_ID).references(USERS, USERS.ID)
                );
    }
}