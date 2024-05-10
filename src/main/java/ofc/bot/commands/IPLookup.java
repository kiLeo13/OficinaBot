package ofc.bot.commands;

import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.handlers.requests.RequesterManager;

import java.awt.*;
import java.util.regex.Pattern;

@DiscordCommand(name = "ip", description = "Coleta informações aproximadas de um IP.", autoDefer = true)
public class IPLookup extends SlashCommand {
    private static final String REGEX = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
    private static final Color EMBED_COLOR = new Color(114, 222, 64);

    @Option(required = true)
    private static final OptionData IP = new OptionData(OptionType.STRING, "ip", "O IP a executar o lookup.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        String ipInput = ctx.getSafeOption("ip", OptionMapping::getAsString);
        Guild guild = ctx.getGuild();

        if (!Pattern.matches(REGEX, ipInput))
            return Status.INVALID_IP_ADDRESS_FORMAT.args(ipInput);

        IPData ipData = getIpData(ipInput);

        if (ipData == null)
            return Status.IP_NOT_FOUND;

        MessageEmbed embed = embed(guild, ipData);

        ctx.replyEmbeds(embed);

        return Status.PASSED;
    }

    private MessageEmbed embed(Guild guild, IPData ipData) {

        EmbedBuilder builder = new EmbedBuilder();

        builder
                .setTitle("🗺 IP Lookup")
                .setDescription("Mostrando informações do IP: `" + ipData.query + "`.")
                .setColor(EMBED_COLOR)
                .addField("🌎 País", ipData.country + " (" + ipData.countryCode + ")", true)
                .addField("📌 Região", ipData.regionName, true)
                .addField("🏙 Cidade", ipData.city, true)
                .addField("🕒 Timezone", ipData.timezone, true)
                .addField("📡 Provedor", ipData.isp, true)
                .setFooter(guild.getName(), guild.getIconUrl());

        if (ipData.zip != null && !ipData.zip.isBlank())
            builder.addField("📬 Código Postal", ipData.zip, true);

        return builder.build();
    }

    private IPData getIpData(String ip) {
        String response = RequesterManager.get("http://ip-api.com/json/" + ip, String::new);
        Gson gson = new Gson();
        IPData value = gson.fromJson(response, IPData.class);

        return value.status.equals("success")
                ? value
                : null;
    }

    private record IPData(
            String query,
            String status,
            String country,
            String countryCode,
            String regionName,
            String city,
            String timezone,
            String zip,
            String isp
    ) {}
}