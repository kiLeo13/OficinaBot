package ofc.bot.commands.marriages.misc;

import ofc.bot.commands.marriages.misc.subcommands.Help;
import ofc.bot.commands.marriages.pagination.marriages.MarriageListSubcommand;
import ofc.bot.commands.marriages.misc.subcommands.Accept;
import ofc.bot.commands.marriages.misc.subcommands.CancelProposal;
import ofc.bot.commands.marriages.misc.subcommands.Reject;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.handlers.commands.slash.SlashCommand;

@DiscordCommand(name = "marriage", description = "Comandos gerais de utilidade para os casamentos.")
public class BaseMarriage extends SlashCommand {

    public BaseMarriage() {
        super(
                new Help(),
                new Accept(),
                new Reject(),
                new CancelProposal(),
                new MarriageListSubcommand()
        );
    }
}