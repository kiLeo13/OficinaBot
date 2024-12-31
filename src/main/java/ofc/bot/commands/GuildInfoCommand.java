package ofc.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.awt.*;
import java.util.List;

@DiscordCommand(name = "serverinfo", description = "Informações gerais sobre o servidor.")
public class GuildInfoCommand extends SlashCommand {

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Guild guild = ctx.getGuild();
        MessageEmbed embed = embed(guild);

        return ctx.create()
                .setEmbeds(embed)
                .send();
    }

    private MessageEmbed embed(Guild guild) {
        EmbedBuilder builder = new EmbedBuilder();

        long timeCreated = guild.getTimeCreated().toEpochSecond();
        String creation = String.format("<t:%d>\n<t:%1$d:R>", timeCreated);
        Member owner = guild.retrieveOwner().complete();
        String ownerName = owner == null ? "Not found" : owner.getEffectiveName();
        String banner = guild.getBannerUrl() == null ? "" : guild.getBannerUrl() + "?size=2048";

        List<GuildChannel> channels = guild.getChannels(true);
        List<TextChannel> textChannels = guild.getTextChannels();
        List<VoiceChannel> audioChannels = guild.getVoiceChannels();
        List<Category> categories = guild.getCategories();
        List<ForumChannel> forums = guild.getForumChannels();
        List<ThreadChannel> threads = guild.getThreadChannels();

        return builder
                .setTitle("<a:M_Myuu:643942157325041668> " + guild.getName())
                .setThumbnail(guild.getIconUrl())
                .setColor(new Color(193, 126, 142))
                .addField("🌐 Server ID", "`" + guild.getOwnerIdLong() + "`", true)
                .addField("📅 Criação", creation, true)
                .addField("👑 Dono", "`" + ownerName + "`", true)
                .addField("💬 Canais (e Categorias) (" + channels.size() + ")", String.format("""
                        🔉 Áudio: `%d`
                        ⚽ Categorias: `%d`
                        💭 Fóruns: `%s`
                        📝 Texto: `%d`
                        🎈 Threads: `%d`
                        """,
                        audioChannels.size(),
                        categories.size(),
                        forums.size(),
                        textChannels.size(),
                        threads.size()
                ), true)
                .addField("👥 Membros (" + guild.getMemberCount() + ")", "", false)
                .setImage(banner)
                .setFooter(guild.getName(), guild.getIconUrl())
                .build();
    }
}