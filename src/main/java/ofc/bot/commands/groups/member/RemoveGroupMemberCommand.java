package ofc.bot.commands.groups.member;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(name = "group member remove", description = "Remove um membro do seu grupo.")
public class RemoveGroupMemberCommand extends SlashSubcommand {
    private final OficinaGroupRepository grpRepo;

    public RemoveGroupMemberCommand(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        long ownerId = ctx.getUserId();
        Member member = ctx.getOption("member", OptionMapping::getAsMember);
        OficinaGroup group = grpRepo.findByOwnerId(ownerId);

        if (member == null)
            return Status.MEMBER_NOT_FOUND;

        if (group == null)
            return Status.YOU_DO_NOT_OWN_A_GROUP;

        if (!hasRole(member, group.getRoleId()))
            return Status.MEMBER_NOT_IN_THE_GROUP;

        Button confirmation = ButtonContextFactory.createRemoveGroupMemberConfirmationButton(group, member.getIdLong());
        return ctx.create(true)
                .setContent(Status.CONFIRM_GROUP_MEMBER_REMOVE.args(member.getAsMention()))
                .setActionRow(confirmation)
                .send();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "member", "O membro a ser removido do grupo.", true)
        );
    }

    private boolean hasRole(Member member, long roleId) {
        return member.getRoles()
                .stream()
                .anyMatch(r -> r.getIdLong() == roleId);
    }
}
