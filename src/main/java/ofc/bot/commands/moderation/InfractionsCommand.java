package ofc.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.MemberPunishment;
import ofc.bot.handlers.interactions.EntityContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.handlers.paginations.PaginationItem;
import ofc.bot.handlers.paginations.Paginators;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@DiscordCommand(name = "infractions", permission = Permission.MANAGE_SERVER)
public class InfractionsCommand extends SlashCommand {
    public static final int PAGE_SIZE = 1;

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Guild guild = ctx.getGuild();
        User target = ctx.getSafeOption("user", OptionMapping::getAsUser);
        boolean showInactive = ctx.getOption("show-inactive", false, OptionMapping::getAsBoolean);
        long targetId = target.getIdLong();
        long guildId = guild.getIdLong();
        PaginationItem<MemberPunishment> infrs = Paginators.viewInfractions(
                targetId, guildId, PAGE_SIZE, 0, showInactive);

        if (infrs.isEmpty())
            return Status.USER_HAS_NO_INFRACTIONS;

        MemberPunishment infr = infrs.get(0);
        boolean active = infr.isActive();
        List<Button> btns = EntityContextFactory.createInfractionsButtons(
                infr.getId(), active, targetId, infrs.getPageIndex(), showInactive, infrs.hasMore());
        MessageEmbed embed = EmbedFactory.embedInfractions(target, guild, infrs, infr.getModeratorId());

        return ctx.create()
                .setEmbeds(embed)
                .setActionRow(btns)
                .send();
    }

    @Override
    protected void init() {
        setDesc("Veja as infrações de um usuário.");

        addOpt(OptionType.USER, "user", "O usuário que deseja ver as infrações.", true);
        addOpt(OptionType.BOOLEAN, "show-inactive", "Mostrar até infrações já apagadas? (Padrão: False).");
    }
}
