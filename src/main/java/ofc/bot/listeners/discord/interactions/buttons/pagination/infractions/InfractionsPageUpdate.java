package ofc.bot.listeners.discord.interactions.buttons.pagination.infractions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.commands.moderation.InfractionsCommand;
import ofc.bot.domain.entity.MemberPunishment;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.paginations.PaginationItem;
import ofc.bot.handlers.paginations.Paginators;
import ofc.bot.util.Bot;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@ButtonHandler(scope = Scopes.Punishments.VIEW_INFRACTIONS, autoResponseType = AutoResponseType.DEFER_EDIT)
public class InfractionsPageUpdate implements BotButtonListener {

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        int pageIndex = ctx.get("page_index");
        long targetId = ctx.get("target_id");
        long guildId = ctx.getGuildId();
        Guild guild = ctx.getGuild();
        PaginationItem<MemberPunishment> infrs = Paginators.viewInfractions(
                targetId, guildId, InfractionsCommand.PAGE_SIZE, pageIndex);

        if (infrs.isEmpty())
            return Status.USER_HAS_NO_INFRACTIONS;

        MemberPunishment infr = infrs.get(0);
        boolean active = infr.isActive();
        List<Button> btns = ButtonContextFactory.createInfractionsButtons(
                infr.getId(), active, targetId, pageIndex, infrs.hasMore());

        Bot.fetchUser(targetId).queue(target -> {
            long infrModeratorId = infr.getModeratorId();
            MessageEmbed embed = EmbedFactory.embedInfractions(target, guild, infrs, infrModeratorId);

            ctx.create()
                    .setEmbeds(embed)
                    .setActionRow(btns)
                    .edit();
        }, (err) -> ctx.reply(Status.USER_NOT_FOUND));
        return Status.OK;
    }
}
