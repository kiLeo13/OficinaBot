package ofc.bot.handlers.commands.contexts.impl;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import ofc.bot.handlers.commands.contexts.OptionsContainer;
import ofc.bot.handlers.interactions.actions.impl.SlashRepliableAction;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class SlashCommandContext extends SlashRepliableAction implements OptionsContainer<OptionMapping> {

    public SlashCommandContext(SlashCommandInteraction itr) {
        super(itr);
    }

    @Override
    public void ack(boolean ephemeral) {
        if (!isAcknowledged())
            getSource().deferReply(ephemeral).queue();
    }

    @Override
    public boolean hasOption(String name) {
        return getSource().getOption(name) != null;
    }

    @Override
    public boolean hasOptions() {
        return !getSource().getOptions().isEmpty();
    }

    @Override
    @Contract("_, !null, _ -> !null")
    public <T> T getOption(@NotNull String name, T fallback, @NotNull Function<? super OptionMapping, ? extends T> resolver) {
        T option = getSource().getOption(name, resolver);
        return option == null
                ? fallback
                : option;
    }

    @Override
    public <T extends Enum<T>> T getEnumOption(@NotNull String name, @NotNull Function<String, T> resolver) {
        String val = getOption(name, OptionMapping::getAsString);
        return val == null ? null : resolver.apply(val);
    }

    @Override
    public String toString() {
        SlashCommandInteraction itr = getSource();
        List<OptionMapping> options = itr.getOptions();
        String prettyOptions = Bot.format(options, (opt) -> " :" + opt.getName());

        return "/" + itr.getName() + prettyOptions;
    }
}