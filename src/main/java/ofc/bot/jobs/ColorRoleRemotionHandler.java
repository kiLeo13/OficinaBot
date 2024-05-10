package ofc.bot.jobs;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.Main;
import ofc.bot.util.content.annotations.jobs.CronJob;
import ofc.bot.databases.entities.records.ColorRoleRecord;
import ofc.bot.databases.DBManager;
import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static ofc.bot.databases.entities.tables.ColorRoles.COLOR_ROLES;
import static org.jooq.impl.DSL.*;

@CronJob(expression = "0 0 0 ? * * *") // Every day at midnight
public class ColorRoleRemotionHandler implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorRoleRemotionHandler.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        LOGGER.info("Checking for members with color role due to be removed...");

        List<ColorRoleRecord> toRemove = retrieveToRemove();
        JDA api = Main.getApi();

        if (toRemove.isEmpty()) {
            LOGGER.info("No members found.");
            return;
        }

        LOGGER.info("Found {} member{}.", toRemove.size(), toRemove.size() == 1 ? "" : "s");

        for (ColorRoleRecord data : toRemove) {

            long roleId = data.getRoleId();
            long userId = data.getUserId();
            long guildId = data.getGuildId();
            Guild guild = api.getGuildById(guildId);
            Role role = guild == null ? null : guild.getRoleById(roleId);

            if (guild == null) {
                LOGGER.warn("Could not find guild with id {}! Ignoring color role removal", roleId);
                continue;
            }

            if (role == null) {
                LOGGER.warn("Could not find role {}! Ignoring color role removal", roleId);
                continue;
            }

            guild.removeRoleFromMember(UserSnowflake.fromId(userId), role).queue((s) -> {
                LOGGER.info("Successfully removed color role {} from {}", roleId, userId);
                
                removeFrom(guildId, userId, roleId);

            }, (error) -> {

                if (error instanceof ErrorResponseException response && response.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                    removeFrom(guildId, userId, roleId);
                    LOGGER.warn("Member '{}' was not found, probably no longer present in the server, ignoring.", userId);
                } else {
                    LOGGER.error("Could not remove color role " + roleId + " from " + userId, error);
                }
            });
        }
    }

    private List<ColorRoleRecord> retrieveToRemove() {

        DSLContext ctx = DBManager.getContext();

        return ctx.selectFrom(COLOR_ROLES)
                .where(field("(julianday('now') - julianday(datetime(updated_at, 'unixepoch'))) >= 60", SQLDataType.BOOLEAN))
                .fetch();
    }

    private void removeFrom(long guildId, long userId, long roleId) {

        DSLContext ctx = DBManager.getContext();

        ctx.deleteFrom(COLOR_ROLES)
                .where(COLOR_ROLES.GUILD_ID.eq(guildId))
                .and(COLOR_ROLES.USER_ID.eq(userId))
                .and(COLOR_ROLES.ROLE_ID.eq(roleId))
                .execute();
    }
}