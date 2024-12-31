package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.tables.BankTransactionsTable;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

import java.util.List;

import static org.jooq.impl.DSL.noCondition;

/**
 * Repository for {@link BankTransaction} entity.
 */
public class BankTransactionRepository {
    private static final BankTransactionsTable BANK_TRANSACTIONS = BankTransactionsTable.BANK_TRANSACTIONS;
    private final DSLContext ctx;

    public BankTransactionRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public void save(BankTransaction bankTrs) {
        ctx.insertInto(BANK_TRANSACTIONS)
                .set(bankTrs.intoMap())
                .onDuplicateKeyIgnore()
                .execute();
    }

    /**
     * Fetches a set of item transactions of the given action and item types after
     * the timestamp provided.
     *
     * @param after the unix time (in seconds).
     * @param buyerId the person who issued the action on the items.
     * @param action the action the user took (usually buying or selling something).
     * @param items the types of item that should be fetched.
     * @return a list of items after the provided timestamp,
     *         with the given transaction type and item types.
     */
    public List<BankTransaction> findByItemTypesAndUserAfter(long after, long buyerId, @NotNull TransactionType action, @NotNull StoreItemType... items) {
        return findByItemTypesAndUserAfter(after, buyerId, action, List.of(items));
    }

    /**
     * Fetches a set of item transactions of the given action and item types after
     * the timestamp provided.
     *
     * @param after the unix time (in seconds).
     * @param buyerId the person who issued the action on the items.
     * @param action the action the user took (usually buying or selling something).
     * @param items the types of item that should be fetched.
     * @return a list of items after the provided timestamp,
     *         with the given transaction type and item types.
     */
    public List<BankTransaction> findByItemTypesAndUserAfter(long after, long buyerId, @NotNull TransactionType action, @NotNull List<StoreItemType> items) {
        List<String> itemsNames = items.stream().map(StoreItemType::toString).toList();

        return ctx.selectFrom(BANK_TRANSACTIONS)
                .where(BANK_TRANSACTIONS.CREATED_AT.ge(after))
                .and(BANK_TRANSACTIONS.USER_ID.eq(buyerId))
                .and(BANK_TRANSACTIONS.ACTION.eq(action.toString()))
                .and(itemsNames.isEmpty() ? noCondition() : BANK_TRANSACTIONS.PRODUCT.in(itemsNames))
                .fetch();
    }
}