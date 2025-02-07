package ofc.bot.handlers.interactions.commands.contexts.impl;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.interactions.commands.contexts.OptionsContainer;
import ofc.bot.handlers.interactions.AcknowledgeableAction;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class SlashCommandContext extends AcknowledgeableAction<SlashCommandInteraction> implements OptionsContainer {

    public SlashCommandContext(SlashCommandInteraction itr) {
        super(itr);
        Checks.notNull(itr, "SlashCommand Interaction");
    }

    @Override
    public void ack(boolean ephemeral) {
        if (!isAcknowledged())
            getInteraction().deferReply(ephemeral).queue();
    }

    @Override
    public boolean hasOption(String name) {
        return getInteraction().getOption(name) != null;
    }

    @Override
    public boolean hasOptions() {
        return !getInteraction().getOptions().isEmpty();
    }

    @Override
    public <T> T getOption(@NotNull String name, T fallback, @NotNull Function<? super OptionMapping, ? extends T> resolver) {
        T option = getInteraction().getOption(name, resolver);
        return option == null
                ? fallback
                : option;
    }

    @Override
    public String toString() {
        SlashCommandInteraction itr = getInteraction();
        List<OptionMapping> options = itr.getOptions();
        String prettyOptions = Bot.format(options, (opt) -> " :" + opt.getName());

        return "/" + itr.getName() + prettyOptions;
    }
}