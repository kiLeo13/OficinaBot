package ofc.bot.jobs;

import net.dv8tion.jda.api.JDA;
import ofc.bot.Main;
import ofc.bot.util.content.annotations.jobs.CronJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@CronJob(expression = "0 40 22 ? * * *")
public class ToddyMedicineReminder implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JDA api = Main.getApi();

        api.openPrivateChannelById("962903960493101056").queue(dm -> {
            dm.sendMessage("O seu vagabundo, o remédio num entra sozinho na boca não").queue();
        });
    }
}