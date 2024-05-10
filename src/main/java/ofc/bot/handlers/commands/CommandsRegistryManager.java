package ofc.bot.handlers.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.Main;
import ofc.bot.handlers.commands.slash.AbstractCommandData;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommandGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommandsRegistryManager {
    private static CommandsRegistryManager instance;
    private static final Logger logger = LoggerFactory.getLogger(CommandsRegistryManager.class);
    private static final Map<String, SlashCommand> commands = new HashMap<>(100);

    private CommandsRegistryManager() {}

    public static CommandsRegistryManager getManager() {
        if (instance == null) instance = new CommandsRegistryManager();
        return instance;
    }

    public SlashCommand getCommand(String name) {
        return commands.get(name);
    }

    /**
     * This method returns the {@link AbstractCommandData} instance of a command
     * by it's full name, for example, you can provide the following String:
     * {@code permissions member add} and if the command exists in the map, an instance of it
     * will be returned.
     * <p>
     * <b><u>Never</u></b> pass command names with any other characters other than the name,
     * or provide multiple spaces between two arguments.
     * Ex:
     * <p>
     * Wrong: {@code /voice mute}.
     * <p>
     * Correct: {@code voice mute}.
     *
     * @param fullName The full command name, found at {@link Command#getFullCommandName()}.
     * @return The command instance found, {@code null} otherwise.
     */
    public AbstractCommandData resolveCommand(String fullName) {

        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("Cannot search for null or empty command name");

        String[] names = fullName.split(" ");

        SlashCommand cmd = getCommand(names[0]);

        if (cmd == null || names.length == 1)
            return cmd;

        return names.length == 2
                ? cmd.getSubcommand(names[1])
                : cmd.getSubcommand(names[1], names[2]);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private void registerCommands(Map<String, SlashCommand> commands) {
        CommandsRegistryManager.commands.putAll(commands);

        upsertCommands();
    }

    private void upsertCommands() {

        JDA api = Main.getApi();
        Collection<SlashCommand> slashCommands = commands.values();
        List<SlashCommandData> slashCommandsData = slashCommands.stream()
                .map(SlashCommand::toCommandData)
                .toList();

        for (SlashCommand slash : slashCommands)
            CommandPrinter.prettyCommand(slash);

        api.updateCommands().addCommands(slashCommandsData).queue((slashCmds) -> {

            logger.info("Successfully created/updated a total of {} commands!", slashCmds.size());
        }, (e) -> {

            logger.error("Could not upsert Application Commands", e);
        });
    }

    private static class CommandPrinter {

        public static void prettyCommand(SlashCommand slash) {

            String name = slash.getName();
            String prettyOptions = prettyOptions(slash.getOptions());

            logger.info("[✔] Successfully registered Command: /{} {}", name, prettyOptions);

            if (slash.hasSubcommands())
                CommandPrinter.prettySubcommand(slash);

            if (slash.hasSubcommandGroups())
                CommandPrinter.prettySubcommandGroups(slash);
        }

        private static void prettySubcommand(SlashCommand slash) {

            String superCommandName = slash.getName();
            Collection<? extends SlashSubcommand> subs = slash.getSubcommands();

            for (SlashSubcommand sub : subs) {

                String subName = sub.getName();
                String prettyOptions = prettyOptions(sub.getOptions());

                logger.info(" └─ /{} {} {}", superCommandName, subName, prettyOptions);
            }
        }

        private static void prettySubcommandGroups(SlashCommand slash) {

            String superCommandName = slash.getName();
            Collection<? extends SlashSubcommandGroup> groups = slash.getSubcommandGroups();

            for (SlashSubcommandGroup group : groups) {

                Collection<? extends SlashSubcommand> subcommands = group.getSubcommands();

                for (SlashSubcommand sub : subcommands) {

                    String groupName = group.getName();
                    String subName = sub.getName();
                    String prettyOptions = prettyOptions(sub.getOptions());
                    String indent = slash.hasSubcommands() ? "  " : " ";

                    logger.info("{}└─ /{} {} {} {}", indent, superCommandName, groupName, subName, prettyOptions);
                }
            }
        }

        private static String prettyOptions(List<OptionData> options) {

            StringBuilder builder = new StringBuilder();

            for (OptionData opt : options) {

                if (opt.isRequired())
                    builder.append(" <")
                            .append(opt.getName())
                            .append(">");
                else
                    builder.append(" [")
                            .append(opt.getName())
                            .append("]");
            }

            return builder.toString().strip();
        }
    }

    public static class Builder {
        private final Map<String, SlashCommand> mappedCommands;

        private Builder() {
            this.mappedCommands = new HashMap<>(100);
        }

        public void addCommand(SlashCommand command) {

            Checks.notNull(command, "Slash Command");

            this.mappedCommands.put(command.getName(), command);
        }

        public void commit() {
            getManager().registerCommands(this.mappedCommands);
        }
    }
}