package ofc.bot.commands.relationships.marriages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.sqlite.repository.MarriageRepository;
import ofc.bot.domain.viewmodels.MarriageView;
import ofc.bot.domain.viewmodels.MarriagesView;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@DiscordCommand(name = "marriage list", description = "Liste todos os casamentos do usuário fornecido.")
public class MarriageListCommand extends SlashSubcommand {
    private final MarriageRepository marrRepo;

    public static final int MAX_USERS_PER_PAGE = 10;

    public MarriageListCommand(MarriageRepository marrRepo) {
        this.marrRepo = marrRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        int page = ctx.getOption("page", 1, OptionMapping::getAsInt);
        Guild guild = ctx.getGuild();
        User sender = ctx.getUser();
        User target = ctx.getOption("user", sender, OptionMapping::getAsUser);
        long targetId = target.getIdLong();
        MarriagesView marriagesData = viewMarriages(targetId, page);
        int maxPages = marriagesData.maxPages();

        if (page > maxPages)
            return Status.PAGE_DOES_NOT_EXIST.args(maxPages);

        if (marriagesData.isEmpty())
            return Status.MARRIAGE_LIST_IS_EMPTY;

        MessageEmbed embed = EmbedFactory.embedMarriages(guild, target, marriagesData);
        boolean hasNext = marriagesData.page() < marriagesData.maxPages();
        List<Button> buttons = ButtonContextFactory.createMarriageListButtons(targetId, page, hasNext);

        return ctx.create()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .send();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "O usuário para descobrir os casamentos"),

                new OptionData(OptionType.INTEGER, "page", "A página a ver os casamentos.")
                        .setRequiredRange(1, Integer.MAX_VALUE)
        );
    }

    private MarriagesView viewMarriages(long userId, int inputPage) {
        List<MarriageView> rels = marrRepo.viewByUserId(userId);
        int count = marrRepo.countByUserId(userId);
        int maxPages = Bot.calcMaxPages(count, MAX_USERS_PER_PAGE);

        return new MarriagesView(rels, userId, inputPage, maxPages, count);
    }
}