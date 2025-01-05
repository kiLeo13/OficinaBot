package ofc.bot.commands.groups;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.BankTransactionRepository;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@DiscordCommand(name = "group delete", description = "Apaga o grupo criado por você.", cooldown = 10)
public class DeleteGroupCommand extends SlashSubcommand {
    private final BankTransactionRepository bankTrRepo;
    private final OficinaGroupRepository grpRepo;

    public DeleteGroupCommand(BankTransactionRepository bankTrRepo, OficinaGroupRepository grpRepo) {
        this.bankTrRepo = bankTrRepo;
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        long userId = ctx.getUserId();
        Member issuer = ctx.getIssuer();
        OficinaGroup group = grpRepo.findByOwnerId(userId);

        if (group == null)
            return Status.YOU_DO_NOT_OWN_A_GROUP;

        boolean isFree = group.hasFreeAccess();
        long amountSpent = calculateExpenses(group);
        int refund = isFree ? 0 : Math.round(-amountSpent * OficinaGroup.REFUND_PERCENT);

        Button confirmButton = ButtonContextFactory.createGroupDeletionConfirm(group, refund);
        MessageEmbed embed = EmbedFactory.embedGroupDelete(issuer, group, refund);
        return ctx.create()
                .setActionRow(confirmButton)
                .setEmbeds(embed)
                .send();
    }

    private long calculateExpenses(OficinaGroup group) {
        long timestamp = group.getTimeCreated() - 1;
        List<BankTransaction> items = bankTrRepo.findByItemTypesAndUserAfter(
                timestamp,
                group.getOwnerId(),
                TransactionType.ITEM_BOUGHT,
                StoreItemType.getGroupRefundable()
        );
        return items.stream()
                .mapToLong(BankTransaction::getAmount)
                .sum();
    }
}
