package ofc.bot.commands.additionals;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;
import ofc.bot.handlers.interactions.EntityContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

@DiscordCommand(name = "additionals roles")
public class AdditionalRolesCommand extends SlashSubcommand {

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        long chanId = ctx.getSafeOption("channel", OptionMapping::getAsChannel).getIdLong();
        Message.Attachment img = ctx.getOption("attachment", OptionMapping::getAsAttachment);
        String hexColor = ctx.getOption("color", OptionMapping::getAsString);
        int maxChoices = ctx.getOption("max-choices", -1, OptionMapping::getAsInt);

        int color = 0;
        try {
            color = hexColor == null ? color : Integer.parseInt(hexColor, 16);
        } catch (NumberFormatException e) {
            return Status.INVALID_COLOR_PROVIDED;
        }

        Modal modal = EntityContextFactory.createChoosableRolesModal(img, chanId, color, maxChoices);
        return ctx.replyModal(modal);
    }

    @Override
    protected void init() {
        setDesc("Configura uma mensagem para membros ganharem cargos adicionais.");

        addOpt(OptionType.CHANNEL, "channel", "O canal a ser enviado a mensagem do menu.", true,
                (it) -> it.setChannelTypes(ChannelType.TEXT));

        addOpt(OptionType.ATTACHMENT, "attachment", "A imagem/banner da embed.");
        addOpt(OptionType.STRING, "color", "A cor da embed.", 6, 6);
        addOpt(OptionType.INTEGER, "max-choices", "Quantas opções o usuário pode, no máximo, escolher.", 1, SelectMenu.OPTIONS_MAX_AMOUNT);
    }
}