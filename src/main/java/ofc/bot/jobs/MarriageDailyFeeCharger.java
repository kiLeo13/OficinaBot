package ofc.bot.jobs;

import ofc.bot.commands.marriages.Marry;
import ofc.bot.content.annotations.jobs.CronJob;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.MarriageRecord;
import ofc.bot.databases.services.TransactionalService;
import ofc.bot.util.Bot;
import ofc.bot.util.EconomyUtil;
import ofc.bot.util.exclusions.ExclusionType;
import ofc.bot.util.exclusions.ExclusionUtil;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static ofc.bot.databases.entities.tables.Economy.ECONOMY;
import static ofc.bot.databases.entities.tables.Marriages.MARRIAGES;
import static org.jooq.impl.DSL.update;

/**
 * This class is responsible for charging the marriages' fee every day
 * at 12:00 AM, excluding every relationship with users in the {@link ofc.bot.databases.entities.tables.UsersExclusions UsersExclusions}
 * table, of type {@code MARRIAGE_FEE}.
 */
@CronJob(expression = "0 0 0 ? * * *") // Every day at midnight
public class MarriageDailyFeeCharger implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarriageDailyFeeCharger.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        Map<Long, List<MarriageRecord>> marriages = fetchUsersMarriages();
        List<MarriageRecord> divorcedMarriages = new ArrayList<>();

        if (marriages.isEmpty()) {
            LOGGER.info("No married users found");
            return;
        }

        TransactionalService transaction = new TransactionalService();

        marriages.forEach((userId, userMarriages) -> {

            long balance = EconomyUtil.fetchBalance(userId);
            int cost = userMarriages.size() * Marry.DAILY_COST;

            if (balance < cost) {
                LOGGER.warn("Requester {} cannot afford all of its relationships", userId);
                List<MarriageRecord> divorced = removeMarriagesFromUser(balance, userId, userMarriages, transaction);
                divorcedMarriages.addAll(divorced);
            }
        });

        chargeUsers(marriages, transaction);

        transaction.getQueued().forEach(q -> System.out.println(unbindQuery(q.getSQL(), q.getBindValues())));

        if (divorcedMarriages.isEmpty())
            LOGGER.info("All members were able to afford their relationships");
        else
            LOGGER.info("The following users were not able to afford their relationships taxes: {}", formatMarriages(divorcedMarriages));
    }

    private String unbindQuery(String sql, List<Object> values) {
        StringBuilder valueBuilder = new StringBuilder(sql);
        int index = 0;

        for (Object val : values) {
            index = valueBuilder.indexOf("?", index);

            if (index == -1) {
                break;
            }

            valueBuilder.replace(index, index + 1, String.valueOf(val));
            index += String.valueOf(val).length();
        }

        return valueBuilder.toString();
    }

    private static void chargeUsers(Map<Long, List<MarriageRecord>> marriages, TransactionalService trs) {

        marriages.forEach((userId, relationships) -> {

            // Remove all entries from the list corresponding to divorced individuals
            // to ensure accurate charges for users.
            // Charging someone for something they no longer have is not appropriate
            relationships.removeIf(MarriageRecord::isDivorceQueued);

            if (relationships.isEmpty())
                return;

            // We must recalculate the cost, as the method removeMarriagesFromUser() was called,
            // removing all the necessary relationships in order to fit in the requester's balance
            int cost = relationships.size() * Marry.DAILY_COST;
            Query query = update(ECONOMY)
                    .set(ECONOMY.BALANCE, ECONOMY.BALANCE.minus(cost))
                    .set(ECONOMY.UPDATED_AT, Bot.unixNow())
                    .where(ECONOMY.USER_ID.eq(userId));

            trs.add(query);
        });

        try {
            trs.commit();
        } catch (DataAccessException e) {
            LOGGER.error("Could not charge users' marriages daily tax", e);
        }
    }

    /** This method will only divorce the necessary amount
     * for the requester to be able to afford it, not all of them.
     * <p>
     * This method respects the current order of the list, removing
     * from most recent to oldest relationships.
     *
     * @param balance       The balance of the requester.
     * @param relationships The requester's relationships.
     * @return The divorced relationships due to insufficient balance.
     */
    private static List<MarriageRecord> removeMarriagesFromUser(long balance, long userId, List<MarriageRecord> relationships, TransactionalService trs) {

        int cost = relationships.size() * Marry.DAILY_COST;
        List<MarriageRecord> removedMarriages = new ArrayList<>();

        while (balance < cost && !relationships.isEmpty()) {

            int lastMarriageIndex = relationships.size() - 1;
            MarriageRecord removedMarriage = relationships.remove(lastMarriageIndex);
            long targetRemoved = removedMarriage.getTargetId();
            // This is used for logging purposes, in order to consistenly point
            // who is the user with insufficient balance and their partner
            long userPartner = targetRemoved != userId ? targetRemoved : removedMarriage.getRequesterId();

            removedMarriages.add(removedMarriage);
            removedMarriage.divorce(trs);

            LOGGER.info("User {} does not have money to stay married to {}", userId, userPartner);
            cost -= Marry.DAILY_COST;
        }

        return Collections.unmodifiableList(removedMarriages);
    }

    private Map<Long, List<MarriageRecord>> fetchUsersMarriages() {

        Map<Long, List<MarriageRecord>> usersMarriages = new HashMap<>();
        DSLContext ctx = DBManager.getContext();
        List<Long> bypassUsers = ExclusionUtil.fetchExcluded(ExclusionType.MARRIAGE_FEE);

        List<MarriageRecord> marriages = ctx.select(MARRIAGES.ID, MARRIAGES.REQUESTER_ID, MARRIAGES.TARGET_ID, MARRIAGES.CREATED_AT)
                .from(MARRIAGES)
                .where(MARRIAGES.REQUESTER_ID.notIn(bypassUsers)
                        .and(MARRIAGES.TARGET_ID.notIn(bypassUsers))
        ).fetchInto(MARRIAGES);

        for (MarriageRecord m : marriages) {

            long requesterId = m.getRequesterId();
            long targetId = m.getTargetId();

            // Update requester's marriages
            List<MarriageRecord> requesterMarriages = usersMarriages.getOrDefault(requesterId, new ArrayList<>());

            requesterMarriages.add(m);
            usersMarriages.put(requesterId, requesterMarriages);

            // Update target's marriages
            List<MarriageRecord> targetMarriages = usersMarriages.getOrDefault(targetId, new ArrayList<>());

            targetMarriages.add(m);
            usersMarriages.put(targetId, targetMarriages);
        }

        // We must order the users' marriages by their creation,
        // in order to divorce accordingly when their balance is insufficient
        usersMarriages.values().forEach(this::orderMarriagesByCreation);

        return Collections.unmodifiableMap(usersMarriages);
    }

    private void orderMarriagesByCreation(List<MarriageRecord> marriages) {
        marriages.sort(Comparator.comparing(MarriageRecord::getCreated));
    }

    private String formatMarriages(List<MarriageRecord> marriages) {
        return Bot.format(marriages, mrg -> String.format("\n[%d -> %d]", mrg.getRequesterId(), mrg.getTargetId()));
    }
}