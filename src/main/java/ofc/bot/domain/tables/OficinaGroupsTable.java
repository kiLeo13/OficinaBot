package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.OficinaGroup;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import static ofc.bot.domain.tables.UsersTable.USERS;
import static org.jooq.impl.DSL.foreignKey;

public class OficinaGroupsTable extends InitializableTable<OficinaGroup> {
    public static final OficinaGroupsTable OFICINA_GROUPS = new OficinaGroupsTable();

    public final Field<Integer> ID              = newField("id",               SQLDataType.INTEGER.identity(true));
    public final Field<Long> OWNER_ID           = newField("owner_id",         SQLDataType.BIGINT.notNull());
    public final Field<Long> GUILD_ID           = newField("guild_id",         SQLDataType.BIGINT.notNull());
    public final Field<Long> ROLE_ID            = newField("role_id",          SQLDataType.BIGINT.notNull());
    public final Field<Long> TEXT_CHANNEL_ID    = newField("text_channel_id",  SQLDataType.BIGINT);
    public final Field<Long> VOICE_CHANNEL_ID   = newField("voice_channel_id", SQLDataType.BIGINT);
    public final Field<String> NAME             = newField("name",             SQLDataType.CHAR.notNull());
    public final Field<String> EMOJI            = newField("emoji",            SQLDataType.CHAR.notNull());
    public final Field<String> CURRENCY         = newField("currency",         SQLDataType.CHAR.notNull());
    public final Field<Integer> AMOUNT_PAID     = newField("amount_paid",      SQLDataType.INTEGER.notNull());
    public final Field<Double> REFUND_PERCENT   = newField("refund_percent",   SQLDataType.DOUBLE.notNull());
    public final Field<Integer> HAS_FREE_ACCESS = newField("has_free_access",  SQLDataType.INTEGER.notNull());
    public final Field<String> RENT_STATUS      = newField("rent_status",      SQLDataType.CHAR.notNull());
    public final Field<Long> CREATED_AT         = newField("created_at",       SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT         = newField("updated_at",       SQLDataType.BIGINT.notNull());

    public OficinaGroupsTable() {
        super("groups");
    }

    @NotNull
    @Override
    public Class<OficinaGroup> getRecordType() {
        return OficinaGroup.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields())
                .constraint(
                        foreignKey(OWNER_ID).references(USERS, USERS.ID)
                );
    }
}
