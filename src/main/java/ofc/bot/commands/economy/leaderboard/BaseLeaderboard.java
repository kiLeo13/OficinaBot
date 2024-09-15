package ofc.bot.commands.economy.leaderboard;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.EconomyRecord;
import ofc.bot.databases.entities.tables.Economy;
import ofc.bot.handlers.buttons.ButtonManager;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;
import org.jooq.Result;

import java.util.List;
import java.util.UUID;

import static ofc.bot.databases.entities.tables.Economy.ECONOMY;
import static ofc.bot.databases.entities.tables.Users.USERS;

@DiscordCommand(name = "leaderboard", description = "Veja o placar de líderes global da economia.")
public class BaseLeaderboard extends SlashCommand {
    private static final int MAX_USERS_PER_PAGE = 10;

    @Option
    private static final OptionData PAGE = new OptionData(OptionType.INTEGER, "page", "A página do placar de líderes a verificar.")
            .setMinValue(1);

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        int page = ctx.getOption("page", 1, OptionMapping::getAsInt);
        Guild guild = ctx.getGuild();
        LeaderboardData leaderboard = retrieveLeaderboard(page);
        int maxPages = leaderboard.maxPages();

        if (page > maxPages)
            return Status.PAGE_DOES_NOT_EXIST.args(maxPages);

        if (leaderboard.isEmpty())
            return Status.LEADERBOARD_IS_EMPTY;

        boolean hasMorePages = page < maxPages;
        Button[] buttons = generateButtons(page, hasMorePages);
        MessageEmbed embed = embed(guild, leaderboard);

        ctx.reply()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .send();

        return Status.PASSED;
    }

    public static Button[] generateButtons(int page, boolean hasNext) {

        int previousPage = page - 1;
        int nextPage = page + 1;
        boolean hasPrevious = page > 1;

        String previousID = UUID.randomUUID().toString();
        String nextID = UUID.randomUUID().toString();
        Button previous = Button.primary(previousID, "Previous")
                .withDisabled(!hasPrevious);
        Button next = Button.primary(nextID, "Next")
                .withDisabled(!hasNext);

        ButtonManager.create(previousID)
                .setValueInt(previousPage)
                .setIdentity("leaderboard")
                .insert();

        ButtonManager.create(nextID)
                .setValueInt(nextPage)
                .setIdentity("leaderboard")
                .insert();

        return new Button[]{ previous, next };
    }

    public static MessageEmbed embed(Guild guild, LeaderboardData leaderboard) {

        EmbedBuilder builder = new EmbedBuilder();

        int page = leaderboard.page();
        String pages = String.format("Pág %s/%s", Bot.strfNumber(page), Bot.strfNumber(leaderboard.maxPages()));
        List<EconomyRecord> users = leaderboard.usersData();

        builder
                .setAuthor("Economy Leaderboard", null, Economy.BANK_ICON)
                .setDescription("💸 Placar de Líderes Global.\n\n" + formatUsers(users, page))
                .setColor(Bot.Colors.DEFAULT)
                .setFooter(pages, guild.getIconUrl());

        return builder.build();
    }

    private static String formatUsers(List<EconomyRecord> users, int page) {

        StringBuilder builder = new StringBuilder();
        int offset = (page - 1) * MAX_USERS_PER_PAGE;
        int pos = 1;

        for (EconomyRecord er : users) {
            String name = er.getUserName();
            String rank = String.format("%d.", offset + pos++);
            String prettyBalance = Bot.strfNumber(er.getBalance());

            String text = String.format("%s `%s`**・**$%s\n", rank, name, prettyBalance);

            builder.append(text);
        }

        return builder.toString().strip();
    }

    @SuppressWarnings("DataFlowIssue")
    public static LeaderboardData retrieveLeaderboard(int inputPage) {

        // 'inputPage' is a user-provided page which is always equivalent to (index + 1)
        int pageIndex = inputPage - 1;
        int offset = pageIndex * MAX_USERS_PER_PAGE;
        DSLContext ctx = DBManager.getContext();

        Result<EconomyRecord> economyData = ctx.select(ECONOMY.USER_ID, ECONOMY.BALANCE, USERS.NAME)
                .from(ECONOMY)
                .join(USERS).on(ECONOMY.USER_ID.eq(USERS.ID))
                .groupBy(ECONOMY.USER_ID)
                .orderBy(ECONOMY.BALANCE.desc())
                .offset(offset)
                .limit(10)
                .fetchInto(ECONOMY);

        Integer rowsCount = ctx.selectCount()
                .from(ECONOMY)
                .fetchOneInto(int.class);

        int maxPages = Bot.calculateMaxPages(rowsCount, MAX_USERS_PER_PAGE);

        return new LeaderboardData(economyData, inputPage, maxPages);
    }
}