package ofc.bot.listeners.discord.interactions.buttons.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.domain.viewmodels.LeaderboardView;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@ButtonHandler(scope = Scopes.Economy.VIEW_LEADERBOARD, autoResponseType = AutoResponseType.DEFER_EDIT)
public class LeaderboardOffsetUpdate implements BotButtonListener {
    private final UserEconomyRepository ecoRepo;

    public LeaderboardOffsetUpdate(UserEconomyRepository ecoRepo) {
        this.ecoRepo = ecoRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        int pageIndex = ctx.get("page_index");
        Guild guild = ctx.getGuild();
        LeaderboardView leaderboard = ecoRepo.viewLeaderboard(pageIndex);
        boolean hasMorePages = leaderboard.hasMorePages();
        List<Button> newButtons = ButtonContextFactory.createLeaderboardButtons(pageIndex, hasMorePages);
        MessageEmbed newEmbed = EmbedFactory.embedLeaderboard(guild, leaderboard);

        return ctx.create()
                .setEmbeds(newEmbed)
                .setActionRow(newButtons)
                .edit();
    }
}