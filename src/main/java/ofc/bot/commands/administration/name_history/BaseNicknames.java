package ofc.bot.commands.administration.name_history;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.databases.entities.INameChangeLog;
import ofc.bot.handlers.buttons.ButtonManager;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.CommandPermission;
import ofc.bot.util.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.NameLogUtil;

import java.awt.*;
import java.util.List;
import java.util.UUID;

@DiscordCommand(name = "names", description = "Veja o histórico de apelidos de um usuário.")
@CommandPermission(Permission.MANAGE_SERVER)
public class BaseNicknames extends SlashCommand {

    @Option(required = true)
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "O usuário a procurar pelo histórico de apelidos.");

    @Option(required = true)
    private static final OptionData TYPE = new OptionData(OptionType.STRING, "type", "O tipo de nome a ser recuperado.")
            .addChoice("Guild Nick", "nick")
            .addChoice("Global Name", "global")
            .addChoice("Name", "name");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User sender = ctx.getUser();
        String type = ctx.getSafeOption("type", OptionMapping::getAsString);
        User target = ctx.getOption("user", sender, OptionMapping::getAsUser);
        Guild guild = ctx.getGuild();
        long targetId = target.getIdLong();
        NamesHistoryData namesHistoryData = NameLogUtil.retrieveNamesOfUser(type, targetId, 0);

        if (namesHistoryData.isEmpty())
            return Status.NO_NAME_HISTORY_FOR_USER.args(target.getName());

        boolean hasMorePages = namesHistoryData.page() < namesHistoryData.maxPages();
        Button[] buttons = generateButtons(type, targetId, 0, hasMorePages);
        MessageEmbed embed = embed(namesHistoryData, guild, target);

        ctx.reply()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .send();

        return Status.PASSED;
    }

    public static Button[] generateButtons(String type, long targetId, int currentOffset, boolean hasNext) {

        int previousOffset = currentOffset - 10;
        int nextOffset = currentOffset + 10;
        boolean hasPrevious = currentOffset >= 10;

        String previousID = UUID.randomUUID().toString();
        String nextID = UUID.randomUUID().toString();
        Button previous = Button.primary(previousID, "◀")
                .withDisabled(!hasPrevious);
        Button next = Button.primary(nextID, "▶")
                .withDisabled(!hasNext);

        ButtonManager.create(previousID)
                .setValueInt(previousOffset)
                .setIdentity("names")
                .setPayload(type)
                .setPermission(Permission.MANAGE_SERVER)
                .setEntity(targetId)
                .insert();

        ButtonManager.create(nextID)
                .setValueInt(nextOffset)
                .setIdentity("names")
                .setPayload(type)
                .setPermission(Permission.MANAGE_SERVER)
                .setEntity(targetId)
                .insert();

        return new Button[]{ previous, next };
    }

    public static MessageEmbed embed(NamesHistoryData namesHistoryData, Guild guild, User target) {

        EmbedBuilder builder = new EmbedBuilder();

        List<? extends INameChangeLog> names = namesHistoryData.names();
        String name = target.getEffectiveName();
        String avatar = target.getEffectiveAvatarUrl();
        String page = Bot.strfNumber(namesHistoryData.page());
        String maxPages = Bot.strfNumber(namesHistoryData.maxPages());
        String results = Bot.strfNumber(namesHistoryData.rowsCount());
        String description = String.format("Resultados encontrados: `%s`.\n\n%s", results, formatUsers(names));

        builder.setAuthor(name, null, avatar)
                .setDescription(description)
                .setColor(Color.CYAN)
                .setFooter(page + "/" + maxPages, guild.getIconUrl());

        return builder.build();
    }

    private static String formatUsers(List<? extends INameChangeLog> names) {

        return Bot.format(names, (update) -> {

            String value = update.getNewValue();
            String name = value == null ? "*removed*" : replaceFormattings(value);
            long timestamp = update.getTimestamp();

            return switch (update.getContext()) {

                case NAME, GLOBAL_NAME -> String.format("- <t:%d:d> %s\n", timestamp, name);

                case NICK -> String.format("- <t:%d:d> %s <@%d>\n", timestamp, name, update.getModeratorId());
            };
        });
    }

    private static String replaceFormattings(String name) {

        return name
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("`", "\\`");
    }
}