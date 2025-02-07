package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.entities.Guild;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.GroupPermission;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.EntityPolicyRepository;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.events.impl.BankTransactionEvent;
import ofc.bot.handlers.economy.*;
import ofc.bot.handlers.groups.permissions.GroupPermissionManager;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;

@ButtonHandler(scope = Scopes.Group.ADD_PERMISSION, autoResponseType = AutoResponseType.THINKING)
public class GroupPermissionAddHandler implements BotButtonListener {
    private final GroupPermissionManager permissionManager;

    public GroupPermissionAddHandler(EntityPolicyRepository policyRepo) {
        this.permissionManager = new GroupPermissionManager(policyRepo);
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        int price = ctx.get("amount");
        OficinaGroup group = ctx.get("group");
        GroupPermission perm = ctx.get("permission");
        CurrencyType currency = group.getCurrency();
        PaymentManager bank = PaymentManagerProvider.fromType(currency);
        Guild guild = ctx.getGuild();
        long ownerId = group.getOwnerId();
        long guildId = guild.getIdLong();

        BankAction chargeAction = bank.charge(ownerId, guildId, 0, price, "Group Permission Added");
        if (!chargeAction.isOk()) {
            return Status.INSUFFICIENT_BALANCE;
        }

        boolean success = permissionManager.grant(perm, group);
        if (!success) {
            chargeAction.rollback();
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
        ctx.disable();
        dispatchPermissionAddEvent(ownerId, currency, price);
        return Status.GROUP_PERMISSION_GRANTED_SUCESSFULLY.args(perm.getDisplay());
    }

    private void dispatchPermissionAddEvent(long userId, CurrencyType currency, int amount) {
        BankTransaction tr = new BankTransaction(userId, -amount, currency, TransactionType.ITEM_BOUGHT, StoreItemType.GROUP_PERMISSION);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}