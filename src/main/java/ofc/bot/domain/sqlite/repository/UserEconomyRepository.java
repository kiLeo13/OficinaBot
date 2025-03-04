package ofc.bot.domain.sqlite.repository;

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

    public boolean hasEnough(long userId, long amount) {
        return ctx.fetchExists(
                USERS_ECONOMY,
                USERS_ECONOMY.USER_ID.eq(userId).and(USERS_ECONOMY.BALANCE.ge(amount))
        );
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

    public void transfer(long senderId, long receiverId, long amount) {
        transfer(senderId, receiverId, amount, amount);
    }

    public void updateCtx(DSLContext trsCtx, UserEconomy eco) {
        trsCtx.update(USERS_ECONOMY)
                .set(eco)
                .where(USERS_ECONOMY.USER_ID.eq(eco.getUserId()))
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
     * Finds the rank of a user based on the {@code eco} parameter.
     * <p>
     * If {@code null} is provided in the {@code eco} parameter, then {@code 0}
     * is returned, meaning the user has no rank.
     *
     * @param eco the {@link UserEconomy} to fetch the rank.
     * @return the rank of the user, or {@code 0} if {@code eco} is null.
     */
    public int findRankByUser(@Nullable UserEconomy eco) {
        if (eco == null) return 0;

        long userId = eco.getUserId();
        long balance = eco.getBalance();
        return ctx.fetchCount(
                USERS_ECONOMY,
                USERS_ECONOMY.BALANCE.gt(balance)
                        .or(USERS_ECONOMY.BALANCE.eq(balance).and(USERS_ECONOMY.USER_ID.gt(userId)))
        ) + 1;
    }

    public int findRankByUserId(long userId) {
        return findRankByUser(findByUserId(userId));
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