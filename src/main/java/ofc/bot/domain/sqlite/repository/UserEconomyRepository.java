package ofc.bot.domain.sqlite.repository;

import ofc.bot.commands.economy.LeaderboardCommand;
import ofc.bot.domain.entity.UserEconomy;
import ofc.bot.domain.sqlite.MapperFactory;
import ofc.bot.domain.tables.UsersEconomyTable;
import ofc.bot.domain.tables.UsersTable;
import ofc.bot.domain.viewmodels.BalanceView;
import ofc.bot.domain.viewmodels.LeaderboardUser;
import ofc.bot.domain.viewmodels.LeaderboardView;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Result;

import java.util.List;

import static org.jooq.impl.DSL.*;

/**
 * Repository for {@link ofc.bot.domain.entity.UserEconomy UserEconomy} entity.
 */
public class UserEconomyRepository {
    private static final BalanceView EMPTY_BALANCE_DATA = new BalanceView(0, 0, 0, 0, 0, 0, false);
    private static final UsersEconomyTable USERS_ECONOMY = UsersEconomyTable.USERS_ECONOMY;
    private static final UsersTable USERS = UsersTable.USERS;
    private final DSLContext ctx;

    public UserEconomyRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Attempts to save a new {@link UserEconomy} record to the database.
     * If it collides, an UPDATE is performed, as always,
     * excluding the {@code created_at} column.
     *
     * @param user the user economy to be "upsert".
     * @return a new {@link UserEconomy} instance with the updated values.
     */
    public UserEconomy upsert(UserEconomy user) {
        user.changed(USERS_ECONOMY.CREATED_AT, false); // To avoid updating this field
        ctx.insertInto(USERS_ECONOMY)
                .set(user.intoMap())
                .onDuplicateKeyUpdate()
                .set(user)
                .execute();

        return findByUserId(user.getUserId());
    }

    public long fetchBalanceByUserId(long userId) {
        return ctx.select(USERS_ECONOMY.BALANCE)
                .from(USERS_ECONOMY)
                .where(USERS_ECONOMY.USER_ID.eq(userId))
                .fetchOptionalInto(long.class)
                .orElse(0L);
    }

    public void transfer(long senderId, long receiverId, long amountToSend, long totalProvided) {
        ctx.transaction((cfg) -> {
            DSLContext trsCtx = cfg.dsl();

            updateBalanceCtx(trsCtx, senderId, -totalProvided);
            updateBalanceCtx(trsCtx, receiverId, amountToSend);
        });
    }

    public void updateCtx(DSLContext trsCtx, UserEconomy eco) {
        trsCtx.update(USERS_ECONOMY)
                .set(eco)
                .execute();
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
     * Returns the rank of the user for the supplied ID, ranging from {@code 1}
     * to {@code N}, where {@code 1} has the highest/best position.
     * <p>
     * If the user is not found in the leaderboard, {@link Integer#MAX_VALUE} is returned instead.
     * <p>
     * <b>Note:</b> You should avoid using this function multiple times, as this is a heavy operation
     * (specially in larger datasets), scanning the whole table every call.
     *
     * @param userId the ID of the user to have their rank fetched.
     * @return the rank of the user with the given ID (if found),
     * {@link Integer#MAX_VALUE} otherwise.
     */
    public int findRankByUserId(long userId) {
        return ctx.with("ranked_economy").as(
                        select(
                                USERS_ECONOMY.USER_ID,
                                rowNumber().over(orderBy(USERS_ECONOMY.BALANCE.desc())).as("rank")
                        ).from(USERS_ECONOMY))
                .select(
                        field(name("ranked_economy", "rank"), int.class)
                )
                .from(table(name("ranked_economy")))
                .join(USERS_ECONOMY)
                .on(field(name("ranked_economy", "user_id"), long.class).eq(USERS_ECONOMY.USER_ID))
                .join(USERS)
                .on(USERS.ID.eq(field(name("ranked_economy", "user_id"), long.class)))
                .where(field(name("ranked_economy", "user_id")).eq(userId))
                .fetchOptionalInto(int.class)
                .orElse(0);
    }

    @NotNull
    public BalanceView viewBalance(long userId) {
        UserEconomy userEco = findByUserId(userId);

        if (userEco == null) return EMPTY_BALANCE_DATA;

        int rank = findRankByUserId(userId);
        return BalanceView.of(userEco, rank);
    }

    public LeaderboardView viewLeaderboard(int pageIndex) {
        int offset = pageIndex * LeaderboardCommand.MAX_USERS_PER_PAGE;
        int rowsCount = ctx.fetchCount(USERS_ECONOMY);
        Result<?> userEconomyData = ctx.select(USERS_ECONOMY.USER_ID, USERS_ECONOMY.BALANCE, USERS.NAME)
                .from(USERS_ECONOMY)
                .leftJoin(USERS).on(USERS_ECONOMY.USER_ID.eq(USERS.ID))
                .groupBy(USERS_ECONOMY.USER_ID)
                .orderBy(USERS_ECONOMY.BALANCE.desc())
                .offset(offset)
                .limit(10)
                .fetch();

        int maxPages = Bot.calcMaxPages(rowsCount, LeaderboardCommand.MAX_USERS_PER_PAGE);
        List<LeaderboardUser> usersView = userEconomyData.map(MapperFactory::mapLeaderboardUsers);
        return new LeaderboardView(usersView, pageIndex, maxPages);
    }

    private void updateBalanceCtx(DSLContext ctx, long userId, long amount) {
        long now = Bot.unixNow();
        ctx.insertInto(USERS_ECONOMY)
                .set(USERS_ECONOMY.USER_ID, userId)
                .set(USERS_ECONOMY.BALANCE, amount)
                .set(USERS_ECONOMY.CREATED_AT, now)
                .set(USERS_ECONOMY.UPDATED_AT, now)
                .onDuplicateKeyUpdate()
                .set(USERS_ECONOMY.BALANCE, USERS_ECONOMY.BALANCE.plus(amount))
                .set(USERS_ECONOMY.UPDATED_AT, now)
                .execute();
    }
}