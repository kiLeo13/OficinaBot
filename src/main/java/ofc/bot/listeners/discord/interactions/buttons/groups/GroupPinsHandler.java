package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.RestAction;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.events.impl.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.*;
import ofc.bot.handlers.interactions.AutoResponseType;
import ofc.bot.handlers.interactions.InteractionListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.InteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@InteractionHandler(scope = Scopes.Group.PIN_MESSAGE, autoResponseType = AutoResponseType.THINKING)
public class GroupPinsHandler implements InteractionListener<ButtonClickContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupPinsHandler.class);
    private static final int MAX_PINNED_MESSAGES = 50;

    @Override
    public InteractionResult onExecute(ButtonClickContext ctx) {
        long userId = ctx.getUserId();
        long guildId = ctx.getGuildId();
        long msgId = ctx.get("message_id");
        int price = ctx.get("amount");
        boolean shouldPin = ctx.get("is_pin");
        OficinaGroup group = ctx.get("group");
        TextChannel chan = group.getTextChannel();
        CurrencyType currency = group.getCurrency();
        PaymentManager bank = PaymentManagerProvider.fromType(currency);

        if (chan == null)
            return Status.YOUR_GROUP_DOES_NOT_HAVE_TEXT_CHANNEL;

        Message msg;
        try {
            msg = chan.retrieveMessageById(msgId).complete();
        } catch (Exception e) {
            return parseException(e);
        }

        if (shouldPin) {
            List<Message> msgs = chan.retrievePinnedMessages().complete();
            if (msgs.size() >= MAX_PINNED_MESSAGES)
                return Status.HIT_MAX_PINNED_MESSAGES;
        }

        boolean isPinned = msg.isPinned();
        if (isPinned && shouldPin)
            return Status.MESSAGE_ALREADY_PINNED;

        if (!isPinned && !shouldPin)
            return Status.MESSAGE_ALREADY_UNPINNED;

        BankAction chargeAction = bank.charge(userId, guildId, 0, price, "Pin Group Message");
        if (!chargeAction.isOk())
            return Status.INSUFFICIENT_BALANCE;

        getAction(msg, shouldPin).queue(v -> {
            if (shouldPin) {
                ctx.reply(Status.MESSAGE_SUCCESSFULLY_PINNED);
                dispatchPinEvent(currency, userId, price);
            } else
                ctx.reply(Status.MESSAGE_SUCCESSFULLY_UNPINNED);
        }, err -> {
            LOGGER.error("Could not pin message", err);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });
        ctx.disable();
        return Status.OK;
    }

    private RestAction<?> getAction(Message msg, boolean shouldPin) {
        return shouldPin ? msg.pin() : msg.unpin();
    }

    private Status parseException(Exception e) {
        if (!(e instanceof ErrorResponseException erresp)) {
            LOGGER.error("Could not find message to be pinned", e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }

        return switch (erresp.getErrorResponse()) {
            case UNKNOWN_CHANNEL -> Status.CHANNEL_NOT_FOUND;
            case UNKNOWN_MESSAGE -> Status.MESSAGE_NOT_FOUND;
            default -> Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        };
    }

    private void dispatchPinEvent(CurrencyType curr, long userId, int amount) {
        BankTransaction tr = new BankTransaction(userId, amount, curr, TransactionType.ITEM_BOUGHT, StoreItemType.PIN_MESSAGE);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}
