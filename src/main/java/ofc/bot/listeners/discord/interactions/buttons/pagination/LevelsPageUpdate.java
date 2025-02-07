package ofc.bot.listeners.discord.interactions.buttons.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.sqlite.repository.UserXPRepository;
import ofc.bot.domain.viewmodels.LevelView;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.paginations.PaginationItem;
import ofc.bot.handlers.paginations.Paginators;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@ButtonHandler(scope = Scopes.Misc.PAGINATE_LEVELS, autoResponseType = AutoResponseType.DEFER_EDIT)
public class LevelsPageUpdate implements BotButtonListener {
    private final UserXPRepository xpRepo;

    public LevelsPageUpdate(UserXPRepository xpRepo) {
        this.xpRepo = xpRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        int pageIndex = ctx.get("page_index");
        long authorId = ctx.getAuthorId();
        Guild guild = ctx.getGuild();
        PaginationItem<LevelView> pageItem = Paginators.viewLevels(pageIndex);

        if (pageItem.isEmpty())
            return Status.LEADERBOARD_IS_EMPTY;

        boolean hasMore = pageItem.hasMore();
        LevelView levelView = xpRepo.viewLevelByUserId(authorId);
        List<Button> buttons = ButtonContextFactory.createLevelsButtons(authorId, pageIndex, hasMore);
        MessageEmbed embed = EmbedFactory.embedLevels(guild, levelView, pageItem);

        return ctx.create()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .edit();
    }
}