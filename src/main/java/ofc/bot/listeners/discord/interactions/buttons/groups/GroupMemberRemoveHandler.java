package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.events.entities.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ButtonHandler(
        scope = OficinaGroup.GROUP_MEMBER_REMOVE_BUTTON_SCOPE,
        autoResponseType = AutoResponseType.THINKING_EPHEMERAL
)
public class GroupMemberRemoveHandler implements BotButtonListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupMemberRemoveHandler.class);

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        Guild guild = ctx.getGuild();
        OficinaGroup group = ctx.get("group");
        long targetId = ctx.get("target_id");
        long ownerId = group.getOwnerId();
        long groupRoleId = group.getRoleId();
        Role groupRole = guild.getRoleById(groupRoleId);

        if (groupRole == null)
            return Status.GROUP_ROLE_NOT_FOUND;

        guild.removeRoleFromMember(UserSnowflake.fromId(targetId), groupRole).queue(v -> {
            ctx.reply(Status.MEMBER_SUCCESSFULLY_REMOVED_FROM_GROUP.args(targetId));

            dispatchGroupMemberRemoveEvent(group.getCurrency(), ownerId);
        }, (err) -> {
            LOGGER.error("Could not remove role &{} from member @{}", groupRoleId, targetId);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });
        ctx.disable();
        return Status.OK;
    }

    private void dispatchGroupMemberRemoveEvent(CurrencyType currency, long issuerId) {
        BankTransaction tr = new BankTransaction(issuerId, 0, currency, TransactionType.ITEM_SOLD, StoreItemType.GROUP_SLOT);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}
