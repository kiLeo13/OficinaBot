package ofc.bot.handlers.interactions.commands.slash;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;

import java.util.ArrayList;
import java.util.List;

public class SubcommandGroup {
    private final String name;
    private final String description;
    private final List<SlashSubcommand> subcommands;
    private SlashCommand superCommand;

    public SubcommandGroup(String name, String description) {
        this.name = name;
        this.description = description;
        this.subcommands = new ArrayList<>(CommandData.MAX_OPTIONS);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public SubcommandGroup addSubcommand(SlashSubcommand sub) {
        this.subcommands.add(sub);
        return this;
    }

    public List<? extends SlashSubcommand> getSubcommands() {
        return this.subcommands;
    }

    public SubcommandGroup setSuperCommand(SlashCommand superCommand) {
        this.superCommand = superCommand;
        return this;
    }

    public SlashCommand getSuperCommand() {
        return this.superCommand;
    }

    public SubcommandGroupData build() {
        SubcommandGroupData group = new SubcommandGroupData(getName(), getDescription());

        for (SlashSubcommand sub : subcommands) {
            group.addSubcommands(sub.build());
        }
        return group;
    }
}