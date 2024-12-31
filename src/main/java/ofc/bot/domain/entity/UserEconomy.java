package ofc.bot.domain.entity;

import ofc.bot.domain.tables.UsersEconomyTable;
import ofc.bot.util.Bot;
import org.jooq.impl.TableRecordImpl;

public class UserEconomy extends TableRecordImpl<UserEconomy> {
    private static final UsersEconomyTable USERS_ECONOMY = UsersEconomyTable.USERS_ECONOMY;

    public static final String LEADERBOARD_BUTTON_SCOPE = "LEADERBOARD";
    public static final String LEADERBOARD_ROW_FORMAT = "%d. `%s`**・**$%s";
    public static final String SYMBOL = "<a:coin:1160091618746060811>";
    public static final String RANK_SYMBOL = "⬆️";
    public static final String BANK_ICON = "https://media.discordapp.net/attachments/506838906872922145/506899959816126493/h5D6Ei0.png";

    public UserEconomy() {
        super(USERS_ECONOMY);
    }

    public UserEconomy(long userId, long balance, long lastDailyAt, long lastWorkAt, long createdAt, long updatedAt) {
        this();
        set(USERS_ECONOMY.USER_ID, userId);
        set(USERS_ECONOMY.BALANCE, balance);
        set(USERS_ECONOMY.LAST_DAILY_AT, lastDailyAt);
        set(USERS_ECONOMY.LAST_WORK_AT, lastWorkAt);
        set(USERS_ECONOMY.CREATED_AT, createdAt);
        set(USERS_ECONOMY.UPDATED_AT, updatedAt);
    }

    /**
     * Instantiates a new {@link UserEconomy} instance on-the-fly
     * with default values set.
     * Including {@code created_at} and {@code updated_at}.
     * <p>
     * You must call
     * {@link ofc.bot.domain.sqlite.repository.UserEconomyRepository#upsert(UserEconomy) UserEconomyRepository.save(UserEconomy)}
     * on this instance in order to persist it.
     *
     * @param userId the id of the user to be instantiated.
     * @return a new {@link UserEconomy} instance.
     */
    public static UserEconomy fromUserId(long userId) {
        long now = Bot.unixNow();
        return new UserEconomy(userId, 0, 0, 0, now, now);
    }

    public long getUserId() {
        return get(USERS_ECONOMY.USER_ID);
    }

    public long getBalance() {
        return get(USERS_ECONOMY.BALANCE);
    }

    public long getLastDailyAt() {
        Long val = get(USERS_ECONOMY.LAST_DAILY_AT);
        return val == null ? 0 : val;
    }

    public long getLastWorkAt() {
        Long val = get(USERS_ECONOMY.LAST_WORK_AT);
        return val == null ? 0 : val;
    }

    public long getTimeCreated() {
        return get(USERS_ECONOMY.CREATED_AT);
    }

    public long getLastUpdated() {
        return get(USERS_ECONOMY.UPDATED_AT);
    }

    public UserEconomy setUserId(long userId) {
        set(USERS_ECONOMY.USER_ID, userId);
        return this;
    }

    public UserEconomy setBalance(long balance) {
        set(USERS_ECONOMY.BALANCE, balance);
        return this;
    }

    public UserEconomy setLastDailyAt(long lastDailyAt) {
        set(USERS_ECONOMY.LAST_DAILY_AT, lastDailyAt);
        return this;
    }

    public UserEconomy setLastWorkAt(long lastWorkAt) {
        set(USERS_ECONOMY.LAST_WORK_AT, lastWorkAt);
        return this;
    }

    public UserEconomy modifyBalance(long amount) {
        setBalance(getBalance() + amount);
        return this;
    }

    public UserEconomy setTimeCreated(long createdAt) {
        set(USERS_ECONOMY.CREATED_AT, createdAt);
        return this;
    }

    public UserEconomy setLastUpdated(long updatedAt) {
        set(USERS_ECONOMY.UPDATED_AT, updatedAt);
        return this;
    }

    /**
     * Shortcut for {@link #setLastUpdated(long)}.
     * Here you don't have to manually provide a timestamp value,
     * the field is updated through the {@link Bot#unixNow()} method call.
     *
     * @return The current instance, for chaining convenience.
     */
    public UserEconomy tickUpdate() {
        return setLastUpdated(Bot.unixNow());
    }
}
