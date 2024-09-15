package ofc.bot.store.commands;

import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

// @DiscordCommand(name = "store", description = "Mostra os itens disponíveis na loja.")
public class StoreCommand extends SlashCommand {

    @Override
    public CommandResult onCommand(CommandContext ctx) {


        return Status.PASSED;
    }
}