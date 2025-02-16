package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.tables.BankTransactionsTable;
import ofc.bot.handlers.economy.CurrencyType;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

import java.util.List;

import static org.jooq.impl.DSL.noCondition;

/**
 * Repository for {@link BankTransaction} entity.
 */
public class BankTransactionRepository extends Repository<BankTransaction> {
    private static final BankTransactionsTable BANK_TRANSACTIONS = BankTransactionsTable.BANK_TRANSACTIONS;

    public BankTransactionRepository(DSLContext ctx) {
        super(ctx);
    }

    @NotNull
    @Override
    public InitializableTable<BankTransaction> getTable() {
        return BANK_TRANSACTIONS;
    }

    public int countUserTransactions(long userId, List<CurrencyType> currencies, List<TransactionType> actions) {
        List<String> sqlCurrencies = currencies.stream().map(CurrencyType::name).toList();
        List<String> sqlActions = actions.stream().map(TransactionType::name).toList();
        return ctx.fetchCount(BANK_TRANSACTIONS,
                BANK_TRANSACTIONS.USER_ID.eq(userId)
                        .or(BANK_TRANSACTIONS.RECEIVER_ID.eq(userId))
                        .and(BANK_TRANSACTIONS.CURRENCY.in(sqlCurrencies))
                        .and(BANK_TRANSACTIONS.ACTION.in(sqlActions))
        );
    }

    public List<BankTransaction> findUserTransactions(
            long userId, List<CurrencyType> currencies, List<TransactionType> actions, int limit, int offset
    ) {
        List<String> sqlCurrencies = currencies.stream().map(CurrencyType::name).toList();
        List<String> sqlActions = actions.stream().map(TransactionType::name).toList();
        return ctx.selectFrom(BANK_TRANSACTIONS)
                .where(BANK_TRANSACTIONS.USER_ID.eq(userId).or(BANK_TRANSACTIONS.RECEIVER_ID.eq(userId)))
                .and(BANK_TRANSACTIONS.CURRENCY.in(sqlCurrencies))
                .and(BANK_TRANSACTIONS.ACTION.in(sqlActions))
                .orderBy(BANK_TRANSACTIONS.CREATED_AT.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
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