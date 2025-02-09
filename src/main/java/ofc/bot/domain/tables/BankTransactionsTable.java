package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.BankTransaction;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;

import static ofc.bot.domain.tables.UsersTable.USERS;

public class BankTransactionsTable extends InitializableTable<BankTransaction> {
    public static final BankTransactionsTable BANK_TRANSACTIONS = new BankTransactionsTable();

    public final Field<Integer> ID       = newField("id",          INT.identity(true));
    public final Field<Long> USER_ID     = newField("user_id",     BIGINT.notNull());
    public final Field<Long> RECEIVER_ID = newField("receiver_id", BIGINT);
    public final Field<String> CURRENCY  = newField("currency",    CHAR.notNull());
    public final Field<Long> AMOUNT      = newField("amount",      BIGINT.notNull());
    public final Field<String> ACTION    = newField("action",      CHAR.notNull());
    public final Field<String> COMMENT   = newField("comment",     CHAR);
    public final Field<String> PRODUCT   = newField("product",     CHAR);
    public final Field<Long> CREATED_AT  = newField("created_at",  BIGINT.notNull());

    public BankTransactionsTable() {
        super("bank_transactions");
    }

    @NotNull
    @Override
    public Class<BankTransaction> getRecordType() {
        return BankTransaction.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields())
                .constraints(
                        foreignKey(USER_ID).references(USERS, USERS.ID),
                        foreignKey(RECEIVER_ID).references(USERS, USERS.ID)
                );
    }
}
