package ofc.bot.jobs.roles;

import ofc.bot.databases.DBManager;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.jobs.CronJob;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static ofc.bot.databases.entities.tables.FormerMembersRoles.FORMER_MEMBERS_ROLES;

/**
 * This class is responsible for removing backups older than 90 days
 * from the database (table {@link ofc.bot.databases.entities.tables.FormerMembersRoles FormerMembersRoles}) based on the
 * {@link ofc.bot.databases.entities.tables.FormerMembersRoles#CREATED_AT created_at} column.
 */
@CronJob(expression = "0 0 0 ? * * *") // Every day at midnight
public class ExpiredBackupsRemover implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredBackupsRemover.class);
    private static final long EXPIRY_DAYS = TimeUnit.DAYS.toSeconds(90);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        LOGGER.info("Removing expired backups (older than {} days)...", TimeUnit.SECONDS.toDays(EXPIRY_DAYS));

        try {
            int deleted = deleteExpiredBackups();

            if (deleted == 0)
                LOGGER.info("No backups were deleted");
            else
                LOGGER.info("Successfully deleted a total of {} backed-up roles", deleted);
        } catch (DataAccessException e) {
            LOGGER.error("Could not delete expired backups", e);
        }
    }

    private int deleteExpiredBackups() {

        DSLContext ctx = DBManager.getContext();
        long now = Bot.unixNow();
        long maxAge = now - EXPIRY_DAYS;

        return ctx.deleteFrom(FORMER_MEMBERS_ROLES)
                .where(FORMER_MEMBERS_ROLES.CREATED_AT.lessThan(maxAge))
                .and(FORMER_MEMBERS_ROLES.PRIVILEGED.eq(0))
                .execute();
    }
}