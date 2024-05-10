package ofc.bot.jobs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import ofc.bot.Main;
import ofc.bot.util.content.annotations.jobs.CronJob;
import ofc.bot.handlers.TemporalTaskExecutor;
import ofc.bot.handlers.economy.Balance;
import ofc.bot.handlers.economy.UEconomyManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

@CronJob(expression = "0 0/5 * ? * * *") // Every 5 minutes
public class VoiceChatMoneyHandler implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceChatMoneyHandler.class);
    private static final Random random = new Random();

    private static final long SALADA_VOICE_CHANNEL_ID = 693627612454453250L;
    private static final long GUILD_ID = 582430782577049600L;
    private static final int MIN_VALUE = 20;
    private static final int MAX_VALUE = 40;

    private static final Predicate<Member> CONDITION = (m) -> m.getVoiceState() != null
            && !m.getVoiceState().isMuted()
            && !m.getVoiceState().isDeafened();

    private static final List<String> NON_ELIGIBLE_CATEGORIES = List.of(
            "587036164926734337",
            "691194660902928435",
            "664972695129030656"
    );

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        Guild oficina = Main.getApi().getGuildById(GUILD_ID);

        if (oficina == null) {
            LOGGER.warn("Guild for id {} was not found", GUILD_ID);
            return;
        }

        List<Member> membersToPay = getEligibleMembers(oficina);
        TemporalTaskExecutor executor = new TemporalTaskExecutor(1000); // 1 request per second
        long guildId = oficina.getIdLong();
        int totalGiven = 0;

        if (membersToPay.isEmpty()) {
            LOGGER.info("No members found.");
            return;
        }

        for (Member member : membersToPay) {

            int randomValue = random.nextInt(MIN_VALUE, MAX_VALUE + 1);
            long userId = member.getIdLong();
            long currentVoiceChannelId = member.getVoiceState().getChannel().getIdLong();
            boolean isSalada = currentVoiceChannelId == SALADA_VOICE_CHANNEL_ID;

            int amount = isSalada ? randomValue * 2 : randomValue;

            executor.addTask(() -> {

                final long CASH_DEFAULT = 0;
                final long BANK_DEFAULT = 0;

                // Members in the "Salada" voice channel
                // will earn 2x more and be credited in their bank, instead of cash for normal voice channels
                long cash = isSalada ? CASH_DEFAULT : amount;
                long bank = isSalada ? amount : BANK_DEFAULT;

                Balance balance = UEconomyManager.updateBalance(guildId, userId, cash, bank, "VoiceChat money");

                if (balance == null)
                    LOGGER.warn("Failed to give money to user '{}'", userId);
            });

            totalGiven += amount;
        }

        LOGGER.info("A total of ${} was given to {} different members at {}", String.format("%02d", totalGiven), membersToPay.size(), oficina.getName());
        executor.run();
    }

    private List<Member> getEligibleMembers(Guild guild) {

        List<VoiceChannel> voiceChannels = getEligibleVoiceChannels(guild);
        return voiceChannels.stream()
                .filter(this::hasEnoughMembers)
                .flatMap(vc -> vc.getMembers().stream())
                .filter(CONDITION)
                .filter(m -> !m.getUser().isBot())
                .toList();
    }

    private boolean hasEnoughMembers(VoiceChannel vc) {

        List<Member> undeafenedMembers = vc.getMembers()
                .stream()
                .filter(m -> !m.getUser().isBot())
                .filter(m -> m.getVoiceState() != null && !m.getVoiceState().isDeafened())
                .toList();

        return undeafenedMembers.size() >= 2;
    }

    private List<VoiceChannel> getEligibleVoiceChannels(Guild guild) {

        return guild.getVoiceChannels()
                .stream()
                .filter(this::isEligible)
                .toList();
    }

    private boolean isEligible(VoiceChannel vc) {

        Category parentCategory = vc.getParentCategory();

        return parentCategory != null && !NON_ELIGIBLE_CATEGORIES.contains(parentCategory.getId());
    }
}