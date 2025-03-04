package ofc.bot.jobs.groups;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import ofc.bot.Main;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.RentStatus;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.domain.sqlite.repository.Repositories;
import ofc.bot.util.content.annotations.jobs.CronJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@CronJob(expression = "0 0 0 1 * ? *")
public class GroupsInvoiceHandler implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsInvoiceHandler.class);
    private final OficinaGroupRepository grpRepo;

    public GroupsInvoiceHandler() {
        this.grpRepo = Repositories.getOficinaGroupRepository();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Starting invoice operation");
        List<OficinaGroup> groups = grpRepo.findAllExcept(RentStatus.FREE, RentStatus.NOT_PAID);
        JDA api = Main.getApi();

        // Pretty printing ^^
        if (groups.size() == 1) {
            LOGGER.info("Found 1 group");
        } else {
            LOGGER.info("Found {} groups", groups.size());
        }
        if (groups.isEmpty()) return;

        for (OficinaGroup gp : groups) {
            if (gp.getRentStatus() == RentStatus.TRIAL) {
                LOGGER.info("Group \"{}\" ({}) is trial", gp.getName(), gp.getId());
                gp.setRentStatus(RentStatus.PAID).tickUpdate();
                grpRepo.upsert(gp);
                continue;
            }

            long roleId = gp.getRoleId();
            Role role = api.getRoleById(roleId);
            if (role == null) {
                LOGGER.info("Role \"{}\" for group {} not found", roleId, gp.getId());
                continue;
            }

            Guild guild = role.getGuild();
            // Yes, its usually discouraged to call get(), as it blocks the current thread,
            // but here we need operations to be sync.
            List<Member> members = guild.findMembersWithRoles(role).get();
            long rentValue = gp.calcRawRent(members);

            gp.setInvoiceAmount(rentValue)
                    .setRentStatus(RentStatus.PENDING)
                    .tickUpdate();
            grpRepo.upsert(gp);
        }
        LOGGER.info("Invoice operation completed!");
    }
}