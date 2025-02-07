package ofc.bot.commands.exclusions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(name = "exclusion remove", description = "Remova exceções das funções do bot.", permission = Permission.MANAGE_SERVER)
public class RemoveExclusionCommand extends SlashSubcommand {

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        return null;
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }
}