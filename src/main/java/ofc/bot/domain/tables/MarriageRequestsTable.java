package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.MarriageRequest;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import static ofc.bot.domain.tables.UsersTable.USERS;
import static org.jooq.impl.DSL.foreignKey;
import static org.jooq.impl.DSL.name;

public class MarriageRequestsTable extends InitializableTable<MarriageRequest> {
    public static final MarriageRequestsTable MARRIAGE_REQUESTS = new MarriageRequestsTable();

    public final Field<Integer> ID        = createField(name("id"),           SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Long> REQUESTER_ID = createField(name("requester_id"), SQLDataType.BIGINT.notNull());
    public final Field<Long> TARGET_ID    = createField(name("target_id"),    SQLDataType.BIGINT.notNull());
    public final Field<Long> CREATED_AT   = createField(name("created_at"),   SQLDataType.BIGINT.notNull());

    public MarriageRequestsTable() {
        super("marriage_requests");
    }

    @NotNull
    @Override
    public Class<MarriageRequest> getRecordType() {
        return MarriageRequest.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields())
                .constraints(
                        foreignKey(REQUESTER_ID).references(USERS, USERS.ID),
                        foreignKey(TARGET_ID).references(USERS, USERS.ID)
                );
    }
}