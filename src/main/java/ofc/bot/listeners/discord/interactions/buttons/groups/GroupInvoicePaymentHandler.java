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
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Bot;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import org.jooq.exception.DataAccessException;

@ButtonHandler(scope = Scopes.Group.PAY_INVOICE, autoResponseType = AutoResponseType.THINKING)
public class GroupInvoicePaymentHandler implements BotButtonListener {
    private final OficinaGroupRepository grpRepo;

    public GroupInvoicePaymentHandler(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        long userId = ctx.getUserId();
        long guildId = ctx.getGuildId();
        int amount = ctx.get("amount");
        User user = ctx.getUser();
        OficinaGroup gp = ctx.get("group");
        CurrencyType currency = gp.getCurrency();
        PaymentManager bank = PaymentManagerProvider.fromType(currency);

        BankAction chargeAction = bank.charge(userId, guildId, 0, amount, "Group invoice payment");
        if (!chargeAction.isOk())
            return Status.INSUFFICIENT_BALANCE;

        gp.setInvoiceAmount(0)
                .setRentStatus(RentStatus.PAID)
                .tickUpdate();

        try {
            grpRepo.upsert(gp);
            dispatchInvoicePaymentEvent(currency, userId, amount);
            sendPaymentConfirmation(user, amount);

            return Status.INVOICE_SUCCESSFULLY_PAID.args(Bot.fmtMoney(amount));
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
