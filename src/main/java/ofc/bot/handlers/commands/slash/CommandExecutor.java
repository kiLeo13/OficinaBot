package ofc.bot.handlers.commands.slash;

import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;

public interface CommandExecutor {

    CommandResult onCommand(CommandContext ctx);
}