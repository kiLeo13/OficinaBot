package ofc.bot.handlers.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import ofc.bot.util.content.annotations.listeners.EventHandler;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.contexts.SlashCommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.AbstractCommandData;
import ofc.bot.handlers.commands.slash.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@EventHandler
public class SlashInteractionHandler extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlashInteractionHandler.class);
    private static final ExecutorService EXECUTOR = ForkJoinPool.commonPool();

    private static final long TARGET_GUILD_ID = 582430782577049600L;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        Guild guild = event.getGuild();
        
        if (guild == null)
            return;

        if (guild.getIdLong() != TARGET_GUILD_ID)
            return;

        String fullName = event.getFullCommandName();
        AbstractCommandData cmd = CommandsRegistryManager.resolveCommand(fullName);
        CommandContext ctx = new SlashCommandContext(event);
        Member member = ctx.getIssuer();
        User user = ctx.getUser();
        long userId = user.getIdLong();

        // Command not found by name
        if (cmd == null)
            return;

        long waitCooldown = resolveCooldown(cmd, userId);

        if (waitCooldown > 0 && !member.hasPermission(Permission.ADMINISTRATOR)) {
            ctx.reply(Status.PLEASE_WAIT_COOLDOWN.args(waitCooldown + "s"));
            LOGGER.warn("User '@{} ({})' hit command \"/{}\" rate-limit, time remain: {}s", user.getName(), userId, fullName, waitCooldown);
            return;
        }

        if (cmd.isAutoDefer())
            event.deferReply(cmd.isDeferEphemeral()).queue();

        handleCommand(ctx, cmd);
        cmd.refreshCooldown(userId);
    }

    /**
     * Determines the remaining cooldown time (in seconds) before the user can execute the same command again.
     * <p>
     * Negative values may be returned, indicating that the cooldown expectation has been exceeded.
     * For example, if the cooldown is set to 30s but the user has waited 40s, -10 will be returned.
     *
     * @param cmd The command attempting execution.
     * @param userId The user's ID attempting command usage.
     * @return The remaining cooldown time (in seconds).
     */
    private long resolveCooldown(AbstractCommandData cmd, long userId) {

        int cooldown = cmd.getCooldown(); // 30
        long period = cmd.getUserCooldown(userId);

        return cooldown == 0
                ? 0
                : cooldown - period;
    }

    private void handleCommand(CommandContext ctx, CommandExecutor cmd) {

        SlashCommandInteraction interaction = ctx.getInteraction();
        User user = ctx.getUser();
        String cmdName = interaction.getFullCommandName();
        List<OptionMapping> options = interaction.getOptions();

        EXECUTOR.execute(() -> {

            try {
                // This is an awful way of getting the response time,
                // but it's more about aesthetics than debugging itself :)
                long start = System.currentTimeMillis();
                CommandResult status = cmd.onCommand(ctx);
                long end = System.currentTimeMillis();

                output(user.getName(), cmdName, status, options, (int) (end - start));

                if (status != null && status.getContent() != null)
                    ctx.reply(status.getContent(), status.isEphemeral());

            } catch (Exception e) {
                LOGGER.error("Could not execute command \"/{}\"", cmdName, e);
                ctx.reply("Ocorreu um erro :/", true);
            }
        });
    }

    private void output(String userName, String cmdName, CommandResult result, List<OptionMapping> options, int millis) {

        String status = result == null ? "Unknown" : result.getStatus().name();

        LOGGER.info("{} issued \"/{}{}\" with status {}, took {}ms",
                userName,
                cmdName,
                formatOptions(options),
                status,
                millis
        );
    }

    private String formatOptions(List<OptionMapping> options) {

        if (options.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder();

        builder.append(' ');

        for (OptionMapping opt : options) {
            builder.append(opt.getName())
                    .append(": ")
                    .append(getPrintableValue(opt))
                    .append(' ');
        }

        builder.setLength(builder.length() - 1); // Remove the whitespace at the end

        return builder.toString().stripTrailing();
    }

    private String getPrintableValue(OptionMapping opt) {

        OptionType type = opt.getType();

        return switch (type) {

            case BOOLEAN -> String.valueOf(opt.getAsBoolean());
            case STRING -> '"' + opt.getAsString() + '"';
            case INTEGER, NUMBER, ROLE, MENTIONABLE, CHANNEL -> opt.getAsString();
            case USER -> "@" + opt.getAsUser().getName();
            case ATTACHMENT -> opt.getAsAttachment().getUrl();

            case UNKNOWN -> throw new IllegalStateException("Received unknown option type for option " + opt.getName());

            case SUB_COMMAND, SUB_COMMAND_GROUP -> throw new IllegalArgumentException("Subcommand or Subcommand group should not be provided as command options");
        };
    }
}