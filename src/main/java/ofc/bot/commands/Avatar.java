package ofc.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;

import java.awt.*;

@DiscordCommand(name = "avatar", description = "Mostra o avatar de um usuário.")
public class Avatar extends SlashCommand {

    @Option
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "O usuário a verificar o avatar.");

    @Option
    private static final OptionData IS_GUILD = new OptionData(OptionType.BOOLEAN, "from_server", "Se o avatar mostrado deve ser o específico do servidor atual (Padrão: False).");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        MessageEmbed embed;
        Member issuer = ctx.getIssuer();
        User user = ctx.getUser();
        Guild guild = ctx.getGuild();
        boolean fromGuild = ctx.getOption("from_server", false, OptionMapping::getAsBoolean);
        boolean targetProvided = ctx.hasOption("user");

        if (fromGuild) {
            Member targetInput = ctx.getOption("user", OptionMapping::getAsMember);
            Member target = targetProvided ? targetInput : issuer;

            if (target == null)
                return Status.MEMBER_NOT_IN_GUILD;

            String url = target.getAvatarUrl();

            if (url == null)
                return Status.NO_GUILD_AVATAR_PRESENT;

            embed = embed(url, guild, target.getUser());
        } else {
            User target = ctx.getOption("user", user, OptionMapping::getAsUser);
            String url = target.getEffectiveAvatarUrl();

            embed = embed(url, guild, target);
        }

        ctx.replyEmbeds(embed);

        return Status.PASSED;
    }

    private MessageEmbed embed(String url, Guild guild, User target) {

        EmbedBuilder builder = new EmbedBuilder();
        String name = target.getName();
        String resizedUrl = url.contains("size") ? url : url + "?size=2048";
        String title = "🖼 " + name;
        Color color = Bot.Colors.DISCORD;

        return builder
                .setTitle(title, url)
                .setDescription(String.format("Avatar de `%s`", target.getEffectiveName()))
                .setColor(color)
                .setImage(resizedUrl)
                .setFooter(guild.getName(), guild.getIconUrl())
                .build();
    }
}