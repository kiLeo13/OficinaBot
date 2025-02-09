package ofc.bot.domain.entity;

import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.RepositoryFactory;
import ofc.bot.domain.sqlite.repository.UserRepository;
import ofc.bot.domain.tables.BankTransactionsTable;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.impl.TableRecordImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankTransaction extends TableRecordImpl<BankTransaction> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BankTransaction.class);
    private static final BankTransactionsTable BANK_TRANSACTIONS = BankTransactionsTable.BANK_TRANSACTIONS;

    public BankTransaction() {
        super(BANK_TRANSACTIONS);
    }

    public BankTransaction(long userId, @Nullable Long receiverId, long amount,
                           @Nullable String comment, @NotNull CurrencyType currencyType,
                           @NotNull TransactionType action, @Nullable StoreItemType product, long createdAt) {
        this();
        set(BANK_TRANSACTIONS.USER_ID, userId);
        set(BANK_TRANSACTIONS.RECEIVER_ID, receiverId);
        set(BANK_TRANSACTIONS.CURRENCY, currencyType.toString());
        set(BANK_TRANSACTIONS.AMOUNT, amount);
        set(BANK_TRANSACTIONS.COMMENT, comment);
        set(BANK_TRANSACTIONS.ACTION, action.toString());
        set(BANK_TRANSACTIONS.PRODUCT, product == null ? null : product.toString());
        set(BANK_TRANSACTIONS.CREATED_AT, createdAt);

        if (action.isApplicableOnItems() && product == null)
            LOGGER.warn("Item-related operation ({}) of amount {} has no declared product", action, amount);

        if (action == TransactionType.ITEM_BOUGHT && amount > 0)
            LOGGER.warn("ITEM_BOUGHT operation of item \"{}\" has amount greater than 0 ({}), maybe -{} was intended?", getProduct(), amount, amount);

        if (action == TransactionType.ITEM_SOLD && amount < 0)
            LOGGER.warn("ITEM_SOLD operation of item \"{}\" has negative amount ({}), maybe {} was intended?", getProduct(), amount, -amount);
    }

    public BankTransaction(long userId, @Nullable Long receiverId,
                           long amount, @Nullable String comment, @NotNull CurrencyType currencyType,
                           @NotNull TransactionType action, @Nullable StoreItemType product) {
        this(userId, receiverId, amount, comment, currencyType, action, product, Bot.unixNow());
    }

    public BankTransaction(long userId, @Nullable Long receiverId,
                           long amount, @Nullable String comment, @NotNull CurrencyType currencyType,
                           @NotNull TransactionType action) {
        this(userId, receiverId, amount, comment, currencyType, action, null);
    }

    public BankTransaction(long userId, @Nullable Long receiverId,
                           long amount, @NotNull CurrencyType currencyType,
                           @NotNull TransactionType action) {
        this(userId, receiverId, amount, null, currencyType, action, null);
    }

    public BankTransaction(long userId, long amount,
                           @Nullable String comment, @NotNull CurrencyType currencyType,
                           @NotNull TransactionType action, @Nullable StoreItemType product, long createdAt) {
        this(userId, null, amount, comment, currencyType, action, product, createdAt);
    }

    public BankTransaction(long userId, long amount,
                           @Nullable String comment, @NotNull CurrencyType currencyType,
                           @NotNull TransactionType action, @Nullable StoreItemType product) {
        this(userId, null, amount, comment, currencyType, action, product, Bot.unixNow());
    }

    public BankTransaction(long userId, long amount, @NotNull CurrencyType currencyType,
                           @NotNull TransactionType action, @Nullable StoreItemType product) {
        this(userId, amount, null, currencyType, action, product);
    }

    public BankTransaction(long userId, long amount, @Nullable String comment,
                           @NotNull CurrencyType currencyType, @NotNull TransactionType action) {
        this(userId, amount, comment, currencyType, action, null);
    }

    public BankTransaction(long userId, long amount, @NotNull CurrencyType currencyType,
                           @NotNull TransactionType action) {
        this(userId, amount, null, currencyType, action);
    }

    public int getId() {
        return get(BANK_TRANSACTIONS.ID);
    }

    public long getUserId() {
        return get(BANK_TRANSACTIONS.USER_ID);
    }

    public Long getReceiverId() {
        return get(BANK_TRANSACTIONS.RECEIVER_ID);
    }

    public CurrencyType getCurrencyType() {
        String curr = get(BANK_TRANSACTIONS.CURRENCY);
        return curr == null ? null : CurrencyType.fromName(curr);
    }

    public long getAmount() {
        return get(BANK_TRANSACTIONS.AMOUNT);
    }

    public TransactionType getAction() {
        String action = get(BANK_TRANSACTIONS.ACTION);
        return TransactionType.valueOf(action);
    }

    public String getComment() {
        return get(BANK_TRANSACTIONS.COMMENT);
    }

    /**
     * The product involved in this buy/sell action.
     * <p>
     * May return {@code null} if:
     * <ul>
     *   <li>The column was not selected in the query.</li>
     *   <li>The bank transaction was not from an item being bought/sold.</li>
     * </ul>
     *
     * @return the product involved in this transaction.
     */
    public StoreItemType getProduct() {
        String product = get(BANK_TRANSACTIONS.PRODUCT);
        return product == null ? null : StoreItemType.fromName(product);
    }

    public long getTimeCreated() {
        return get(BANK_TRANSACTIONS.CREATED_AT);
    }

    public BankTransaction setUserId(long userId) {
        set(BANK_TRANSACTIONS.USER_ID, userId);
        return this;
    }

    @NotNull
    public AppUser retrieveUser() {
        UserRepository userRepo = RepositoryFactory.getUserRepository();
        AppUser user = userRepo.findById(getUserId());
        if (user == null)
            throw new IllegalStateException("For some unknown reason, the user " + getUserId() +
                    " does not seem to be in our database, wtf???");
        return user;
    }

    public BankTransaction setReceiverId(long receiverId) {
        set(BANK_TRANSACTIONS.RECEIVER_ID, receiverId);
        return this;
    }

    @Nullable
    public AppUser retrieveReceiver() {
        Long recId = getReceiverId();
        if (recId == null) return null;

        UserRepository userRepo = RepositoryFactory.getUserRepository();
        AppUser user = userRepo.findById(recId);
        if (user == null)
            throw new IllegalStateException("For some unknown reason, the user " + getUserId() +
                    " does not seem to be in our database, wtf???");
        return user;
    }

    public BankTransaction setAmount(long amount) {
        set(BANK_TRANSACTIONS.AMOUNT, amount);
        return this;
    }

    public BankTransaction setAction(TransactionType action) {
        set(BANK_TRANSACTIONS.ACTION, action.toString());
        return this;
    }

    public BankTransaction setComment(String comment) {
        set(BANK_TRANSACTIONS.COMMENT, comment);
        return this;
    }

    public BankTransaction setTimeCreated(long timeCreated) {
        set(BANK_TRANSACTIONS.CREATED_AT, timeCreated);
        return this;
    }
}