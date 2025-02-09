package ofc.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@DiscordCommand(name = "unban", description = "Desbana um usuário do servidor.", permission = Permission.BAN_MEMBERS)
public class UnbanCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnbanCommand.class);

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        User user = ctx.getSafeOption("user", OptionMapping::getAsUser);
        Guild guild = ctx.getGuild();

        guild.unban(user).queue(v -> {
            ctx.reply(Status.MEMBER_SUCCESSFULLY_UNBANNED.args(user.getAsMention()));
        }, (err) -> {
            LOGGER.error("Could not unban user {}", user.getId(), err);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });
        return Status.OK;
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "O usuário a ser desbanido.", true)
        );
    }
}