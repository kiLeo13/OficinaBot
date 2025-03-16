package ofc.bot.domain.sqlite.repository;

import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.commands.economy.LeaderboardCommand;
import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.UserEconomy;
import ofc.bot.domain.sqlite.MapperFactory;
import ofc.bot.domain.tables.UsersEconomyTable;
import ofc.bot.domain.tables.UsersTable;
import ofc.bot.domain.viewmodels.LeaderboardUser;
import ofc.bot.domain.viewmodels.LeaderboardView;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Result;

import java.util.List;

/**
 * Repository for {@link UserEconomy} entity.
 */
public class UserEconomyRepository extends Repository<UserEconomy> {
    private static final UsersEconomyTable USERS_ECONOMY = UsersEconomyTable.USERS_ECONOMY;
    private static final UsersTable USERS = UsersTable.USERS;

    public UserEconomyRepository(DSLContext ctx) {
        super(ctx);
    }

    @NotNull
    @Override
    public InitializableTable<UserEconomy> getTable() {
        return USERS_ECONOMY;
    }

    public boolean hasEnoughWallet(long userId, int amount) {
        return ctx.fetchExists(
                USERS_ECONOMY,
                USERS_ECONOMY.USER_ID.eq(userId).and(USERS_ECONOMY.WALLET.ge(amount))
        );
    }

    public boolean hasEnoughBank(long userId, int amount) {
        return ctx.fetchExists(
                USERS_ECONOMY,
                USERS_ECONOMY.USER_ID.eq(userId).and(USERS_ECONOMY.BANK.ge(amount))
        );
    }

    /**
     * Checks both {@link UserEconomy#getBank() bank} and {@link UserEconomy#getWallet() wallet}.
     * <p>
     * This method sums both values and check against the provided {@code amount}.
     * If you want to check each value independently, call {@link #hasEnoughWallet(long, int)}
     * or {@link #hasEnoughBank(long, int)} instead.
     * <p>
     * If the provided {@code userId} does not exist in the database, {@code false} is returned.
     *
     * @param userId The ID of the user to be checked.
     * @param amount The amount to be checked against the user's total balance.
     * @return {@code true} if the users has, in total, the enough money provided, {@code false} otherwise.
     */
    public boolean hasEnough(long userId, long amount) {
        return ctx.fetchExists(
                USERS_ECONOMY,
                USERS_ECONOMY.USER_ID.eq(userId)
                        .and(USERS_ECONOMY.WALLET.plus(USERS_ECONOMY.BANK).cast(long.class).ge(amount))
        );
    }

    public int fetchWalletByUserId(long userId) {
        return ctx.select(USERS_ECONOMY.WALLET)
                .from(USERS_ECONOMY)
                .where(USERS_ECONOMY.USER_ID.eq(userId))
                .fetchOptionalInto(int.class)
                .orElse(0);
    }

    /**
     * Transfers the money from the {@code from}'s wallet to {@code to}.
     * <p>
     * This method does not run any checks, except for "int32" overflows.
     * <p>
     * This method has 2 parameters ({@code send} and {@code take}) because it allows
     * the caller to define some taxes over the money to be transferred.
     *
     * @param from Who is sending the money to {@code to}.
     * @param to Who is receiving the money from {@code from}.
     * @param send The amount to be sent to {@code to}.
     * @param take The amount to be removed from {@code from}.
     * @throws IllegalArgumentException If:
     *            <ul>
     *              <li>{@code send} or {@code take} are negative or {@code 0}.</li>
     *              <li>{@code from} or {@code to} are {@code null}.</li>
     *            </ul>
     */
    public void transfer(@NotNull UserEconomy from, @NotNull UserEconomy to, int send, int take) {
        Checks.notNull(from, "From User");
        Checks.notNull(to, "To User");
        Checks.positive(send, "Send");
        Checks.positive(take, "Take");

        ctx.transaction((cfg) -> {
            DSLContext trsCtx = cfg.dsl();

            from.modifyBalance(-take, 0).tickUpdate();
            to.modifyBalance(send, 0).tickUpdate();

            upsert(trsCtx, from);
            upsert(trsCtx, to);
        });
    }

    public void transfer(long fromId, long toId, int send, int take) {
        UserEconomy from = findByUserId(fromId);

        if (from == null)
            throw new UnsupportedOperationException("User for ID " + fromId + " does not exist. " +
                    "How are we supposed to take money from a user that has never interacted with our economy???");

        // The receiver user does not have to actually exist, we can pseudo-generate a new one
        UserEconomy to = findByUserId(toId, UserEconomy.fromUserId(toId));
        transfer(from, to, send, take);
    }

    public void transfer(UserEconomy from, UserEconomy to, int amount) {
        transfer(from, to, amount, amount);
    }

    public void transfer(long fromId, long toId, int amount) {
        transfer(fromId, toId, amount, amount);
    }

    public UserEconomy findByUserId(long userId) {
        return ctx.selectFrom(USERS_ECONOMY)
                .where(USERS_ECONOMY.USER_ID.eq(userId))
                .fetchOne();
    }

    public UserEconomy findByUserId(long userId, UserEconomy fallback) {
        UserEconomy eco = findByUserId(userId);
        return eco == null ? fallback : eco;
    }

    /**
     * This method returns the last time the user has
     * run the {@code /daily} command successfully.
     * <p>
     * If no users for the given id are present in this table,
     * {@code 0} is returned.
     *
     * @param userId the id of the user to be checked.
     * @return the last time the user has successfully run the {@code /daily} command,
     * {@code 0} otherwise.
     */
    public long fetchLastDailyByUserId(long userId) {
        return ctx.select(USERS_ECONOMY.LAST_DAILY_AT)
                .from(USERS_ECONOMY)
                .where(USERS_ECONOMY.USER_ID.eq(userId))
                .fetchOptionalInto(long.class)
                .orElse(0L);
    }

    /**
     * This method returns the last time the user has
     * run the {@code /work} command successfully.
     * <p>
     * If no users for the given id are present in this table,
     * {@code 0} is returned.
     *
     * @param userId the id of the user to be checked.
     * @return the last time the user has successfully run the {@code /work} command,
     * {@code 0} otherwise.
     */
    public long fetchLastWorkByUserId(long userId) {
        return ctx.select(USERS_ECONOMY.LAST_WORK_AT)
                .from(USERS_ECONOMY)
                .where(USERS_ECONOMY.USER_ID.eq(userId))
                .fetchOptionalInto(long.class)
                .orElse(0L);
    }

    /**
     * Retrieves the rank of a user based on the specified {@link UserEconomy} instance.
     * <p>
     * If {@code null} is provided in the {@code eco} parameter, {@code 0} is returned.
     * <p>
     * <b>Note:</b> While <i>YOU CAN</i> use {@linkplain UserEconomy#isGenerated() generated}
     * records to estimate where a particular amount or user would fit in the leaderboard,
     * be aware that generated records are not fetched from the database,
     * and their ranking might be imprecise.
     *
     * @param eco the {@link UserEconomy} instance representing the user's economy data.
     * @return the rank of the user, or {@code 0} if {@code eco} is {@code null}.
     */
    public int findRankByUser(@Nullable UserEconomy eco) {
        if (eco == null) return 0;

        long userId = eco.getUserId();
        long total = eco.getTotal();
        return ctx.fetchCount(
                USERS_ECONOMY,
                USERS_ECONOMY.WALLET.plus(USERS_ECONOMY.BANK).cast(long.class).gt(total)
                        .or(USERS_ECONOMY.WALLET.plus(USERS_ECONOMY.BANK).cast(long.class).eq(total)
                                .and(USERS_ECONOMY.USER_ID.gt(userId)))
        ) + 1;
    }

    public int findRankByUserId(long userId) {
        return findRankByUser(findByUserId(userId));
    }

    public LeaderboardView viewLeaderboard(int pageIndex) {
        int offset = pageIndex * LeaderboardCommand.MAX_USERS_PER_PAGE;
        int rowsCount = ctx.fetchCount(USERS_ECONOMY);
        Result<?> userEconomyData = ctx.select(USERS_ECONOMY.USER_ID, USERS_ECONOMY.WALLET, USERS_ECONOMY.BANK, USERS.NAME)
                .from(USERS_ECONOMY)
                .leftJoin(USERS).on(USERS_ECONOMY.USER_ID.eq(USERS.ID))
                .groupBy(USERS_ECONOMY.USER_ID)
                .orderBy(USERS_ECONOMY.BANK.plus(USERS_ECONOMY.WALLET).desc())
                .offset(offset)
                .limit(10)
                .fetch();

        int maxPages = Bot.calcMaxPages(rowsCount, LeaderboardCommand.MAX_USERS_PER_PAGE);
        List<LeaderboardUser> usersView = userEconomyData.map(MapperFactory::mapLeaderboardUsers);
        return new LeaderboardView(usersView, pageIndex, maxPages);
    }
}