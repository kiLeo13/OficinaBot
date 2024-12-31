package ofc.bot.listeners.discord.interactions.buttons.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.Main;
import ofc.bot.commands.relationships.marriages.ProposalsListCommand;
import ofc.bot.domain.entity.MarriageRequest;
import ofc.bot.domain.sqlite.repository.MarriageRequestRepository;
import ofc.bot.domain.viewmodels.ProposalsView;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@ButtonHandler(scope = MarriageRequest.MARRIAGE_BUTTON_SCOPE)
public class ProposalListPagination implements BotButtonListener {
    private final MarriageRequestRepository mreqRepo;

    public ProposalListPagination(MarriageRequestRepository mreqRepo) {
        this.mreqRepo = mreqRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        int page = ctx.get("page");
        long userId = ctx.get("target_id");
        Guild guild = ctx.getGuild();
        String type = ctx.get("type");
        ProposalsView proposals = mreqRepo.viewProposals(type, userId, page - 1);
        boolean hasMorePages = proposals.page() < proposals.maxPages();
        List<Button> buttons = ButtonContextFactory.createProposalsListButtons(page, userId, hasMorePages, type);

        Main.getApi().retrieveUserById(userId).queue((user) -> {
            MessageEmbed embed = EmbedFactory.embedProposals(guild, user, proposals);

            ctx.create()
                    .setEmbeds(embed)
                    .setActionRow(buttons)
                    .edit();
        }, (e) -> ctx.reply(Status.USER_NOT_FOUND));

        return Status.OK;
    }
}
