package ofc.bot.jobs;

import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.domain.sqlite.repository.RepositoryFactory;
import ofc.bot.util.content.annotations.jobs.CronJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// This will be implemented in a future
@CronJob(expression = "0 0 14 10 * ? *")
public class GroupsRentCharger implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsRentCharger.class);
    private final OficinaGroupRepository grpRepo;

    // This property is 'true' when a charge operation is on-going,
    // to avoid hitting UnbelievaBoat's API rate-limit, the
    // ofc.bot.jobs.VoiceChatMoneyHandler will always wait for this value to become 'false'
    // in order to start updating users' cash
    private static boolean operating = false;

    public GroupsRentCharger() {
        this.grpRepo = RepositoryFactory.getOficinaGroupRepository();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Initializing groups rent charge process...");
        List<OficinaGroup> groups = grpRepo.findGroups(false);
        LOGGER.info("Found {} groups to be charged", groups.size());


    }

    public static boolean isOperating() {
        return operating;
    }
}