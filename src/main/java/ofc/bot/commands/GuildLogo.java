package ofc.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;

@DiscordCommand(name = "guild_avatar", description = "Envia a imagem atual do servidor.")
public class GuildLogo extends SlashCommand {

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Guild guild = ctx.getGuild();
        MessageEmbed embed = embed(guild);

        if (embed == null)
            return Status.GUILD_HAS_NO_ICON;

        ctx.replyEmbeds(embed);

        return Status.PASSED;
    }
    
    private MessageEmbed embed(Guild guild) {

        EmbedBuilder builder = new EmbedBuilder();
    
        String name = guild.getName();
        String url = guild.getIconUrl();
        String icon = url == null ? null : url + "?size=2048";
        String desc = guild.getDescription();

        if (url == null)
            return null;

        return builder
                .setTitle(name)
                .setDescription(desc)
                .setImage(icon)
                .setColor(Bot.Colors.DEFAULT)
                .setFooter(name, icon)
                .build();
    }
}