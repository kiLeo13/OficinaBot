package ofc.bot.handlers.commands.contexts;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import ofc.bot.handlers.commands.options.ArgumentMapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * An interface for commands that support options/arguments.
 *
 * @param <M> The type of option mapper, i.e: {@link OptionMapping} and {@link ArgumentMapper}.
 */
public interface OptionsContainer<M> {
    boolean hasOption(String name);

    boolean hasOptions();

    <T> T getOption(@NotNull String name, T fallback, @NotNull Function<? super M, ? extends T> resolver);

    <T extends Enum<T>> T getEnumOption(@NotNull String name, @NotNull Function<String, T> resolver);

    @Nullable
    default <T> T getOption(@NotNull String name, @NotNull Function<? super M, ? extends T> resolver) {
        return getOption(name, null, resolver);
    }

    default <T extends Enum<T>> T getEnumOption(@NotNull String name, T fallback, @NotNull Function<String, T> resolver) {
        T val = getEnumOption(name, resolver);
        return val == null ? fallback : val;
    }

    @NotNull
    default <T> List<T> collectOptions(@NotNull Function<? super M, T> resolver, @NotNull String... names) {
        return Arrays.stream(names)
                .map(name -> getOption(name, resolver))
                .filter(Objects::nonNull)
                .toList();
    }

    @NotNull
    default <T> T getSafeOption(@NotNull String name, @NotNull Function<? super M, ? extends T> resolver) {
        T data = getOption(name, resolver);

        if (data == null)
            throwNullOptionException(name);

        return data;
    }

    @Contract("_ -> fail")
    private void throwNullOptionException(String opt) {
        throw new IllegalStateException("The option \"" + opt + "\" was expected to be present but is missing. " +
                "This could be due to the option not being marked as required or a naming mismatch. " +
                "It is unlikely to be a Discord-related issue."
        );
    }
}