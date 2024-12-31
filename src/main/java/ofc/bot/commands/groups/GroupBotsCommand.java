package ofc.bot.commands.groups;

import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

@DiscordCommand(name = "group bots", description = "Adicione bots ao seu grupo.")
public class GroupBotsCommand extends SlashSubcommand {

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        return ctx.reply("Em breve...", true);
    }
}