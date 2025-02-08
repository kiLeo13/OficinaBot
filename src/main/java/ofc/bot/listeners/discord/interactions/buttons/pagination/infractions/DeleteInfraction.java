package ofc.bot.listeners.discord.interactions.buttons.pagination.infractions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.commands.moderation.InfractionsCommand;
import ofc.bot.domain.entity.MemberPunishment;
import ofc.bot.domain.sqlite.repository.MemberPunishmentRepository;
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
import org.jooq.exception.DataAccessException;

import java.util.List;

@ButtonHandler(scope = Scopes.Punishments.DELETE_INFRACTION, autoResponseType = AutoResponseType.DEFER_EDIT)
public class DeleteInfraction implements BotButtonListener {
    private final MemberPunishmentRepository pnshRepo;

    public DeleteInfraction(MemberPunishmentRepository pnshRepo) {
        this.pnshRepo = pnshRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        long issuerId = ctx.getUserId();
        long guildId = ctx.getGuildId();
        long targetId = ctx.get("target_id");
        int pageIndex = ctx.get("page_index"); // Current page
        int infrId = ctx.get("infraction_id");
        Guild guild = ctx.getGuild();
        MemberPunishment infr = pnshRepo.findById(infrId);

        if (infr == null)
            return Status.INFRACTION_NOT_FOUND;

        infr.setActive(false)
                .setDeletionAuthorId(issuerId)
                .tickUpdate();

        // Saving to the database the inactivation ^^
        try {
            pnshRepo.upsert(infr);
        } catch (DataAccessException e) {
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }

        Bot.fetchUser(targetId).queue(target -> {
            int newPageIndex = Math.max(pageIndex - 1, 0);
            PaginationItem<MemberPunishment> infrs = Paginators.viewInfractions(
                    targetId, guildId, InfractionsCommand.PAGE_SIZE, newPageIndex);

            if (infrs.isEmpty()) {
                ctx.reply(Status.USER_HAS_NO_INFRACTIONS);
                return;
            }

            MemberPunishment updatedIfr = infrs.get(0);
            boolean active = updatedIfr.isActive();
            MessageEmbed embed = EmbedFactory.embedInfractions(target, guild, infrs, issuerId);
            List<Button> btns = ButtonContextFactory.createInfractionsButtons(
                    updatedIfr.getId(), active, targetId, newPageIndex, infrs.hasMore());

            ctx.create()
                    .setEmbeds(embed)
                    .setActionRow(btns)
                    .edit();
        }, (err) -> ctx.reply(Status.USER_NOT_FOUND));
        return Status.OK;
    }
}