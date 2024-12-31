package ofc.bot.listeners.discord.interactions.buttons.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.Main;
import ofc.bot.commands.relationships.marriages.MarriageListCommand;
import ofc.bot.domain.entity.Marriage;
import ofc.bot.domain.sqlite.repository.MarriageRepository;
import ofc.bot.domain.viewmodels.MarriageView;
import ofc.bot.domain.viewmodels.MarriagesView;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@ButtonHandler(scope = Marriage.MARRIAGE_BUTTON_SCOPE)
public class MarriageListPagination implements BotButtonListener {
    private final MarriageRepository marrRepo;

    public MarriageListPagination(MarriageRepository marrRepo) {
        this.marrRepo = marrRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        Message message = ctx.getMessage();
        Guild guild = message.getGuild();
        int page = ctx.get("page");
        long targetId = ctx.get("target_id");
        MarriagesView marriagesData = viewMarriages(targetId, page);
        boolean hasMorePages = marriagesData.page() < marriagesData.maxPages();
        List<Button> newButtons = ButtonContextFactory.createMarriageListButtons(targetId, page, hasMorePages);

        Main.getApi().retrieveUserById(marriagesData.userId()).queue((target) -> {
            MessageEmbed newEmbed = EmbedFactory.embedMarriages(guild, target, marriagesData);

            message.editMessageEmbeds(newEmbed)
                    .setComponents(ActionRow.of(newButtons))
                    .queue();
        }, (error) -> ctx.reply(Status.USER_NOT_FOUND));

        return Status.OK;
    }

    private MarriagesView viewMarriages(long userId, int inputPage) {
        List<MarriageView> rels = marrRepo.viewByUserId(userId);
        int count = marrRepo.countByUserId(userId);
        int maxPages = Bot.calcMaxPages(count, MarriageListCommand.MAX_USERS_PER_PAGE);

        return new MarriagesView(rels, userId, inputPage, maxPages, count);
    }
}