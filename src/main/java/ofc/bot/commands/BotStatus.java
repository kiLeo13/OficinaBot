package ofc.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ofc.bot.Main;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;

import java.time.Instant;

@DiscordCommand(name = "status", description = "Veja os status do bot :)")
public class BotStatus extends SlashCommand {

    private static final long megaBytes = 1024 * 1024;

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        JDA api = Main.getApi();

        api.getRestPing().queue((apiPing) -> {

            String javaVersion = System.getProperty("java.version");
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long gatewayPing = api.getGatewayPing();
            long initTime = Instant.ofEpochMilli(Main.getInitTime()).getEpochSecond();
            int activeThreads = Thread.activeCount();

            MessageEmbed embed = embed(
                    javaVersion,
                    usedMemory / megaBytes,
                    apiPing,
                    gatewayPing,
                    initTime,
                    activeThreads
            );

            ctx.replyEmbeds(embed);
        });

        return Status.PASSED;
    }

    private MessageEmbed embed(String javaVersion, long usedMemoryMB, long apiPing, long gatewayPing, long initTime, int threadCount) {

        EmbedBuilder builder = new EmbedBuilder();

        String formattedPing = String.format("Gateway Ping: `%dms`.\nAPI Ping: `%dms`.", gatewayPing, apiPing);
        String threads = String.format("%02d", threadCount);
        String uptime = String.format("<t:%d>\n<t:%1$d:R>", initTime);

        builder
                .setTitle("Oficina's Status")
                .addField("📡 Response Time", formattedPing, true)
                .addField("🕒 Uptime", uptime, true)
                .addField("💻 Used Memory", usedMemoryMB + " MB", true)
                .addField("☕ Java Version", javaVersion, true)
                .addField("🤝 Active Threads", threads, true)
                .setColor(Bot.Colors.DISCORD);

        return builder.build();
    }
}