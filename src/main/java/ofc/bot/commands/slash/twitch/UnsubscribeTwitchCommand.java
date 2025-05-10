package ofc.bot.commands.twitch;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
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
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@DiscordCommand(name = "twitch unsubscribe")
public class UnsubscribeTwitchCommand extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnsubscribeTwitchCommand.class);
    private final TwitchSubscriptionRepository twitchSubRepo;

    public UnsubscribeTwitchCommand(TwitchSubscriptionRepository twitchSubRepo) {
        this.twitchSubRepo = twitchSubRepo;
    }

    @Override
    public InteractionResult onCommand(@NotNull SlashCommandContext ctx) {
        String dest = ctx.getSafeOption("destination", OptionMapping::getAsString);
        String chanId = ctx.getSafeOption("channel", OptionMapping::getAsString);
        Member issuer = ctx.getIssuer();
        TwitchService twitch = Main.getTwitch();
        TwitchSubscription sub = twitchSubRepo.findByChannelIdAndDestination(chanId, dest);

        if (!issuer.hasPermission(Permission.ADMINISTRATOR))
            return Status.YOU_CANNOT_RUN_THIS_COMMAND;

        if (sub == null)
            return Status.TWITCH_USER_NOT_SUBSCRIBED;

        try {
            twitchSubRepo.delete(sub);

            if (shouldFlush(chanId)) {
                twitch.ubsubStreamOnline(sub);
            }

            return Status.SUBSCRIPTION_SUCCESSFULLY_DELETED;
        } catch (DataAccessException e) {
            LOGGER.error("Failed to delete Twitch subscription of channel '{}'", chanId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Desinscreva de um canal da Twitch para não receber mais notificações de live.";
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "destination", "Onde a live está sendo anunciada.", true),
                new OptionData(OptionType.STRING, "channel", "O canal a se desinscrever.", true, true)
        );
    }

    private boolean shouldFlush(String chanId) {
        return !twitchSubRepo.existsByChannelId(chanId);
    }
}