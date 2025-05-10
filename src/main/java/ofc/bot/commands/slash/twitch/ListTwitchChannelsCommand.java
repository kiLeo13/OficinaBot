package ofc.bot.commands.twitch;

import com.github.twitch4j.helix.domain.User;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.Main;
import ofc.bot.handlers.interactions.EntityContextFactory;
import ofc.bot.handlers.commands.Cooldown;
import ofc.bot.handlers.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.commands.responses.states.InteractionResult;
import ofc.bot.handlers.commands.responses.states.Status;
import ofc.bot.handlers.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.twitch.TwitchService;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

@DiscordCommand(name = "twitch channels")
public class ListTwitchChannelsCommand extends SlashSubcommand {

    @Override
    public InteractionResult onCommand(@NotNull SlashCommandContext ctx) {
        final int pageIndex = 0;
        String name = ctx.getSafeOption("name", OptionMapping::getAsString).toLowerCase();
        Guild guild = ctx.getGuild();
        TwitchService twitch = Main.getTwitch();
        List<User> users = twitch.retrieveUsers(name);

        if (users.isEmpty())
            return Status.TWITCH_NO_USERS_FOUND;

        List<Button> buttons = EntityContextFactory.createTwitchUsersButtons(users, pageIndex);
        MessageEmbed embed = EmbedFactory.embedTwitchUser(guild, users.getFirst());
        return ctx.create()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .send();
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Veja uma lista de canais pelo nome fornecido.";
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "name", "O nome do canal a ser pesquisado.", true)
        );
    }

    @NotNull
    @Override
    public Cooldown getCooldown() {
        return Cooldown.of(false, 3, TimeUnit.SECONDS);
    }
}