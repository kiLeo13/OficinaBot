package ofc.bot.commands.groups;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.EntityContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.concurrent.TimeUnit;

@DiscordCommand(name = "group modify")
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

        int price = 0;
        int newColor = -1;

        if (newColorHex != null) {
            try {
                newColor = Integer.parseInt(newColorHex, 16);
            } catch (NumberFormatException e) {
                return Status.INVALID_COLOR_PROVIDED;
            }
        }

        if (!OficinaGroup.hasFreeAccess(issuer)) {
            if (newName != null) price += StoreItemType.UPDATE_GROUP.getPrice();
            if (newColorHex != null) price += StoreItemType.UPDATE_GROUP.getPrice();
        }

        Button confirmButton = EntityContextFactory.createModifyGroupConfirm(group, newName, newColor, price);
        MessageEmbed embed = EmbedFactory.embedGroupModify(issuer, group, newName, newColor, price);
        return ctx.create()
                .setActionRow(confirmButton)
                .setEmbeds(embed)
                .send();
    }

    @Override
    protected void init() {
        setDesc("Modifica dados do seu grupo, como nome e cor.");
        setCooldown(1, TimeUnit.MINUTES);

        addOpt(OptionType.STRING, "new-name", "O novo nome do grupo.", OficinaGroup.MIN_NAME_LENGTH, OficinaGroup.MAX_NAME_LENGTH);
        addOpt(OptionType.STRING, "new-color", "A nova cor do grupo.", 6, 6);
    }
}