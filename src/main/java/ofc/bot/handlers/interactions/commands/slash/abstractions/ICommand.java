package ofc.bot.handlers.interactions.commands.slash.abstractions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.interactions.commands.contexts.OptionsContainer;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;

import java.util.List;

public interface ICommand<T extends OptionsContainer> {
    InteractionResult onSlashCommand(T ctx);

    /**
     * Returns the name identifying that command inside the structure,
     * see examples:
     * <ul>
     *   <li>{@code mod ban} will return {@code ban}.</li>
     *   <li>{@code mod ban add} will return {@code add}.</li>
     *   <li>{@code mod ban remove} will return {@code remove}.</li>
     *   <li>{@code mod ban view} will return {@code view}.</li>
     * </ul>
     *
     * @return the last argument of the command's qualified name.
     * @see #getQualifiedName()
     */
    default String getName() {
        String[] args = getQualifiedName().split(" ");
        return args[args.length - 1];
    }

    /**
     * The full command name, that is, if its a subcommand or a subcommand
     * in a subcommand group, the full qualified name is:
     * <ul>
     *   <li>{@code mod ban} not {@code ban}.</li>
     *   <li>{@code mod ban add} not {@code add}.</li>
     *   <li>{@code mod ban remove} not {@code remove}.</li>
     *   <li>{@code mod ban view} not {@code view}.</li>
     * </ul>
     *
     * @return the full qualified name.
     */
    String getQualifiedName();

    String getDescription();

    Permission getPermission();

    List<OptionData> getOptions();

    int getCooldown();

    /**
     * Determines the remaining cooldown time (in seconds) before the user can execute the same command again.
     * <p>
     * Negative values may be returned, indicating that the cooldown expectation has been exceeded.
     * For example, if the cooldown is set to 30s but the user has waited 40s, -10 will be returned.
     *
     * @param userId The user's ID attempting command usage.
     * @return The remaining cooldown time (in seconds).
     */
    long cooldownRemain(long userId);

    default boolean inCooldown(long userId) {
        return cooldownRemain(userId) > 0;
    }

    /**
     * Updates the rate-limit/cooldown of this command for the given user.
     * <p>
     * If the current command does not have any rate-limit,
     * then nothing should happen.
     *
     * @param userId The id of the user who ran the command.
     */
    void tickCooldown(long userId);
}