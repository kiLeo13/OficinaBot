package ofc.bot.handlers.interactions.commands.slash;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.slash.abstractions.ICommand;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;

import java.util.*;

public class SlashCommandsRegistryManager {
    private static final Map<String, ICommand<SlashCommandContext>> commands = new HashMap<>(Commands.MAX_SLASH_COMMANDS);

    public static ICommand<SlashCommandContext> getCommand(String fullName) {
        return commands.get(fullName);
    }

    public static void register(List<SlashCommand> cmds) {
        commands.putAll(mapExecutables(cmds));
    }

    private static Map<String, ICommand<SlashCommandContext>> mapExecutables(List<SlashCommand> cmds) {
        Map<String, ICommand<SlashCommandContext>> mappedCommands = new HashMap<>();

        for (SlashCommand cmd : cmds) {
            if (!cmd.hasSubs()) {
                mappedCommands.put(cmd.getQualifiedName(), cmd);
                continue;
            }

            for (SlashSubcommand sub : cmd.findAllSubcommands()) {
                mappedCommands.put(sub.getQualifiedName(), sub);
            }
        }
        return Collections.unmodifiableMap(mappedCommands);
    }
}