package ofc.bot.commands.marriages.pagination.marriages;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.MarriageRecord;
import ofc.bot.handlers.buttons.ButtonManager;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import ofc.bot.util.Bot;
import ofc.bot.util.MarriageUtil;
import org.jooq.DSLContext;

import java.awt.*;
import java.util.List;
import java.util.UUID;

import static ofc.bot.databases.entities.tables.Marriages.MARRIAGES;
import static ofc.bot.databases.entities.tables.Users.USERS;

@DiscordCommand(name = "list", description = "Liste todos os casamentos do usuário fornecido.")
public class MarriageListSubcommand extends SlashSubcommand {
    private static final int MAX_USERS_PER_PAGE = 10;
    private static final Color EMBED_COLOR = new Color(255, 0, 127);

    @Option
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "O usuário para descobrir os casamentos");

    @Option
    private static final OptionData PAGE = new OptionData(OptionType.INTEGER, "page", "A página a ver os casamentos.")
            .setMinValue(1);

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        int page = ctx.getOption("page", 1, OptionMapping::getAsInt);
        Guild guild = ctx.getGuild();
        User sender = ctx.getUser();
        User target = ctx.getOption("user", sender, OptionMapping::getAsUser);
        long targetId = target.getIdLong();
        MarriagesData marriagesData = retrieveMarriageData(targetId, page);
        int maxPages = marriagesData.maxPages();

        if (page > maxPages)
            return Status.PAGE_DOES_NOT_EXIST.args(maxPages);

        if (marriagesData.isEmpty())
            return Status.MARRIAGE_LIST_IS_EMPTY;

        MessageEmbed embed = embed(guild, target, marriagesData);
        boolean hasNext = marriagesData.page() < marriagesData.maxPages();
        Button[] buttons = generateButtons(targetId, page, hasNext);

        ctx.reply()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .send();

        return Status.PASSED;
    }

    protected static Button[] generateButtons(long userId, int page, boolean hasNext) {

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
                .setIdentity("marriages")
                .setEntity(userId)
                .insert();

        ButtonManager.create(nextID)
                .setValueInt(nextPage)
                .setIdentity("marriages")
                .setEntity(userId)
                .insert();

        return new Button[]{ previous, next };
    }

    protected static MessageEmbed embed(Guild guild, User target, MarriagesData data) {

        EmbedBuilder builder = new EmbedBuilder();

        int currentPage = data.page();
        int maxPages = data.maxPages();
        String name = target.getEffectiveName();
        String avatar = target.getEffectiveAvatarUrl();
        String strfPage = Bot.strfNumber(currentPage);
        String strfMaxPages = Bot.strfNumber(maxPages);

        builder
                .setAuthor(name, null, avatar)
                .setDescription("Casamentos de `" + target.getEffectiveName() + "`.\n\n" + formatUsers(data.marriages()))
                .setColor(EMBED_COLOR)
                .setFooter("Pág " + strfPage + "/" + strfMaxPages, guild.getIconUrl());

        return builder.build();
    }

    protected static MarriagesData retrieveMarriageData(long userId, int inputPage) {

        // 'inputPage' is a user-provided page which is always equivalent to (index + 1)
        int pageIndex = inputPage - 1;
        int offset = pageIndex * MAX_USERS_PER_PAGE;
        DSLContext ctx = DBManager.getContext();

        List<MarriageRecord> marriages = ctx.select(MARRIAGES.REQUESTER_ID, MARRIAGES.TARGET_ID, MARRIAGES.CREATED_AT, USERS.GLOBAL_NAME)
                .from(MARRIAGES)
                .join(USERS)
                .on(MARRIAGES.REQUESTER_ID.eq(USERS.ID).or(MARRIAGES.TARGET_ID.eq(USERS.ID)))
                .where(MARRIAGES.REQUESTER_ID.eq(userId).or(MARRIAGES.TARGET_ID.eq(userId))
                        .and(USERS.ID.ne(userId)))
                .groupBy(USERS.ID)
                .orderBy(MARRIAGES.CREATED_AT)
                .offset(offset)
                .limit(10)
                .fetchInto(MARRIAGES);

        int count = MarriageUtil.getMarriageCount(userId);
        int maxPages = Bot.calculateMaxPages(count, MAX_USERS_PER_PAGE);

        return new MarriagesData(marriages, userId, inputPage, maxPages, count);
    }

    private static String formatUsers(List<MarriageRecord> marriages) {

        StringBuilder builder = new StringBuilder();

        for (MarriageRecord mr : marriages) {
            String text = String.format("- %s (<t:%d>)\n", mr.getSelectedUserEffectiveName(), mr.getCreated());
            builder.append(text);
        }

        return builder.toString()
                .strip();
    }
}