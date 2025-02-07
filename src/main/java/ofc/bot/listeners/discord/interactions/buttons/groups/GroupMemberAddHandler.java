package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.events.impl.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.*;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ButtonHandler(
        scope = Scopes.Group.ADD_MEMBER,
        autoResponseType = AutoResponseType.THINKING
)
public class GroupMemberAddHandler implements BotButtonListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupMemberAddHandler.class);

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        Guild guild = ctx.getGuild();
        OficinaGroup group = ctx.get("group");
        Member newMember = ctx.get("new_member");
        int price = ctx.get("amount");
        long roleId = group.getRoleId();
        long ownerId = group.getOwnerId();
        long guildId = guild.getIdLong();
        PaymentManager bank = PaymentManagerProvider.fromType(group.getCurrency());
        Role groupRole = guild.getRoleById(roleId);

        if (groupRole == null)
            return Status.GROUP_ROLE_NOT_FOUND;

        BankAction chargeAction = bank.charge(ownerId, guildId, 0, price, "Group member added");
        if (!chargeAction.isOk()) {
            return Status.INSUFFICIENT_BALANCE;
        }

        guild.addRoleToMember(newMember, groupRole).queue(v -> {
            ctx.reply(Status.MEMBER_SUCCESSFULLY_ADDED_TO_GROUP.args(newMember.getAsMention()));

            dispatchGroupMemberAddEvent(group.getCurrency(), price, ownerId);
        }, (err) -> {
            LOGGER.error("Could not add role &{} to member @{}", roleId, newMember.getId());

            chargeAction.rollback();
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });
        ctx.disable();
        return Status.OK;
    }

    private void dispatchGroupMemberAddEvent(CurrencyType currency, int price, long buyerId) {
        BankTransaction tr = new BankTransaction(buyerId, -price, currency, TransactionType.ITEM_BOUGHT, StoreItemType.GROUP_SLOT);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}