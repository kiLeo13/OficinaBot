package ofc.bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.Channels;
import ofc.bot.util.content.Roles;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "among", permission = Permission.MANAGE_ROLES)
public class RoleAmongUsCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAmongUsCommand.class);

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        ctx.ack();

        Member target = ctx.getOption("member", OptionMapping::getAsMember);
        Guild guild = ctx.getGuild();
        MessageChannel channel = ctx.getChannel();
        Role roleAmongUs = guild.getRoleById(Roles.AMONG_US.id());

        if (Channels.C.id() != channel.getIdLong())
            return Status.INCORRECT_CHANNEL_OF_USAGE;

        if (target == null)
            return Status.MEMBER_NOT_FOUND;

        if (roleAmongUs == null)
            return Status.ROLE_NOT_FOUND;

        if (target.getUser().isBot())
            return Status.TARGET_MAY_NOT_BE_A_BOT;

        if (target.getRoles().contains(roleAmongUs))
            return Status.MEMBER_ALREADY_HAS_ROLE.args(roleAmongUs.getName());

        guild.addRoleToMember(target, roleAmongUs).queue((s) -> {
            ctx.reply(Status.ROLE_SUCCESSFULLY_ADDED_TO_MEMBER.args(roleAmongUs.getName(), target.getAsMention()));
        }, (e) -> {
            LOGGER.error("Could not give role '{}' to member '{}'", roleAmongUs.getId(), target.getId(), e);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });
        return Status.OK;
    }

    @Override
    protected void init() {
        setDesc("Permite staffs em Ajudantes Superior+ a darem o cargo de Já Participou Among Us.");

        addOpt(OptionType.USER, "member", "O usuário a dar o cargo.", true);
    }
}