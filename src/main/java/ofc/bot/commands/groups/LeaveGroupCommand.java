package ofc.bot.commands.groups;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(name = "group leave", description = "Saia de um grupo específico.")
public class LeaveGroupCommand extends SlashSubcommand {
    private static final String PI_VALUE = "3141592653";
    private final OficinaGroupRepository grpRepo;

    public LeaveGroupCommand(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        int groupId = ctx.getSafeOption("group", OptionMapping::getAsInt);
        boolean silent = ctx.getOption("silent", false, OptionMapping::getAsBoolean);
        String pi = ctx.getSafeOption("confirmation", OptionMapping::getAsString);
        Member issuer = ctx.getIssuer();
        Guild guild = issuer.getGuild();
        List<Role> issuerRoles = issuer.getRoles();
        OficinaGroup group = grpRepo.findById(groupId);

        if (!PI_VALUE.equals(pi))
            return Status.INCORRECT_CONFIRMATION_VALUE;

        if (group == null)
            return Status.GROUP_NOT_FOUND;

        if (group.getOwnerId() == issuer.getIdLong())
            return Status.CANNOT_LEAVE_YOUR_OWN_GROUP;

        long groupRoleId = group.getRoleId();

        if (!isPresent(issuerRoles, groupRoleId))
            return Status.YOU_ARE_NOT_IN_THE_PROVIDED_GROUP;

        Role groupRole = guild.getRoleById(groupRoleId);

        // Should be impossible
        if (groupRole == null)
            return Status.GROUP_ROLE_NOT_FOUND;

        ctx.ack(true);
        guild.removeRoleFromMember(issuer, groupRole).queue(v -> {
            if (!silent)
                group.sendMessagef("\uD83D\uDC4B O membro %s saiu do grupo.", issuer.getAsMention());

            ctx.reply(Status.SUCCESSFULLY_REMOVED_FROM_GROUP.args(group.getName()));
        });

        return Status.OK;
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "group", "O grupo que deseja sair (pesquise pelo nome, emoji ou id do cargo).", true, true),
                new OptionData(OptionType.STRING, "confirmation", "Insira os 10 primeiros dígitos de PI (sem vírgula e/ou ponto).", true)
                        .setRequiredLength(10, 10),
                new OptionData(OptionType.BOOLEAN, "silent", "Sair sem avisar no chat (Padrão: false).")
        );
    }

    private boolean isPresent(List<Role> roles, long roleId) {
        return roles.stream()
                .anyMatch(r -> r.getIdLong() == roleId);
    }
}
