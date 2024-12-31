package ofc.bot.jobs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import ofc.bot.Main;
import ofc.bot.handlers.TemporalTaskExecutor;
import ofc.bot.handlers.economy.BankAccount;
import ofc.bot.handlers.economy.PaymentManagerProvider;
import ofc.bot.handlers.economy.unb.UnbelievaBoatClient;
import ofc.bot.util.content.annotations.jobs.CronJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

@CronJob(expression = "0 0/5 * ? * * *") // Every 5 minutes
public class VoiceChatMoneyHandler implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceChatMoneyHandler.class);
    private static final Random random = new Random();
    private static final List<Long> SPECIAL_CHANNEL_IDS = List.of(1065077982588305538L, 693627612454453250L);
    private static final int MIN_VALUE = 20;
    private static final int MAX_VALUE = 40;
    private final UnbelievaBoatClient paymentManager = PaymentManagerProvider.getUnbelievaBoatClient();

    private static final Predicate<Member> CONDITION = (m) -> m.getVoiceState() != null
            && !m.getVoiceState().isMuted()
            && !m.getVoiceState().isDeafened();

    private static final List<String> NON_ELIGIBLE_CATEGORIES = List.of(
            "587036164926734337",
            "691194660902928435",
            "664972695129030656"
    );

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void execute(JobExecutionContext context) throws JobExecutionException {
        List<Guild> guilds = Main.getApi().getGuilds();

        List<Member> membersToPay = getEligibleMembers(guilds);
        TemporalTaskExecutor executor = new TemporalTaskExecutor(1000); // 1 request per second
        int totalGiven = 0;

        if (membersToPay.isEmpty()) return;

        for (Member member : membersToPay) {
            Guild guild = member.getGuild();
            int randomValue = random.nextInt(MIN_VALUE, MAX_VALUE + 1);
            long userId = member.getIdLong();
            long currentVoiceChannelId = member.getVoiceState().getChannel().getIdLong();
            long guildId = guild.getIdLong();
            boolean isSpecial = SPECIAL_CHANNEL_IDS.contains(currentVoiceChannelId);

            int amount = isSpecial ? randomValue * 2 : randomValue;
            executor.addTask(() -> {
                // "Special" cases are members in the Salada voice channel, which
                // earn 2x the amount of money and get credited in their bank,
                // instead of cash for normal voice channels
                long cash = isSpecial ? 0 : amount;
                long bank = isSpecial ? amount : 0;

                BankAccount balance = paymentManager.update(
                        guildId, userId, cash, bank, "VoiceChat money"
                );

                if (balance == null)
                    LOGGER.warn("Failed to give money to user '{}'", userId);
            });
            totalGiven += amount;
        }
        LOGGER.info("A total of ${} was given to {} different members at in {} different guilds",
                String.format("%02d", totalGiven), membersToPay.size(), guilds.size()
        );
        executor.run();
    }

    private List<Member> getEligibleMembers(List<Guild> guilds) {
        List<Member> members = new ArrayList<>();
        for (Guild guild : guilds) {
            members.addAll(getEligibleMembers(guild));
        }
        return members;
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