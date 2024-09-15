package ofc.bot.commands.marriages.pagination.proposals;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.MarriageRequestRecord;
import ofc.bot.handlers.buttons.ButtonManager;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Result;

import java.util.List;
import java.util.UUID;

import static ofc.bot.databases.entities.tables.MarriageRequests.MARRIAGE_REQUESTS;

@DiscordCommand(name = "proposals", description = "Veja as propostas de casamento pendentes.")
public class BaseProposals extends SlashCommand {
    private static final int MAX_USERS_PER_PAGE = 10;

    @Option(required = true)
    private static final OptionData TYPE = new OptionData(OptionType.STRING, "type", "O tipo de proposta a ser listado.")
            .addChoice("Outgoing", "out")
            .addChoice("Incoming", "in");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User user = ctx.getUser();
        long senderId = user.getIdLong();
        Guild guild = ctx.getGuild();
        String type = ctx.getSafeOption("type", OptionMapping::getAsString);
        ProposalsData proposals = retrieveProposals(type, senderId, 1);

        if (proposals.isEmpty())
            return Status.MARRIAGE_PROPOSAL_LIST_IS_EMPTY;

        boolean hasMorePages = 1 < proposals.maxPages();
        Button[] buttons = generateButtons(1, senderId, hasMorePages, type);
        MessageEmbed embed = embed(guild, user, proposals);

        ctx.reply()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .send();

        return Status.PASSED;
    }

    public static Button[] generateButtons(int page, long userId, boolean hasNext, String type) {

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
                .setPayload(type)
                .setEntity(userId)
                .setValueInt(previousPage)
                .setIdentity("proposals")
                .insert();

        ButtonManager.create(nextID)
                .setPayload(type)
                .setEntity(userId)
                .setValueInt(nextPage)
                .setIdentity("proposals")
                .insert();

        return new Button[]{ previous, next };
    }

    public static MessageEmbed embed(Guild guild, User user, ProposalsData proposals) {

        EmbedBuilder builder = new EmbedBuilder();
        String type = proposals.type();
        String prettyType = "in".equals(type)
                ? "recebidas"
                : "enviadas";

        int page = proposals.page();
        int maxPages = proposals.maxPages();
        List<MarriageRequestRecord> users = proposals.requests();
        String pages = String.format("Pág %s/%s", Bot.strfNumber(page), Bot.strfNumber(maxPages));
        String prettyCount = Bot.strfNumber(proposals.requestCount());
        String desc = String.format("Propostas de casamento **%s**: `%s`.\n\n%s", prettyType, prettyCount, formatRequests(users, type));

        builder
                .setAuthor(user.getEffectiveName(), null, user.getEffectiveAvatarUrl())
                .setDescription(desc)
                .setColor(Bot.Colors.DEFAULT)
                .setFooter(pages, guild.getIconUrl());

        return builder.build();
    }

    private static String formatRequests(List<MarriageRequestRecord> requests, String type) {

        return Bot.format(requests, (req) -> {

            long timestamp = req.getCreated();

            return switch (type) {

                case "in" -> String.format("- <@%d> (<t:%d:D>)\n", req.getRequesterId(), timestamp);

                case "out" -> String.format("- <@%d> (<t:%d:D>)\n", req.getTargetId(), timestamp);

                default -> throw new IllegalStateException("Unexpected value: " + type);
            };
        });
    }

    @SuppressWarnings("DataFlowIssue")
    public static ProposalsData retrieveProposals(String type, long userId, int inputPage) {

        // 'inputPage' starts at 1
        int pageIndex = inputPage - 1;
        int offset = pageIndex * MAX_USERS_PER_PAGE;
        DSLContext ctx = DBManager.getContext();
        Condition condition = "out".equals(type)
                ? MARRIAGE_REQUESTS.REQUESTER_ID.eq(userId)
                : MARRIAGE_REQUESTS.TARGET_ID.eq(userId);

        Result<MarriageRequestRecord> requests = ctx.selectFrom(MARRIAGE_REQUESTS)
                .where(condition)
                .groupBy(MARRIAGE_REQUESTS.ID)
                .orderBy(MARRIAGE_REQUESTS.CREATED_AT.desc())
                .offset(offset)
                .limit(MAX_USERS_PER_PAGE)
                .fetchInto(MARRIAGE_REQUESTS);

        int rowsCount = ctx.selectCount()
                .from(MARRIAGE_REQUESTS)
                .where(condition)
                .fetchOneInto(int.class);

        int maxPages = Bot.calculateMaxPages(rowsCount, MAX_USERS_PER_PAGE);

        return new ProposalsData(requests, type, userId, inputPage, maxPages, rowsCount);
    }
}