package ofc.bot.util.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import ofc.bot.commands.economy.LeaderboardCommand;
import ofc.bot.domain.entity.*;
import ofc.bot.domain.viewmodels.*;
import ofc.bot.util.Bot;

import java.awt.*;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;

/**
 * Utility class for embeds used in multiple classes.
 * <p>
 * If an embed is used in a single command, with no pagination or confirmation
 * system, then the {@code embed()} method will remain in the same class.
 */
public final class EmbedFactory {
    private static final Color MARRIAGE_EMBED_COLOR = new Color(255, 0, 127);

    private EmbedFactory() {}

    public static MessageEmbed embedBirthdayList(List<Birthday> birthdays, Guild guild, Month month) {
        EmbedBuilder builder = new EmbedBuilder();
        String monthName = getMonthDisplay(month);
        String formattedBirthdays = formatBirthdays(birthdays);
        boolean empty = birthdays.isEmpty();

        return builder
                .setAuthor("Aniversários", null, Birthday.ICON_URL)
                .setDescription("## " + monthName + "\n\n" + (empty ? "*Nenhum aniversariante.*" : formattedBirthdays))
                .setColor(Bot.Colors.DISCORD)
                .setFooter(guild.getName(), guild.getIconUrl())
                .build();
    }

    public static MessageEmbed embedLeaderboard(Guild guild, LeaderboardView leaderboard) {
        EmbedBuilder builder = new EmbedBuilder();

        int page = leaderboard.pageIndex() + 1;
        String pages = String.format("Pág %s/%s", Bot.fmtNum(page), Bot.fmtNum(leaderboard.maxPages()));
        List<LeaderboardUser> users = leaderboard.usersData();

        builder
                .setAuthor("Economy Leaderboard", null, UserEconomy.BANK_ICON)
                .setDescription("💸 Placar de Líderes Global.\n\n" + formatLeaderboardUsers(users, page))
                .setColor(Bot.Colors.DEFAULT)
                .setFooter(pages, guild.getIconUrl());

        return builder.build();
    }

    public static MessageEmbed embedMarriages(Guild guild, User target, MarriagesView data) {
        EmbedBuilder builder = new EmbedBuilder();

        int currentPage = data.page();
        int maxPages = data.maxPages();
        String name = target.getEffectiveName();
        String avatar = target.getEffectiveAvatarUrl();
        String strfPage = Bot.fmtNum(currentPage);
        String strfMaxPages = Bot.fmtNum(maxPages);

        return builder
                .setAuthor(name, null, avatar)
                .setDescription("Casamentos de `" + target.getEffectiveName() + "`.\n\n" + formatUsers(target.getIdLong(), data.marriages()))
                .setColor(MARRIAGE_EMBED_COLOR)
                .setFooter("Pág " + strfPage + "/" + strfMaxPages, guild.getIconUrl())
                .build();
    }

    private static String formatUsers(long issuerId, List<MarriageView> marriages) {
        StringBuilder builder = new StringBuilder();

        for (MarriageView mr : marriages) {
            AppUser selected = mr.partner(issuerId);
            String text = String.format("- %s (<t:%d>)\n", selected.getDisplayName(), mr.createdAt());
            builder.append(text);
        }
        return builder.toString().strip();
    }

    public static MessageEmbed embedUsernameUpdates(NamesHistoryView namesHistoryDTO, Guild guild, User target) {
        EmbedBuilder builder = new EmbedBuilder();

        List<UserNameUpdate> names = namesHistoryDTO.names();
        String name = target.getEffectiveName();
        String avatar = target.getEffectiveAvatarUrl();
        String page = Bot.fmtNum(namesHistoryDTO.page());
        String maxPages = Bot.fmtNum(namesHistoryDTO.maxPages());
        String results = Bot.fmtNum(namesHistoryDTO.total());
        String description = String.format("Resultados encontrados: `%s`.\n\n%s", results, formatUsernameUpdates(names));

        builder.setAuthor(name, null, avatar)
                .setDescription(description)
                .setColor(Color.CYAN)
                .setFooter(page + "/" + maxPages, guild.getIconUrl());

        return builder.build();
    }

    public static MessageEmbed embedProposals(Guild guild, User user, ProposalsView proposals) {
        EmbedBuilder builder = new EmbedBuilder();
        String type = proposals.type();
        String prettyType = "in".equals(type)
                ? "recebidas"
                : "enviadas";

        int page = proposals.page();
        int maxPages = proposals.maxPages();
        List<MarriageRequest> users = proposals.requests();
        String pages = String.format("Pág %s/%s", Bot.fmtNum(page), Bot.fmtNum(maxPages));
        String prettyCount = Bot.fmtNum(proposals.requestCount());
        String desc = String.format("Propostas de casamento **%s**: `%s`.\n\n%s", prettyType, prettyCount, formatProposals(users, type));

        return builder
                .setAuthor(user.getEffectiveName(), null, user.getEffectiveAvatarUrl())
                .setDescription(desc)
                .setColor(Bot.Colors.DEFAULT)
                .setFooter(pages, guild.getIconUrl())
                .build();
    }

    private static String formatProposals(List<MarriageRequest> requests, String type) {
        return Bot.format(requests, (req) -> {
            long timestamp = req.getTimeCreated();

            return switch (type) {
                case "in" -> String.format("- <@%d> (<t:%d:D>)\n", req.getRequesterId(), timestamp);
                case "out" -> String.format("- <@%d> (<t:%d:D>)\n", req.getTargetId(), timestamp);

                default -> throw new IllegalStateException("Unexpected value: " + type);
            };
        });
    }

    private static String formatUsernameUpdates(List<UserNameUpdate> names) {
        return Bot.format(names, (update) -> {
            String value = update.getNewValue();
            String name = value == null ? "*removed*" : escapeUsernameUpdateFormattings(value);
            long timestamp = update.getTimeCreated();

            return switch (update.getScope()) {
                case USERNAME, GLOBAL_NAME -> String.format("- <t:%d:d> %s\n", timestamp, name);

                case GUILD_NICK -> String.format("- <t:%d:d> %s <@%d>\n", timestamp, name, update.getAuthorId());
            };
        });
    }

    private static String escapeUsernameUpdateFormattings(String name) {
        return name
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("`", "\\`");
    }

    private static String formatBirthdays(List<Birthday> birthdays) {
        return Bot.format(
                birthdays,
                (b) -> String.format(Birthday.BIRTHDAYS_FORMAT, b.getPrettyBirthday(), b.getUserId())
        ).strip();
    }

    private static String formatLeaderboardUsers(List<LeaderboardUser> users, int page) {
        StringBuilder builder = new StringBuilder();
        int offset = (page - 1) * LeaderboardCommand.MAX_USERS_PER_PAGE;
        int pos = 1;

        for (LeaderboardUser er : users) {
            int itemPos = offset + pos++;
            String row = String.format(
                    UserEconomy.LEADERBOARD_ROW_FORMAT,
                    itemPos, er.displayIdentifier(), Bot.fmtNum(er.balance())
            );

            builder.append(row).append("\n");
        }

        return builder.toString().strip();
    }

    private static String getMonthDisplay(Month month) {
        String rawDisplay = month.getDisplayName(TextStyle.FULL, Bot.defaultLocale());
        return Bot.upperFirst(rawDisplay);
    }
}
