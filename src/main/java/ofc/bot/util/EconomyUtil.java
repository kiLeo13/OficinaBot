package ofc.bot.util;

import ofc.bot.databases.DBManager;
import org.jooq.DSLContext;

import static ofc.bot.databases.entities.tables.Economy.ECONOMY;

public class EconomyUtil {
    private static final DSLContext context = DBManager.getContext();
    
    public static long fetchBalance(long userId) {

        return context.select(ECONOMY.BALANCE)
                .from(ECONOMY)
                .where(ECONOMY.USER_ID.eq(userId))
                .fetchOptionalInto(long.class)
                .orElse(0L);
    }

    public static void setBalance(long amount, long userId) {

        long timestamp = Bot.unixNow();

        context.insertInto(ECONOMY)
                .set(ECONOMY.USER_ID, userId)
                .set(ECONOMY.BALANCE, amount)
                .set(ECONOMY.LAST_DAILY_AT, 0L)
                .set(ECONOMY.CREATED_AT, timestamp)
                .onDuplicateKeyUpdate()
                .set(ECONOMY.USER_ID, userId)
                .set(ECONOMY.BALANCE, amount)
                .set(ECONOMY.UPDATED_AT, timestamp)
                .execute();
    }

    public static void updateBalance(long userId, long amount) {
        updateBalance(DBManager.getContext(), userId, amount);
    }

    public static void updateBalance(DSLContext ctx, long userId, long amount) {

        long timestamp = Bot.unixNow();

        ctx.insertInto(ECONOMY)
                .set(ECONOMY.USER_ID, userId)
                .set(ECONOMY.BALANCE, amount)
                .set(ECONOMY.CREATED_AT, timestamp)
                .set(ECONOMY.UPDATED_AT, timestamp)
                .onDuplicateKeyUpdate()
                .set(ECONOMY.USER_ID, userId)
                .set(ECONOMY.BALANCE, ECONOMY.BALANCE.plus(amount))
                .set(ECONOMY.UPDATED_AT, timestamp)
                .execute();
    }

    public static boolean willOverflow(long a, long b) {

        if (b > 0)
            return a > Long.MAX_VALUE - b;

        if (b < 0)
            return a < Long.MIN_VALUE - b;

        return false;
    }
}