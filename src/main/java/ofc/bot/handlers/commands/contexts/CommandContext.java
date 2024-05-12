package ofc.bot.handlers.commands.contexts;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.TimeUtil;
import ofc.bot.handlers.commands.exceptions.CommandExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public interface CommandContext extends IAcknowledgeable<SlashCommandInteraction> {

    /**
     * The id of the {@link SlashCommandInteraction}.
     *
     * @return The id of the interaction.
     */
    long getId();

    @NotNull
    MessageChannelUnion getChannel();

    @NotNull
    default TextChannel getTextChannel() {
        return getChannel().asTextChannel();
    }

    @NotNull
    Member getIssuer();

    @NotNull
    User getUser();

    @NotNull
    Guild getGuild();

    @NotNull
    default OffsetDateTime getTimeCreated() {
        return TimeUtil.getTimeCreated(this::getId);
    }

    default boolean hasOption(String name) {
        return getInteraction().getOption(name) != null;
    }

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

        if (data == null)
            throw new CommandExecutionException("Option \"" + name + "\" cannot be omitted or invalid");

        return data;
    }
}