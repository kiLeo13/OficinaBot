package ofc.bot.jobs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.domain.entity.Birthday;
import ofc.bot.domain.entity.enums.ExclusionType;
import ofc.bot.domain.sqlite.repository.BirthdayRepository;
import ofc.bot.domain.sqlite.repository.RepositoryFactory;
import ofc.bot.domain.sqlite.repository.UserExclusionRepository;
import ofc.bot.util.content.Channels;
import ofc.bot.util.content.annotations.jobs.CronJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@CronJob(expression = "0 0 0 ? * * *") // Every day at midnight
public class BirthdayReminder implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(BirthdayReminder.class);
    private static final String DEFAULT_MESSAGE = "@everyone BELA NOITE, vamos parabenizar esta pessoa tão especial chamada %s, por estar completando `%d` anos! Né? %s";
    private static final String AGELESS_MESSAGE = "@everyone BELA NOITE, vamos parabenizar esta pessoa tão especial chamada %s, por estar fazendo aniversário hoje! Né? %s";
    private static final long ROLE_ID_UNDERAGE = 664918505400958986L;
    private static final long ROLE_ID_ADULT = 664918505963126814L;
    private final UserExclusionRepository exclRepo;
    private final BirthdayRepository bdayRepo;

    public BirthdayReminder() {
        this.exclRepo = RepositoryFactory.getUserExclusionRepository();
        this.bdayRepo = RepositoryFactory.getBirthdayRepository();
    }

    @Override
    public void execute(JobExecutionContext context) {
        List<Birthday> birthdays = bdayRepo.findByCurrentMonth();
        TextChannel channel = Channels.E.textChannel();

        if (channel == null) {
            LOGGER.warn("Could not find text channel! Ignoring birthday reminder");
            return;
        }

        if (birthdays.isEmpty()) return;

        for (Birthday birthday : birthdays) {
            Guild guild = channel.getGuild();
            String name = birthday.getName();
            long userId = birthday.getUserId();
            int turnAge = resolveAge(birthday.getBirthday());

            guild.retrieveMemberById(userId).queue(m -> {
                if (!m.hasAccess(channel)) return;

                String message = getMessage(m, name, turnAge);
                channel.sendMessage(message).queue();

                if (turnAge == 18) updateAgeRole(channel, m);

            }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
        }
    }

    private String getMessage(Member member, String name, int turnAge) {
        long userId = member.getIdLong();
        boolean hideAge = exclRepo.existsByTypeAndUserId(ExclusionType.BIRTHDAY_AGE_DISPLAY, userId);

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

        guild.modifyMemberRoles(member, List.of(adultRole), List.of(underageRole)).queue((v) -> {
            channel.sendMessageFormat("%s seu cargo foi atualizado para maior de idade! 🤨", member.getAsMention()).queue();
        }, (err) -> {
            LOGGER.error("Could not update roles of member '{}'", member.getId(), err);
        });
    }

    private int resolveAge(LocalDate birth) {
        LocalDate now = LocalDate.now();
        return Period.between(birth, now).getYears();
    }
}