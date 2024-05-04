package ofc.bot.handlers.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.Main;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommandGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommandsRegistryManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandsRegistryManager.class);
    private static final Map<String, SlashCommand> commands = new HashMap<>(100);

    public static SlashCommand getCommand(String name) {
        return commands.get(name);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private static void registerCommands(Map<String, SlashCommand> commands) {
        CommandsRegistryManager.commands.putAll(commands);

        upsertCommands();
    }

    private static void upsertCommands() {

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
            registerCommands(this.mappedCommands);
        }
    }
}