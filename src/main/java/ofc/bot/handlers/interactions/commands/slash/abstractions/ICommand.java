package ofc.bot.handlers.interactions.commands.slash.abstractions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.interactions.commands.Cooldown;
import ofc.bot.handlers.interactions.commands.contexts.OptionsContainer;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
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
    @NotNull
    String getQualifiedName();

    @NotNull
    String getDescription();

    @Nullable
    Permission getPermission();

    @NotNull
    List<OptionData> getOptions();

    @NotNull
    Cooldown getCooldown();

    default boolean hasCooldown() {
        return getCooldown().isZero();
    }
}