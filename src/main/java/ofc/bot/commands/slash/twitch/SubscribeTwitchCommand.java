package ofc.bot.commands.twitch;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.Main;
import ofc.bot.domain.entity.TwitchSubscription;
import ofc.bot.domain.sqlite.repository.TwitchSubscriptionRepository;
import ofc.bot.handlers.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.commands.responses.states.InteractionResult;
import ofc.bot.handlers.commands.responses.states.Status;
import ofc.bot.handlers.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.twitch.TwitchService;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@DiscordCommand(name = "twitch subscribe")
public class SubscribeTwitchCommand extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeTwitchCommand.class);
    private final TwitchSubscriptionRepository twitchSubRepo;

    public SubscribeTwitchCommand(TwitchSubscriptionRepository twitchSubRepo) {
        this.twitchSubRepo = twitchSubRepo;
    }

    @Override
    public InteractionResult onCommand(@NotNull SlashCommandContext ctx) {
        String chanId = ctx.getSafeOption("channel-id", OptionMapping::getAsString);
        String dest = ctx.getSafeOption("destination", OptionMapping::getAsString);
        boolean broadcast = ctx.getOption("broadcast", true, OptionMapping::getAsBoolean);
        boolean isChannel = !dest.startsWith("https://");
        long userId = ctx.getUserId();
        Member issuer = ctx.getIssuer();
        TwitchService twitch = Main.getTwitch();
        TwitchSubscription sub = twitchSubRepo.findByChannelIdAndDestination(chanId, dest);

        // Yes, for this command, we check the permission at application level
        if (!issuer.hasPermission(Permission.ADMINISTRATOR))
            return Status.YOU_CANNOT_RUN_THIS_COMMAND;

        if (sub != null)
            return Status.TWITCH_USER_LIKELY_ALREADY_SUBSCRIBED;

        // This checks if the channel exists AND the issuer has permission to see it.
        if (isChannel && !destinationExists(issuer, dest))
            return Status.CHANNEL_NOT_FOUND;

        try {
            long now = Bot.unixNow();
            String subId = handleInternalSub(twitch, chanId);
            TwitchSubscription newSub = new TwitchSubscription(chanId, dest, broadcast, subId, userId, now, now);
            twitchSubRepo.save(newSub);

            return Status.SUBSCRIPTION_SUCCESSFULLY_SAVED;
        } catch (DataAccessException e) {
            LOGGER.error("Could not save Twitch subscription of channel '{}' to database", chanId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Inscreva um canal da Twitch para receber notificações de live.";
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "channel-id", "O ID do canal (use `/twitch channels` se não souber).", true)
                        .setMaxLength(20),
                new OptionData(OptionType.STRING, "destination", "Onde a live deverá ser anunciada.", true),
                new OptionData(OptionType.BOOLEAN, "broadcast", "Devemos marcar everyone ao notificar? (Padrão: True)")
        );
    }

    private String handleInternalSub(TwitchService twitch, String chanId) {
        // Twitch only requires a single subscription per channel, regardless of how many
        // users or destinations we are tracking. Whether 1 user or 1 decillion users subscribe
        // to the same channel, we only need to notify Twitch once.
        //
        // This logic ensures we avoid redundant API calls to Twitch:
        // If the channel has already been subscribed to, we assume
        // Twitch is already aware of it, so we don't re-send the subscription request.
        //
        // Instead, we simply register the new subscriber internally.
        //
        // If this is the first time we're seeing this channel, then we inform Twitch about it.
        return shouldFlush(chanId)
                ? twitch.subStreamOnline(chanId).getId()
                : twitchSubRepo.findAnyByChannelId(chanId).getSubscriptionId();
    }

    private boolean destinationExists(Member member, String id) {
        Guild guild = member.getGuild();
        GuildMessageChannel chan = guild.getChannelById(GuildMessageChannel.class, id);
        return chan != null && member.hasAccess(chan);
    }

    private boolean shouldFlush(String chanId) {
        return !twitchSubRepo.existsByChannelId(chanId);
    }
}