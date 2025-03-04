package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.RentStatus;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.events.impl.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.*;
import ofc.bot.handlers.games.betting.BetManager;
import ofc.bot.handlers.interactions.AutoResponseType;
import ofc.bot.handlers.interactions.InteractionListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Bot;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.InteractionHandler;
import org.jooq.exception.DataAccessException;

@InteractionHandler(scope = Scopes.Group.PAY_INVOICE, autoResponseType = AutoResponseType.THINKING)
public class GroupInvoicePaymentHandler implements InteractionListener<ButtonClickContext> {
    private static final BetManager betManager = BetManager.getManager();
    private final OficinaGroupRepository grpRepo;

    public GroupInvoicePaymentHandler(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onExecute(ButtonClickContext ctx) {
        OficinaGroup group = ctx.get("group");
        CurrencyType currency = group.getCurrency();
        PaymentManager bank = PaymentManagerProvider.fromType(currency);
        User user = ctx.getUser();
        long guildId = ctx.getGuildId();
        long ownerId = ctx.getUserId();
        int value = ctx.get("amount");

        if (betManager.isBetting(ownerId))
            return Status.YOU_CANNOT_DO_THIS_WHILE_BETTING;

        BankAction chargeAction = bank.charge(ownerId, guildId, 0, value, "Group invoice payment");
        if (!chargeAction.isOk())
            return Status.INSUFFICIENT_BALANCE;

        group.setInvoiceAmount(0)
                .setRentStatus(RentStatus.PAID)
                .tickUpdate();

        try {
            grpRepo.upsert(group);
            dispatchInvoicePaymentEvent(currency, ownerId, value);
            sendPaymentConfirmation(user, value);

            return Status.INVOICE_SUCCESSFULLY_PAID.args(Bot.fmtMoney(value));
        } catch (DataAccessException e) {
            chargeAction.rollback();
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private void sendPaymentConfirmation(User user, int amount) {
        String msg = getConfirmationMessage(user, amount);
        user.openPrivateChannel()
                .flatMap(chan -> chan.sendMessage(msg))
                .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
    }

    private String getConfirmationMessage(User user, int amount) {
        return String.format("Olá, %s! Gostaria de informar que recebemos o pagamento da sua fatura de %s! Obrigado 😊",
                user.getEffectiveName(), Bot.fmtMoney(amount));
    }

    private void dispatchInvoicePaymentEvent(CurrencyType curr, long userId, int amount) {
        BankTransaction tr = new BankTransaction(userId, amount, curr, TransactionType.INVOICE_PAID);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}
