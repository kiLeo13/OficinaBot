package ofc.bot.commands.groups;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.RentStatus;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;

@DiscordCommand(
        name = "group pay-invoice",
        description = "Pague a fatura do seu grupo (normalmente será apenas o aluguel).",
        cooldown = 5
)
public class GroupPayInvoiceCommand extends SlashSubcommand {
    private final OficinaGroupRepository grpRepo;

    public GroupPayInvoiceCommand(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        long userId = ctx.getUserId();
        Member issuer = ctx.getIssuer();
        OficinaGroup group = grpRepo.findByOwnerId(userId);

        if (group == null)
            return Status.YOU_DO_NOT_OWN_A_GROUP;

        if (group.getRentStatus() == RentStatus.NOT_PAID)
            return Status.YOU_CAN_NO_LONGER_PAY_THIS_INVOICE;

        int invoice = (int) group.calcCurrentInvoice();
        if (invoice == 0 || group.getRentStatus() == RentStatus.PAID)
            return Status.NO_PENDING_INVOICES;

        Button confirm = ButtonContextFactory.createInvoiceConfirm(group, invoice);
        MessageEmbed embed = EmbedFactory.embedInvoicePayment(issuer, group, invoice);

        return ctx.create()
                .setEmbeds(embed)
                .setActionRow(confirm)
                .send();
    }
}
