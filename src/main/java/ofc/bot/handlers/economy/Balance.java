package ofc.bot.handlers.economy;

public class Balance {
    private String rank;
    private String user_id;
    private long guildId;
    private long cash;
    private long bank;
    private long total;

    protected Balance setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }

    /**
     * Updates the {@link Balance} of the user without providing a reason for it.
     * Use {@link #update(long, long, String) Balance.update(long, long, String)}
     * to provide a reason.
     * <p>
     * If you want to <u>set</u> a new balance, use {@link #set(long, long) Balance.set(long, long)}
     * instead.
     *
     * @param cash the {@code cash} amount to be updated (provide a negative value to remove).
     * @param bank the {@code bank} amount to be updated (provide a negative value to remove).
     * @return the new {@link Balance} with the updated values.
     *
     * @see #update(long, long, String)
     * @see #set(long, long, String)
     * @see #set(long, long)
     */
    public Balance update(long cash, long bank) {
        return update(cash, bank, null);
    }

    /**
     * Updates the {@link Balance} of the user.
     * <p>
     * If you want to <u>set</u> a new balance, use {@link #set(long, long, String) Balance.set(long, long, String)}
     * instead.
     *
     * @param cash the {@code cash} amount to be updated (provide a negative value to remove).
     * @param bank the {@code bank} amount to be updated (provide a negative value to remove).
     * @param reason the reason of the update.
     * @return the new {@link Balance} with the updated values.
     *
     * @see #update(long, long)
     * @see #set(long, long, String)
     * @see #set(long, long)
     */
    public Balance update(long cash, long bank, String reason) {
        return UEconomyManager.updateBalance(this.guildId, Long.parseLong(this.user_id), cash, bank, reason);
    }

    /**
     * Sets a new {@link Balance} to the user without providing a reason for it.
     * Use {@link #set(long, long, String) Balance.set(long, long, String)}
     * to provide a reason.
     * <p>
     * If you want to <u>update</u> their current balance,
     * use {@link #update(long, long) Balance.update(long, long)} instead.
     *
     * @param cash the new {@code cash} amount.
     * @param bank the new {@code bank} amount.
     * @return the new {@link Balance} with the updated values.
     *
     * @see #set(long, long, String)
     * @see #update(long, long, String)
     * @see #update(long, long)
     */
    public Balance set(long cash, long bank) {
        return set(cash, bank, null);
    }

    /**
     * Sets a new {@link Balance} to the user.
     * <p>
     * If you want to <u>update</u> their current balance,
     * use {@link #update(long, long, String) Balance.update(long, long, String)} instead.
     *
     * @param cash the new {@code cash} amount.
     * @param bank the new {@code bank} amount.
     * @param reason the reason of the update.
     * @return the new {@link Balance} with the updated values.
     *
     * @see #set(long, long)
     * @see #update(long, long, String)
     * @see #update(long, long)
     */
    public Balance set(long cash, long bank, String reason) {
        return UEconomyManager.setBalance(this.guildId, Long.parseLong(this.user_id), cash, bank, reason);
    }

    /**
     * Resets the current {@link Balance} of the user.
     * <p>
     * To provide a reason, use {@link #reset(String) Balance.reset(String)} instead.
     *
     * @return the new {@link Balance} with the updated values.
     */
    public Balance reset() {
        return reset(null);
    }

    /**
     * Resets the current {@link Balance} of the user.
     *
     * @param reason the reason of the reset.
     * @return the new {@link Balance} with the updated values.
     */
    public Balance reset(String reason) {
        return set(0, 0, reason);
    }

    public String getRank() {
        return this.rank;
    }

    public String getUserId() {
        return this.user_id;
    }

    public long getGuildId() {
        return this.guildId;
    }

    public long getCash() {
        return this.cash;
    }

    public long getBank() {
        return this.bank;
    }

    public long getTotal() {
        return this.total;
    }
}