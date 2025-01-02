package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.GroupBot;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.events.entities.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.*;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@ButtonHandler(
        scope = OficinaGroup.GROUP_BOT_ADD_BUTTON_SCOPE,
        autoResponseType = AutoResponseType.THINKING
)
public class GroupBotAddHandler implements BotButtonListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupBotAddHandler.class);

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        Guild guild = ctx.getGuild();
        GroupBot bot = ctx.get("bot");
        OficinaGroup group = ctx.get("group");
        TextChannel textChan = group.getTextChannel();
        PaymentManager bank = PaymentManagerProvider.fromType(group.getCurrency());
        boolean isFree = group.hasFreeAccess();
        long ownerId = group.getOwnerId();
        long guildId = guild.getIdLong();
        int price = isFree ? 0 : StoreItemType.ADDITIONAL_BOT.getPrice();

        BankAction chargeAction = bank.charge(ownerId, guildId, 0, price, "Group bot added");
        if (!chargeAction.isOk()) {
            return Status.INSUFFICIENT_BALANCE;
        }

        if (textChan == null)
            return Status.YOUR_GROUP_DOES_NOT_HAVE_TEXT_CHANNEL;

        modifyChannelPermissions(textChan, bot.getBotId()).queue(v -> {
            ctx.reply(Status.GROUP_BOT_SUCCESSFULLY_ADDED.args(bot.getBotMention()));
            dispatchBotAddedEvent(group.getCurrency(), ownerId, price);
        }, (err) -> {
            LOGGER.error("Could not add bot to group {}", group.getId(), err);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });
        ctx.disable();
        return Status.OK;
    }

    private List<Permission> getChannelPermissions() {
        return Arrays.stream(Permission.values())
                .filter(Permission::isChannel)
                .toList();
    }

    private RestAction<?> modifyChannelPermissions(TextChannel channel, long botId) {
        return channel.getManager()
                .putMemberPermissionOverride(botId, getChannelPermissions(), null);
    }

    private void dispatchBotAddedEvent(CurrencyType currency, long userId, int price) {
        BankTransaction tr = new BankTransaction(userId, -price, currency, TransactionType.ITEM_BOUGHT, StoreItemType.ADDITIONAL_BOT);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}
