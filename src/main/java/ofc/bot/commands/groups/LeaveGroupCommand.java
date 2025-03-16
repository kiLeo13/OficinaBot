package ofc.bot.commands.groups;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

import java.util.List;

@DiscordCommand(name = "group leave")
public class LeaveGroupCommand extends SlashSubcommand {
    private static final String PI_VALUE = "3141592653";
    private static final String FAKE_PI = "3141593428";
    private final OficinaGroupRepository grpRepo;

    public LeaveGroupCommand(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        int groupId = ctx.getSafeOption("group", OptionMapping::getAsInt);
        boolean silent = ctx.getOption("silent", false, OptionMapping::getAsBoolean);
        String pi = ctx.getSafeOption("pi-confirmation", OptionMapping::getAsString);
        Member issuer = ctx.getIssuer();
        Guild guild = issuer.getGuild();
        List<Role> issuerRoles = issuer.getRoles();
        OficinaGroup group = grpRepo.findById(groupId);

        if (FAKE_PI.equals(pi))
            return Status.FAKE_PI_JOKE;

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
    protected void init() {
        setDesc("Saia de um grupo específico.");

        addOpt(OptionType.INTEGER, "group", "O grupo que deseja sair (pesquise pelo nome, emoji ou id do cargo).", true, true);
        addOpt(OptionType.STRING, "pi-confirmation", "Insira os 10 primeiros dígitos de PI (sem vírgula e/ou ponto).", true, true, 10, 10);
        addOpt(OptionType.BOOLEAN, "silent", "Sair sem avisar no chat (Padrão: false).");
    }

    private boolean isPresent(List<Role> roles, long roleId) {
        return roles.stream()
                .anyMatch(r -> r.getIdLong() == roleId);
    }

    @DiscordEventHandler
    public static final class FakePISuggester extends ListenerAdapter {

        @Override
        public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
            AutoCompleteQuery focused = e.getFocusedOption();
            String name = focused.getName();

            if (!name.equals("pi-confirmation")) return;

            e.replyChoice(FAKE_PI, FAKE_PI).queue();
        }
    }
}
