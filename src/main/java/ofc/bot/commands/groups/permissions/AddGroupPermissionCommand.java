package ofc.bot.commands.groups.permissions;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(name = "group permissions add", description = "Compre algum nível de permissão para o seu grupo.")
public class AddGroupPermissionCommand extends SlashSubcommand {
    private final OficinaGroupRepository grpRepo;

    public AddGroupPermissionCommand(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        return null;
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                
        );
    }
}