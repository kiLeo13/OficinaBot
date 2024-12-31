package ofc.bot.util.content.annotations.commands;

import net.dv8tion.jda.api.Permission;
import ofc.bot.handlers.interactions.commands.SlashCommandsGateway;
import ofc.bot.handlers.interactions.commands.slash.SubcommandGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DiscordCommand {
    /**
     * The full qualified command name. If this is a subcommand or a subcommand in a
     * {@link SubcommandGroup SubcommandGroup}, you must provide the full command name.
     * <p>
     * See examples for {@code /permissions}, {@code /permissions channel}
     * and {@code /permissions channel view}:
     * <pre>
     *   {@code
     *      @DiscordCommand(name = "permissions")
     *      @DiscordCommand(name = "permissions channel")
     *      @DiscordCommand(name = "permissions channel view")
     *   }
     * </pre>
     * <p>
     * The usage of the {@code /} is optional and will be ignored,
     * you can use it for readability purposes if you want to.
     *
     * @return the executable slash command name.
     */
    String name();
    String description();

    /**
     * The permission the user must have in order to execute the command.
     * <p>
     * Keep in mind that subcommands and subcommand groups cannot have
     * any custom permissions and this field will be ignored if provided.
     *
     * <p>
     * The {@link SlashCommandsGateway SlashCommandsGateway}
     * will not take any actions to prevent undesired executions,
     * these permissions are handled by the Discord API.
     * <p>
     * Omit this field if you want to allow everyone to use this command.
     *
     * @return the required permission to run the command.
     */
    Permission permission() default Permission.UNKNOWN;

    /**
     * The amount of seconds that EACH user must wait before
     * using the same command again.
     *
     * @return The cooldown in seconds.
     */
    int cooldown() default 0;
}