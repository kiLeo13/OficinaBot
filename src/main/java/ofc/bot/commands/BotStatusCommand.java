package ofc.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ofc.bot.Main;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

@DiscordCommand(name = "status")
public class BotStatusCommand extends SlashCommand {
    private static final long megaBytes = 1024 * 1024;

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        ctx.ack();

        JDA api = Main.getApi();
        Guild guild = ctx.getGuild();

        api.getRestPing().queue((apiPing) -> {
            String javaVersion = System.getProperty("java.version");
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long gatewayPing = api.getGatewayPing();
            long initTime = Main.getInitTime();
            int activeThreads = Thread.activeCount();

            MessageEmbed embed = embed(
                    guild,
                    javaVersion,
                    usedMemory / megaBytes,
                    apiPing,
                    gatewayPing,
                    initTime,
                    activeThreads
            );
            ctx.replyEmbeds(embed);
        });
        return Status.OK;
    }

    private MessageEmbed embed(Guild guild, String javaVersion, long usedMemoryMB, long apiPing, long gatewayPing, long initTime, int threadCount) {
        EmbedBuilder builder = new EmbedBuilder();

        String formattedPing = String.format("Gateway Ping: `%dms`.\nAPI Ping: `%dms`.", gatewayPing, apiPing);
        String threads = String.format("%02d", threadCount);
        String uptime = String.format("<t:%d>\n<t:%1$d:R>", initTime);
        int guildCount = Main.getApi().getGuilds().size();

        return builder
                .setTitle("Oficina's Status")
                .addField("📡 Response Time", formattedPing, true)
                .addField("🕒 Uptime", uptime, true)
                .addField("💻 Used Memory", usedMemoryMB + " MB", true)
                .addField("👥 Members Cached", Bot.fmtNum(guild.getMembers().size()), true)
                .addField("🌐 Guilds", Bot.fmtNum(guildCount), true)
                .addField("☕ Java Version", javaVersion, true)
                .addField("🤝 Active Threads", threads, true)
                .setColor(Bot.Colors.DISCORD)
                .build();
    }

    @Override
    protected void init() {
        setDesc("Veja os status do bot :)");
    }
}