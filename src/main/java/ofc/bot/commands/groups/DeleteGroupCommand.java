package ofc.bot.commands.groups;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.BankTransactionRepository;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.awt.*;
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
        OficinaGroup group = grpRepo.findByOwnerId(userId);

        if (group == null)
            return Status.YOU_DO_NOT_OWN_A_GROUP;

        boolean isFree = group.hasFreeAccess();
        long amountSpent = calculateExpenses(group);
        int refund = isFree ? 0 : Math.round(-amountSpent * OficinaGroup.REFUND_PERCENT);
        MessageEmbed embed = embed(group, refund);

        Button confirmButton = ButtonContextFactory.createGroupDeletionConfirmationButton(userId, refund);
        return ctx.create()
                .setActionRow(confirmButton)
                .setEmbeds(embed)
                .send();
    }

    private MessageEmbed embed(OficinaGroup group, int refund) {
        EmbedBuilder builder = new EmbedBuilder();
        CurrencyType currency = group.getCurrency();
        Color color = new Color(255, 50, 50);

        return builder
                .setTitle(group.getName())
                .setDescription("Verifique as informações do grupo antes de apagá-lo.")
                .addField("💳 Economia", currency.getName(), true)
                .addField("💰 Reembolso", Bot.fmtNum(refund), true)
                .setColor(color)
                .build();
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
