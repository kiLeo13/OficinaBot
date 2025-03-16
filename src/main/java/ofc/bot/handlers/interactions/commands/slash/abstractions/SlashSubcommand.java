package ofc.bot.handlers.interactions.commands.slash.abstractions;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import ofc.bot.handlers.interactions.commands.slash.SubcommandGroup;

public abstract class SlashSubcommand extends AbstractSlashCommand {
    private SlashCommand superCommand;
    private SubcommandGroup group;

    public SubcommandGroup getGroup() {
        return this.group;
    }

    public SlashSubcommand setGroup(SubcommandGroup group) {
        this.group = group;
        return this;
    }

    public SlashSubcommand setSuperCommand(SlashCommand superCommand) {
        this.superCommand = superCommand;
        return this;
    }

    public SlashCommand getSuperCommand() {
        return this.superCommand;
    }

    public SubcommandData build() {
        return new SubcommandData(getName(), getDescription())
                .addOptions(getOptions());
    }
}