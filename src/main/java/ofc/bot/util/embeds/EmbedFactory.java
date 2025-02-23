package ofc.bot.util.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import ofc.bot.commands.economy.LeaderboardCommand;
import ofc.bot.domain.entity.*;
import ofc.bot.domain.entity.enums.GroupPermission;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.viewmodels.*;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.handlers.paginations.PaginationItem;
import ofc.bot.util.Bot;
import ofc.bot.util.OficinaEmbed;

import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.*;

/**
 * Utility class for embeds used in multiple classes.
 * <p>
 * If an embed is used in a single command, with no pagination or confirmation
 * system, then the {@code embed()} method will remain in the same class.
 */
public final class EmbedFactory {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
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

    public static MessageEmbed embedChoosableRoles(Guild guild, String title, String desc, String filename, int color) {
        OficinaEmbed builder = new OficinaEmbed();
        Color embedColor = color == 0 ? Bot.Colors.DEFAULT : new Color(color);

        return builder
                .setAuthor(title, null, guild.getIconUrl())
                .setDesc(desc)
                .setColor(embedColor)
                .setImageIf(filename != null, "attachment://" + filename)
                .build();
    }

    public static MessageEmbed embedTransactions(User user, Guild guild, PaginationItem<BankTransaction> trs) {
        OficinaEmbed builder = new OficinaEmbed();
        String title = String.format("Transações de %s", user.getEffectiveName());
        String resultsFound = String.format("Resultados encontrados: `%s`.", Bot.fmtNum(trs.getRowCount()));
        String pages = String.format("Pág. %s/%s", Bot.fmtNum(trs.getPage()), Bot.fmtNum(trs.getPageCount()));

        return builder
                .setAuthor(title, null, user.getEffectiveAvatarUrl())
                .setColor(Bot.Colors.DEFAULT)
                .setDesc(resultsFound)
                .appendDescription(formatTransactions(trs.getEntities()))
                .setFooter(pages, guild.getIconUrl())
                .build();
    }

    public static MessageEmbed embedInfractions(
            User user, Guild guild, PaginationItem<MemberPunishment> infrs, long moderatorId
    ) {
        OficinaEmbed builder = new OficinaEmbed();
        MemberPunishment infr = infrs.get(0);
        boolean active = infr.isActive();
        String modMention = String.format("<@%d>", moderatorId);
        String infrCreation = String.format("<t:%d>", infr.getTimeCreated());
        String resultsFound = String.format("Resultados encontrados: `%s`.", Bot.fmtNum(infrs.getRowCount()));
        String pages = String.format("Pág. %s/%s", Bot.fmtNum(infrs.getPage()), Bot.fmtNum(infrs.getPageCount()));
        String delAuthorMention = Bot.ifNull(infr.getDeletionAuthorMention(), "Ninguém");

        return builder
                .setAuthor(user.getEffectiveName(), null, user.getAvatarUrl())
                .setColor(Bot.Colors.DEFAULT)
                .setDesc(resultsFound)
                .addField("👑 Moderador", modMention)
                .addField("📅 Punido em", infrCreation)
                .addField("📌 Ativo", active ? "Sim" : "Não")
                .addFieldIf(!active, "🚫 Removido por", delAuthorMention)
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

    private static String formatTransactions(List<BankTransaction> trs) {
        TransactionEntryBuilder builder = new TransactionEntryBuilder();

        builder.addSeparator();
        for (BankTransaction tr : trs) {
            long amount = tr.getAmount();
            CurrencyType currency = tr.getCurrencyType();
            TransactionType action = tr.getAction();
            AppUser user = tr.retrieveUser();
            AppUser receiver = tr.retrieveReceiver();
            LocalDateTime timestamp = LocalDateTime.ofEpochSecond(tr.getTimeCreated(), 0, ZoneOffset.ofHours(-3));
            String fmtTimestamp = timestamp.format(DATE_TIME_FORMATTER);
            String recName = receiver == null ? null : receiver.getName();
            String comment = Bot.ifNull(tr.getComment(), "--");
            StoreItemType product = tr.getProduct();
            String productName = product == null ? null : product.getName();

            builder.addF("\uD83C\uDF10 ID: #%d | \uD83D\uDD52 %s (GMT -3)", tr.getId(), fmtTimestamp)
                    .addF("\uD83C\uDF88 Tipo: %s", action.getName())
                    .addFIf(product != null, "\uD83E\uDDE9 Item: %s", productName)
                    .addF("\uD83E\uDD11 %s: %s", resolveUserAlias(action), user.getName())
                    .addFIf(receiver != null, "\uD83D\uDC64 Recebente: %s", recName)
                    .addF("\uD83D\uDCC3 Nota: %s", comment)
                    .addF("\uD83D\uDCB0 Valor: %s (%s)", Bot.fmtMoney(amount), currency.getName())
                    .addSeparator();
        }
        return builder.build();
    }

    private static String resolveUserAlias(TransactionType action) {
        return switch (action) {
            case FEE_PAID,
                 INVOICE_PAID -> "Pagador";
            case CHAT_MONEY,
                 WORK_EXECUTED,
                 MARRIAGE_CREATED,
                 DAILY_COLLECTED -> "Membro";
            case BALANCE_SET,
                 BALANCE_UPDATED -> "Moderador";
            case ITEM_BOUGHT -> "Comprador";
            case ITEM_SOLD -> "Vendedor";
            case MONEY_TRANSFERRED -> "Remetente";
        };
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

    private static final class TransactionEntryBuilder {
        private final List<String> fields = new ArrayList<>();

        TransactionEntryBuilder addSeparator() {
            return addF("---------------------------------------------");
        }

        TransactionEntryBuilder addF(String format, Object... args) {
            this.fields.add(String.format(format, args));
            return this;
        }

        TransactionEntryBuilder addFIf(boolean cond, String format, Object... args) {
            if (cond) {
                addF(format, args);
            }
            return this;
        }

        String build() {
            return String.format("```yml\n%s\n```", String.join("\n", fields)).strip();
        }
    }
}