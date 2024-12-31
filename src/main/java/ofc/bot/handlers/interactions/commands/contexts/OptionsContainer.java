package ofc.bot.handlers.interactions.commands.contexts;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public interface OptionsContainer {
    boolean hasOption(String name);

    boolean hasOptions();

    <T> T getOption(String name, T fallback, Function<? super OptionMapping, ? extends T> resolver);

    @Nullable
    default <T> T getOption(String name, Function<? super OptionMapping, ? extends T> resolver) {
        return getOption(name, null, resolver);
    }

    @NotNull
    default <T> List<T> collectOptions(Function<? super OptionMapping, T> resolver, String... names) {
        return Arrays.stream(names)
                .map(name -> getOption(name, resolver))
                .filter(Objects::nonNull)
                .toList();
    }

    @NotNull
    default <T> T getSafeOption(String name, Function<? super OptionMapping, ? extends T> resolver) {
        T data = getOption(name, resolver);

        if (data == null) throw new IllegalStateException("Option \"" + name + "\" cannot be omitted or invalid");

        return data;
    }

    default <T extends Enum<T>> T getEnumOption(String name, T fallback, Class<T> type) {
        T opt = getEnumOption(name, type);
        return opt == null ? fallback : opt;
    }

    @NotNull
    default <T extends Enum<T>> T getSafeEnumOption(String name, Class<T> type) {
        String opt = getSafeOption(name, OptionMapping::getAsString);
        T value = getEnumOption(name, type);

        if (value == null)
            throw new IllegalStateException(
                    "Could not find a valid option for argument \"" + name + "\", value " + opt +
                            " is not in " + Arrays.toString(type.getEnumConstants())
            );

        return value;
    }

    @Nullable
    default <T extends Enum<T>> T getEnumOption(String name, Class<T> type) {
        String opt = getOption(name, OptionMapping::getAsString);

        if (opt == null) return null;

        try {
            return Enum.valueOf(type, opt);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}