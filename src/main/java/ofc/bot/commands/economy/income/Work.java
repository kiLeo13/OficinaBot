package ofc.bot.commands.economy.income;

import net.dv8tion.jda.api.entities.Member;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.users.MembersDAO;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static ofc.bot.databases.entities.tables.Economy.ECONOMY;

@DiscordCommand(name = "work", description = "Trabalhe para ganhar dinheiro.")
public class Work extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Work.class);
    private static final Random random = new Random();
    private static final long COOLDOWN = 2700; // Seconds

    private static final double BOOSTER_EXTRA_PERCENTAGE = 1.2;
    private static final int MIN = 50;
    private static final int MAX = 200;

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Member sender = ctx.getIssuer();
        long userId = sender.getIdLong();
        long nextWorkAt = getNextWork(userId);
        long now = Bot.unixNow();

        if (nextWorkAt > now)
            return Status.WAIT_BEFORE_WORK_AGAIN.args(nextWorkAt);

        try {
            boolean boosting = sender.isBoosting();
            int randomInt = random.nextInt(MIN, MAX + 1);
            int value = boosting
                    ? (int) (randomInt * BOOSTER_EXTRA_PERCENTAGE)
                    : randomInt;
            String prettyValue = Bot.strfNumber(value);
            String prettyDifference = Bot.strfNumber(value - randomInt);

            work(userId, value);
            MembersDAO.upsertUser(sender.getUser());

            if (boosting)
                return Status.WORK_SUCCESSFUL_BOOSTING.args(prettyValue, prettyDifference);
            else
                return Status.WORK_SUCCESSFUL.args(prettyValue);

        } catch (DataAccessException e) {
            LOGGER.error("Could not perform work operation for user '{}'", userId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private void work(long userId, int value) {

        DSLContext ctx = DBManager.getContext();
        updateBalanceWithWork(ctx, userId, value);
    }

    private void updateBalanceWithWork(DSLContext ctx, long userId, long amount) {

        long timestamp = Bot.unixNow();

        ctx.insertInto(ECONOMY)
                .set(ECONOMY.USER_ID, userId)
                .set(ECONOMY.BALANCE, amount)
                .set(ECONOMY.LAST_WORK_AT, timestamp)
                .set(ECONOMY.CREATED_AT, timestamp)
                .set(ECONOMY.UPDATED_AT, timestamp)
                .onDuplicateKeyUpdate()
                .set(ECONOMY.USER_ID, userId)
                .set(ECONOMY.BALANCE, ECONOMY.BALANCE.plus(amount))
                .set(ECONOMY.LAST_WORK_AT, timestamp)
                .set(ECONOMY.UPDATED_AT, timestamp)
                .execute();
    }

    private long getNextWork(long userId) {

        DSLContext ctx = DBManager.getContext();

        Long lastWork = ctx.select(ECONOMY.LAST_WORK_AT)
                .from(ECONOMY)
                .where(ECONOMY.USER_ID.eq(userId))
                .fetchOneInto(long.class);

        return lastWork == null
                ? 0
                : lastWork + COOLDOWN;
    }
}