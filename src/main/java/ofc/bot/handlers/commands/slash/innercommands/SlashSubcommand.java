package ofc.bot.handlers.commands.slash.innercommands;

import ofc.bot.util.content.annotations.commands.CommandGroup;
import ofc.bot.handlers.commands.slash.AbstractCommandData;

public abstract class SlashSubcommand extends AbstractCommandData {
    private final String subcommandGroupName;

    public SlashSubcommand() {
        this.subcommandGroupName = resolveGroupName();
    }

    public final String getSubcommandGroupName() {
        return this.subcommandGroupName;
    }

    public final boolean isInSubcommandGroup() {
        return subcommandGroupName != null;
    }

    private String resolveGroupName() {

        Class<? extends SlashSubcommand> clazz = this.getClass();
        CommandGroup groupAnnotation = clazz.getDeclaredAnnotation(CommandGroup.class);

        if (groupAnnotation == null)
            return null;

        String groupName = groupAnnotation.value();

        return groupName.isBlank()
                ? null
                : groupName;
    }
}