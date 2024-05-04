package ofc.bot.jobs;

import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.GroupRecord;
import ofc.bot.handlers.economy.Balance;
import ofc.bot.handlers.economy.UEconomyManager;
import org.jooq.DSLContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static ofc.bot.databases.entities.tables.Groups.GROUPS;

// This will be implemented in a future
// @CronJob(expression = "0 0 14 10 * ? *")
public class GroupsRentChargeHandler implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsRentChargeHandler.class);
    private static final long GUILD_ID = 582430782577049600L;

    // This property is 'true' when a charge operation is on-going,
    // to avoid hitting UnbelievaBoat's API rate-limit, the
    // ofc.bot.jobs.VoiceChatMoneyHandler will always wait for this value to become 'false'
    // in order to start updating users' cash
    private static boolean operating = false;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        LOGGER.info("Initializing groups rent charge process");

        List<GroupRecord> groups = fetchGroupsToCharge();

        LOGGER.info("Found {} groups to be charged", groups.size());

        if (groups.isEmpty())
            return;

        chargeGroups(groups, true);
    }

    private void chargeGroups(List<GroupRecord> groups, boolean shouldRetry) {

        for (GroupRecord group : groups) {

            long ownerId = group.getOwnerId();
            long roleId = group.getRoleId();
            Balance ownerBalance = UEconomyManager.getBalance(GUILD_ID, ownerId);

            // The API probably took too long to respond or the user was just not found at all
            // so we retry :)
            if (ownerBalance.getUserId() == null) {

                if (shouldRetry) {
                    LOGGER.warn("Balance of owner for ID '{}' of group role '{}' was not found! Retrying...", ownerId, roleId);
                    chargeGroups(groups, false);
                } else {
                    LOGGER.error("Balance of owner for ID '{}' of group role '{}' was not found! Aborting.", ownerId, roleId);
                }

                return;
            }
        }
    }

    private void charge(GroupRecord group) {

        long ownerId = group.getOwnerId();



    }

    private List<GroupRecord> fetchGroupsToCharge() {

        DSLContext ctx = DBManager.getContext();

        return ctx.selectFrom(GROUPS)
                .where(GROUPS.PRIVILEGED.ne(1))
                .fetch();
    }

    public static boolean isOperating() {
        return operating;
    }
}