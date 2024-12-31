package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.FormerMemberRole;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import static org.jooq.impl.DSL.name;

public class FormerMembersRolesTable extends InitializableTable<FormerMemberRole> {
    public static final FormerMembersRolesTable FORMER_MEMBERS_ROLES = new FormerMembersRolesTable();

    public final Field<Integer> ID         = createField(name("id"),         SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Integer> PRIVILEGED = createField(name("privileged"), SQLDataType.INTEGER.notNull());
    public final Field<Long> USER_ID       = createField(name("user_id"),    SQLDataType.BIGINT.notNull());
    public final Field<Long> GUILD_ID      = createField(name("guild_id"),   SQLDataType.BIGINT.notNull());
    public final Field<Long> ROLE_ID       = createField(name("role_id"),    SQLDataType.BIGINT.notNull());
    public final Field<Long> CREATED_AT    = createField(name("created_at"), SQLDataType.BIGINT.notNull());

    public FormerMembersRolesTable() {
        super("former_members_roles");
    }

    @NotNull
    @Override
    public Class<FormerMemberRole> getRecordType() {
        return FormerMemberRole.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields())
                .unique(USER_ID, ROLE_ID);
    }
}