package ofc.bot.commands.groups;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(name = "group modify", description = "Modifica dados do seu grupo, como nome e cor.", cooldown = 60)
public class ModifyGroupCommand extends SlashSubcommand {
    private final OficinaGroupRepository grpRepo;

    public ModifyGroupCommand(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Member issuer = ctx.getIssuer();
        String newName = ctx.getOption("new-name", OptionMapping::getAsString);
        String newColorHex = ctx.getOption("new-color", OptionMapping::getAsString);
        long userId = ctx.getUserId();
        boolean isEmpty = !ctx.hasOptions();

        if (isEmpty)
            return Status.NOTHING_CHANGED_WITH_REASON.args("nenhum argumento foi fornecido");

        OficinaGroup group = grpRepo.findByOwnerId(userId);

        if (group == null)
            return Status.YOU_DO_NOT_OWN_A_GROUP;

        int cost = 0;
        int newColor = -1;

        if (newColorHex != null) {
            try {
                newColor = Integer.parseInt(newColorHex, 16);
            } catch (NumberFormatException e) {
                return Status.INVALID_COLOR_PROVIDED;
            }
        }

        if (!OficinaGroup.hasFreeAccess(issuer)) {
            if (newName != null) cost += StoreItemType.UPDATE_GROUP.getPrice();
            if (newColorHex != null) cost += StoreItemType.UPDATE_GROUP.getPrice();
        }

        Button confirmButton = ButtonContextFactory.createModifyGroupConfirmationButton(group, newName, newColor, cost);
        return ctx.create()
                .setContent(Status.CONFIRM_GROUP_UPDATE)
                .setActionRow(confirmButton)
                .send();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "new-name", "O novo nome do grupo.")
                        .setRequiredLength(OficinaGroup.MIN_NAME_LENGTH, OficinaGroup.MAX_NAME_LENGTH),

                new OptionData(OptionType.STRING, "new-color", "A nova cor do grupo.")
                        .setRequiredLength(6, 6)
        );
    }
}