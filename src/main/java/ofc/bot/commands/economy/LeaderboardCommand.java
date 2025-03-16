package ofc.bot.commands.economy;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.domain.viewmodels.LeaderboardView;
import ofc.bot.handlers.interactions.EntityContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@DiscordCommand(name = "leaderboard")
public class LeaderboardCommand extends SlashCommand {
    public static final int MAX_USERS_PER_PAGE = 10;
    private final UserEconomyRepository ecoRepo;

    public LeaderboardCommand(UserEconomyRepository ecoRepo) {
        this.ecoRepo = ecoRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        int pageIndex = ctx.getOption("page", 1, OptionMapping::getAsInt) - 1;
        Guild guild = ctx.getGuild();
        LeaderboardView leaderboard = ecoRepo.viewLeaderboard(pageIndex);
        int lastPageIndex = leaderboard.getLastPageIndex();

        if (pageIndex > lastPageIndex)
            return Status.PAGE_DOES_NOT_EXIST.args(lastPageIndex + 1);

        if (leaderboard.isEmpty())
            return Status.LEADERBOARD_IS_EMPTY;

        boolean hasMorePages = leaderboard.hasMorePages();
        List<Button> buttons = EntityContextFactory.createLeaderboardButtons(pageIndex, hasMorePages);
        MessageEmbed embed = EmbedFactory.embedLeaderboard(guild, leaderboard);

        return ctx.create()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .send();
    }

    @Override
    protected void init() {
        setDesc("Veja o placar de líderes global da economia.");

        addOpt(OptionType.INTEGER, "page", "A página do placar de líderes a verificar.", 1, Integer.MAX_VALUE);
    }
}