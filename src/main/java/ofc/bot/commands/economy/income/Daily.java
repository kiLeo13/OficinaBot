package ofc.bot.commands.economy.income;

import net.dv8tion.jda.api.entities.Member;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.users.MembersDAO;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;

import static ofc.bot.databases.entities.tables.Economy.ECONOMY;

@DiscordCommand(name = "daily", description = "Colete a sua recompensa diária.")
public class Daily extends SlashCommand {
    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(Daily.class);
    private static final double BOOSTER_EXTRA_PERCENTAGE = 1.2;
    private static final int MIN = 1000;
    private static final int MAX = 2800;

    @Override
    public CommandResult onCommand(CommandContext ctx) {
        
        Member sender = ctx.getIssuer();
        long userId = sender.getIdLong();
        boolean hasCollected = hasCollected(userId);

        if (hasCollected)
            return Status.DAILY_ALREADY_COLLECTED;

        try {
            boolean boosting = sender.isBoosting();
            int randomInt = RANDOM.nextInt(MIN, MAX + 1);
            int value = boosting
                    ? (int) (randomInt * BOOSTER_EXTRA_PERCENTAGE)
                    : randomInt;
            String prettyValue = Bot.strfNumber(value);
            String prettyDifference = Bot.strfNumber(value - randomInt);

            daily(userId, value);
            MembersDAO.upsertUser(sender.getUser());

            if (boosting)
                return Status.DAILY_SUCCESSFULLY_COLLECTED_BOOSTING.args(prettyValue, prettyDifference);
            else
                return Status.DAILY_SUCCESSFULLY_COLLECT.args(prettyValue);

        } catch (DataAccessException e) {
            LOGGER.error("Could not collect daily prize for user '{}'", userId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private void daily(long userId, int value) {

        DSLContext ctx = DBManager.getContext();
        updateBalanceWithDaily(ctx, userId, value);
    }

    private boolean hasCollected(long userId) {

        Instant now = Instant.now();
        Instant lastUsed = lastUsed(userId);

        if (lastUsed == null)
            return false;

        LocalDate nowDate = now.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate lastUsedDate = lastUsed.atZone(ZoneId.systemDefault()).toLocalDate();

        return nowDate.equals(lastUsedDate);
    }

    private void updateBalanceWithDaily(DSLContext ctx, long userId, long amount) {

        long timestamp = Bot.unixNow();

        ctx.insertInto(ECONOMY)
                .set(ECONOMY.USER_ID, userId)
                .set(ECONOMY.BALANCE, amount)
                .set(ECONOMY.LAST_DAILY_AT, timestamp)
                .set(ECONOMY.CREATED_AT, timestamp)
                .set(ECONOMY.UPDATED_AT, timestamp)
                .onDuplicateKeyUpdate()
                .set(ECONOMY.USER_ID, userId)
                .set(ECONOMY.BALANCE, ECONOMY.BALANCE.plus(amount))
                .set(ECONOMY.LAST_DAILY_AT, timestamp)
                .set(ECONOMY.UPDATED_AT, timestamp)
                .execute();
    }

    private Instant lastUsed(long userId) {

        DSLContext ctx = DBManager.getContext();

        Long lastDaily = ctx.select(ECONOMY.LAST_DAILY_AT)
                .from(ECONOMY)
                .where(ECONOMY.USER_ID.eq(userId))
                .fetchOneInto(long.class);

        return lastDaily == null || lastDaily == 0
                ? null
                : Instant.ofEpochSecond(lastDaily);
    }
}