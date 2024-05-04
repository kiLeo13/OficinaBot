package ofc.bot.jobs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.Main;
import ofc.bot.content.annotations.jobs.CronJob;
import ofc.bot.databases.entities.records.BirthdayRecord;
import ofc.bot.databases.DBManager;
import ofc.bot.util.content.Channels;
import ofc.bot.util.exclusions.ExclusionType;
import ofc.bot.util.exclusions.ExclusionUtil;
import org.jooq.DSLContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ofc.bot.databases.entities.tables.Birthdays.BIRTHDAYS;
import static org.jooq.impl.DSL.*;

@CronJob(expression = "0 0 0 ? * * *") // Every day at midnight
public class BirthdayReminder implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(BirthdayReminder.class);

    private static final String DEFAULT_MESSAGE = "@everyone BELA NOITE, vamos parabenizar esta pessoa tão especial chamada %s, por estar completando `%d` anos! Né? %s";
    private static final String AGELESS_MESSAGE = "@everyone BELA NOITE, vamos parabenizar esta pessoa tão especial chamada %s, por estar fazendo aniversário hoje! Né? %s";

    private static final long ROLE_ID_UNDERAGE = 664918505400958986L;
    private static final long ROLE_ID_ADULT = 664918505963126814L;

    @Override
    public void execute(JobExecutionContext context) {
        
        List<BirthdayRecord> birthdays = retrieveBirthdays();
        TextChannel channel = Main.getApi().getTextChannelById(Channels.E.id());

        if (channel == null) {
            LOGGER.warn("Could not find text channel! Ignoring birthday reminder");
            return;
        }

        if (birthdays.isEmpty())
            return;

        for (BirthdayRecord birthday : birthdays) {

            Guild guild = channel.getGuild();
            String name = birthday.getName();
            long userId = birthday.getUserId();
            int turnAge = resolveAge(birthday.getBirthday());

            guild.retrieveMemberById(userId).queue(m -> {

                String message = getMessage(m, name, turnAge);

                channel.sendMessage(message).queue();

                if (turnAge == 18)
                    updateAgeRole(channel, m);

            }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
        }
    }

    private String getMessage(Member member, String name, int turnAge) {

        long userId = member.getIdLong();
        boolean hideAge = ExclusionUtil.isExcluded(userId, ExclusionType.BIRTHDAY_AGE_DISPLAY);

        return hideAge
                ? String.format(AGELESS_MESSAGE, name, member.getAsMention())
                : String.format(DEFAULT_MESSAGE, name, turnAge, member.getAsMention());
    }

    private void updateAgeRole(TextChannel channel, Member member) {

        Guild guild = channel.getGuild();
        Role underageRole = guild.getRoleById(ROLE_ID_UNDERAGE);
        Role adultRole = guild.getRoleById(ROLE_ID_ADULT);

        // This must either work, or fail completely
        // by no means the bot should remove a role and not add the other one, or vice versa
        if (underageRole == null) {
            LOGGER.warn("Could not find Underage role for id {}", ROLE_ID_UNDERAGE);
            return;
        }

        if (adultRole == null) {
            LOGGER.warn("Could not find Adult role for id {}", ROLE_ID_ADULT);
            return;
        }

        guild.modifyMemberRoles(member, List.of(adultRole), List.of(underageRole)).queueAfter(5, TimeUnit.SECONDS, (v) -> {

            channel.sendMessageFormat("%s seu cargo foi atualizado para maior de idade! 🤨", member.getAsMention()).queue();
        }, (err) -> {
            LOGGER.error("Could not update roles of member '" + member.getId() + "'", err);
        });
    }

    private int resolveAge(LocalDate birth) {
        LocalDate now = LocalDate.now();
        return Period.between(birth, now).getYears();
    }

    private List<BirthdayRecord> retrieveBirthdays() {

        DSLContext ctx = DBManager.getContext();

        return ctx.selectFrom(BIRTHDAYS)
                .where(day(BIRTHDAYS.BIRTHDAY).eq(day(currentDate()))
                        .and(month(BIRTHDAYS.BIRTHDAY).eq(month(currentDate())))
                )
                .fetch();
    }
}