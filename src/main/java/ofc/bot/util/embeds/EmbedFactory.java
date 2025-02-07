package ofc.bot.util.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import ofc.bot.commands.economy.LeaderboardCommand;
import ofc.bot.domain.entity.*;
import ofc.bot.domain.entity.enums.GroupPermission;
import ofc.bot.domain.viewmodels.*;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.handlers.paginations.PaginationItem;
import ofc.bot.util.Bot;

import java.awt.*;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for embeds used in multiple classes.
 * <p>
 * If an embed is used in a single command, with no pagination or confirmation
 * system, then the {@code embed()} method will remain in the same class.
 */
public final class EmbedFactory {
    private static final Color DANGER_RED = new Color(255, 50, 50);

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

    public static MessageEmbed embedInfractions(
            User user, Guild guild, PaginationItem<MemberPunishment> infrs, long moderatorId
    ) {
        EmbedBuilder builder = new EmbedBuilder();
        MemberPunishment infr = infrs.get(0);
        boolean active = infr.isActive();
        String modMention = String.format("<@%d>", moderatorId);
        String infrCreation = String.format("<t:%d>", infr.getTimeCreated());
        String resultsFound = String.format("Resultados encontrados: `%s`.", Bot.fmtNum(infrs.getRowCount()));
        String pages = String.format("%s/%s", Bot.fmtNum(infrs.getPageIndex() + 1), Bot.fmtNum(infrs.getPageCount()));
        String delAuthorMention = Bot.ifNull(infr.getDeletionAuthorMention(), "Ninguém");

        return builder
                .setAuthor(user.getEffectiveName(), null, user.getAvatarUrl())
                .setColor(Bot.Colors.DEFAULT)
                .setDescription(resultsFound)
                .addField("👑 Moderador", modMention, true)
                .addField("📅 Punido em", infrCreation, true)
                .addField("📌 Ativo", active ? "Sim" : "Não", true)
                .addField("🚫 Removido por", delAuthorMention, true)
                .addField("📖 Motivo", infr.getReason(), false)
                .setFooter(pages, guild.getIconUrl())
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

    public static MessageEmbed embedLevels(Guild guild, LevelView user, PaginationItem<LevelView> levels) {
        EmbedBuilder builder = new EmbedBuilder();

        int page = levels.getPage();
        String fmtPages = String.format("Pág %s/%s", Bot.fmtNum(page), Bot.fmtNum(levels.getPageCount()));
        String userRow = String.format(UserXP.LEADERBOARD_ROW_FORMAT, user.rank(), user.displayIdentifier(), Bot.humanizeNum(user.level()));
        
        return builder
                .setAuthor("Levels Leaderboard - Global", null, guild.getIconUrl())
                .setDescription("📊 Placar global de níveis.\n\n" + formatLevelUsers(levels))
                .appendDescription("\n\n")
                .appendDescription(userRow)
                .setColor(Bot.Colors.DEFAULT)
                .setFooter(fmtPages)
                .build();
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

    public static MessageEmbed embedGroupChannelCreate(
            Member buyer, OficinaGroup group, ChannelType type, int price
    ) {
        return embedGroupPurchaseConfirmation(
                buyer,
                group,
                buyer.getUser().getEffectiveAvatarUrl(),
                null,
                "Deseja confirmar a compra deste canal?",
                price,
                Map.of("📚 Tipo", Bot.upperFirst(type.name().toLowerCase()))
        );
    }

    public static MessageEmbed embedInvoicePayment(Member buyer, OficinaGroup group, int amount) {
        return embedGroupPurchaseConfirmation(
                buyer,
                group,
                buyer.getUser().getEffectiveAvatarUrl(),
                null,
                "Deseja confirmar o pagamento da fatura?",
                amount,
                Bot.map()
        );
    }

    public static MessageEmbed embedGroupPermissionAdd(
            Member buyer, OficinaGroup group, GroupPermission perm, int amount
    ) {
        return embedGroupPurchaseConfirmation(
                buyer,
                group,
                buyer.getUser().getEffectiveAvatarUrl(),
                null,
                "Deseja confirmar a adição desta permissão?",
                amount,
                Bot.map("\uD83D\uDC6E Permissão", perm.getDisplay())
        );
    }

    public static MessageEmbed embedGroupMemberAdd(Member buyer, OficinaGroup group, Member newMember, int price) {
        return embedGroupPurchaseConfirmation(
                buyer,
                group,
                newMember.getUser().getEffectiveAvatarUrl(),
                null,
                "Deseja confirmar a adição deste membro?",
                price,
                Map.of("👤 Membro", newMember.getAsMention())
        );
    }

    public static MessageEmbed embedGroupMemberRemove(Member buyer, OficinaGroup group, Member member) {
        return embedGroupPurchaseConfirmation(
                buyer,
                group,
                member.getUser().getEffectiveAvatarUrl(),
                null,
                "Deseja confirmar a remoção deste membro?",
                0,
                Map.of("👤 Membro", member.getAsMention())
        );
    }

    public static MessageEmbed embedGroupCreate(Member buyer, OficinaGroup group, int color) {
        return embedGroupPurchaseConfirmation(
                buyer,
                group,
                buyer.getUser().getEffectiveAvatarUrl(),
                color,
                "Deseja confirmar a compra deste grupo?",
                group.getAmountPaid(),
                Map.of("🎨 Cor", Bot.fmtColorHex(color))
        );
    }

    public static MessageEmbed embedGroupDelete(Member owner, OficinaGroup group, int refund) {
        return embedGroupSellConfirmation(
                owner,
                group,
                owner.getUser().getEffectiveAvatarUrl(),
                DANGER_RED.getRGB(),
                "Deseja confirmar a deleção deste grupo?",
                refund,
                Map.of()
        );
    }

    public static MessageEmbed embedGroupMessagePin(Member buyer, OficinaGroup group, String messageUrl, int price) {
        return embedGroupPurchaseConfirmation(
                buyer,
                group,
                buyer.getUser().getEffectiveAvatarUrl(),
                null,
                "Deseja fixar esta mensagem?",
                price,
                Bot.map("📖 Mensagem", messageUrl)
        );
    }

    public static MessageEmbed embedGroupMessageUnpin(Member owner, OficinaGroup group, String messageUrl) {
        return embedGroupSellConfirmation(
                owner,
                group,
                owner.getUser().getEffectiveAvatarUrl(),
                DANGER_RED.getRGB(),
                "Deseja desfixar esta mensagem?",
                0,
                Bot.map("📖 Mensagem", messageUrl)
        );
    }

    public static MessageEmbed embedGroupBotAdd(Member buyer, OficinaGroup group, GroupBot bot, int price) {
        return embedGroupPurchaseConfirmation(
                buyer,
                group,
                buyer.getUser().getEffectiveAvatarUrl(),
                null,
                "Deseja confirmar a adição deste bot?",
                price,
                Map.of("🤖 Bot", bot.getBotMention())
        );
    }

    public static MessageEmbed embedGroupModify(
            Member buyer, OficinaGroup group, String newName, int newColor, int price
    ) {
        StringBuilder itemsList = new StringBuilder();
        if (newName != null) itemsList.append("Nome").append("\n");
        if (newColor != -1) itemsList.append("Cor");
        return embedGroupPurchaseConfirmation(
                buyer,
                group,
                buyer.getUser().getEffectiveAvatarUrl(),
                null,
                "Deseja confirmar a modificação deste grupo?",
                price,
                Map.of("🎈 Modificações", itemsList.toString())
        );
    }

    private static MessageEmbed embedGroupSellConfirmation(
            Member member, OficinaGroup group, String thumbUrl,
            Integer color, String act, int refund, Map<String, Object> fields
    ) {
        int embedColor = color == null ? group.resolveColor() : color;
        Map<String, Object> fieldsMap = new LinkedHashMap<>();
        fieldsMap.put("💰 Reembolso", Bot.fmtMoney(refund));
        fieldsMap.putAll(fields);
        return embedGroupConfirmation(
                member, group, thumbUrl, act, embedColor, fieldsMap
        );
    }

    private static MessageEmbed embedGroupPurchaseConfirmation(
            Member buyer, OficinaGroup group, String thumbUrl,
            Integer color, String act, int price, Map<String, Object> fields
    ) {
        int embedColor = color == null ? group.resolveColor() : color;
        Map<String, Object> fieldsMap = new LinkedHashMap<>();
        fieldsMap.put("💰 Valor", Bot.fmtMoney(price));
        fieldsMap.putAll(fields);
        return embedGroupConfirmation(
                buyer, group, thumbUrl, act, embedColor, fieldsMap
        );
    }

    private static MessageEmbed embedGroupConfirmation(
            Member member, OficinaGroup group, String thumbUrl, String act,
            int color, Map<String, Object> fields
    ) {
        EmbedBuilder builder = new EmbedBuilder();
        Guild guild = member.getGuild();
        CurrencyType currency = group.getCurrency();
        String guildName = guild.getName();
        String groupName = group.getName();

        builder
                .setTitle(groupName)
                .setDescription(act)
                .setThumbnail(thumbUrl)
                .setColor(color)
                .setFooter(guildName, guild.getIconUrl())
                .addField("💳 Economia", currency.getName(), true);

        fields.forEach((k, v) -> builder.addField(k, v.toString(), true));
        return builder.build();
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
            String name = value == null ? "*removed*" : escapeUsernameSpecialChars(value);
            long timestamp = update.getTimeCreated();

            return switch (update.getScope()) {
                case USERNAME, GLOBAL_NAME -> String.format("- <t:%d:d> %s\n", timestamp, name);

                case GUILD_NICK -> String.format("- <t:%d:d> %s <@%d>\n", timestamp, name, update.getAuthorId());
            };
        });
    }

    private static String escapeUsernameSpecialChars(String name) {
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

    private static String formatLevelUsers(PaginationItem<LevelView> levels) {
        StringBuilder builder = new StringBuilder();
        List<LevelView> entities = levels.getEntities();
        int offset = levels.getOffset();
        int pos = 1;

        for (LevelView user : entities) {
            int itemPos = offset + pos++;
            String row = String.format(
                    UserXP.LEADERBOARD_ROW_FORMAT,
                    itemPos, user.displayIdentifier(), Bot.humanizeNum(user.level())
            );
            builder.append(row).append("\n");
        }
        return builder.toString().strip();
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
