package ofc.bot.listeners.discord.interactions.buttons.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.UserNameUpdate;
import ofc.bot.domain.entity.enums.NameScope;
import ofc.bot.domain.sqlite.repository.UserNameUpdateRepository;
import ofc.bot.domain.viewmodels.NamesHistoryView;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@ButtonHandler(scope = UserNameUpdate.NAME_UPDATE_SCOPE, autoResponseType = AutoResponseType.DEFER_EDIT)
public class NamesPageUpdate implements BotButtonListener {
    private final UserNameUpdateRepository namesRepo;

    public NamesPageUpdate(UserNameUpdateRepository repo) {
        this.namesRepo = repo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        Guild guild = ctx.getGuild();
        NameScope type = ctx.get("type");
        long targetId = ctx.get("target_id");
        int offset = ctx.get("offset");
        NamesHistoryView namesData = namesRepo.viewByUserId(type, targetId, offset, 10);
        boolean hasMorePages = namesData.page() < namesData.maxPages();
        List<Button> newButtons = ButtonContextFactory.createNamesHistoryButtons(type, targetId, offset, hasMorePages);

        if (namesData.isEmpty())
            return ctx.reply(Status.NO_RESULT_FOUND);

        Bot.fetchUser(targetId).queue((target) -> {
            MessageEmbed newEmbed = EmbedFactory.embedUsernameUpdates(namesData, guild, target);

            ctx.editEmbeds(newEmbed)
                    .setComponents(ActionRow.of(newButtons))
                    .queue();

        }, (error) -> ctx.reply(Status.USER_NOT_FOUND));

        return Status.OK;
    }
}