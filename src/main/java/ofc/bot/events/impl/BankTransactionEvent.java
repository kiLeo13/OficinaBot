package ofc.bot.events.impl;

import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.events.EventListener;
import ofc.bot.events.eventbus.GenericApplicationEvent;

public class BankTransactionEvent extends GenericApplicationEvent {
    private final BankTransaction transaction;

    public BankTransactionEvent(BankTransaction itemTransaction) {
        this.transaction = itemTransaction;
    }

    public long getBuyerId() {
        return transaction.getReceiverId();
    }

    public long getReceiverId() {
        return transaction.getReceiverId();
    }

    public BankTransaction getTransaction() {
        return transaction;
    }

    public TransactionType getTransactionType() {
        return transaction.getAction();
    }

    /**
     * Checks whether this transaction was targeted to the self user.
     * <p>
     * If this method returns {@code true}, then calling {@link #getReceiverId()}
     * will always throw a {@link NullPointerException} as there is no receiver
     * (the receiver is kind of who bought the item — the {@link #getBuyerId()}).
     *
     * @return {@code true} if the user actioned this transaction to themself, {@code false}
     *         otherwise (likely a gift or giveaway).
     */
    public boolean isSelfAction() {
        return transaction.getReceiverId() != null;
    }

    /**
     * The item involved in this event (if any), whether it was bought or sold.
     * <p>
     * Keep in mind that, if the action does not involve an item
     * (such as {@link TransactionType#BALANCE_UPDATED}), this method
     * will return {@code null}.
     *
     * @return the item in regard to this event.
     */
    public StoreItemType getTargetItemType() {
        return transaction.getProduct();
    }

    /**
     * The amount of money involved.
     * <p>
     * Keep in mind that, negative values do not exist
     * for transactions.
     * <p>
     * Keep in mind that negative values do exist here,
     * if this method returns a negative amount, then the
     * user spent money on this operation.
     *
     * @return the amount of money involved.
     */
    public long getAmount() {
        return transaction.getAmount();
    }

    @Override
    protected void invoke(EventListener listener) {
        listener.onBankTransaction(this);
    }
}