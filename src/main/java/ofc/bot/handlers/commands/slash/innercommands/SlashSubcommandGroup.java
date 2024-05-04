package ofc.bot.handlers.commands.slash.innercommands;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import ofc.bot.handlers.commands.slash.containers.SubcommandContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SlashSubcommandGroup implements SubcommandContainer {
    private final String name;
    private final String description;
    private final Map<String, SlashSubcommand> subcommands = new HashMap<>();

    public SlashSubcommandGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public final void addSubcommand(SlashSubcommand cmd) {
        this.subcommands.put(cmd.getName(), cmd);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public final Collection<? extends SlashSubcommand> getSubcommands() {
        return this.subcommands.values()
                .stream()
                .toList();
    }

    @Override
    public final SlashSubcommand getSubcommand(String name) {
        return subcommands.get(name);
    }

    @Override
    public final boolean hasSubcommands() {
        return !subcommands.isEmpty();
    }

    public SubcommandGroupData getSubcommandGroupData() {

        return new SubcommandGroupData(this.name, this.description)
                .addSubcommands(getSubcommandsData());
    }
}
