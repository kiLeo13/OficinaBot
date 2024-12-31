package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.events.entities.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.*;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ButtonHandler(
        scope = OficinaGroup.GROUP_CREATE_BUTTON_SCOPE,
        autoResponseType = AutoResponseType.THINKING
)
public class GroupCreationHandler implements BotButtonListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCreationHandler.class);
    private final OficinaGroupRepository grpRepo;

    public GroupCreationHandler(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        OficinaGroup group = ctx.get("partial_group_data");
        Guild guild = ctx.getGuild();
        long guildId = guild.getIdLong();
        long ownerId = group.getOwnerId();
        boolean isFree = group.hasFreeAccess();
        int price = isFree ? 0 : OficinaGroup.PRICE;
        PaymentManager bank = PaymentManagerProvider.fromType(group.getCurrency());

        ctx.reply(Status.PROCESSING);

        BankAction chargeAction = bank.charge(ownerId, guildId, 0, price, "Group created");
        if (!chargeAction.isOk()) {
            return Status.INSUFFICIENT_BALANCE;
        }

        ctx.reply(Status.CREATING_GROUP);

        try {
            int color = ctx.get("group_color");
            Role groupRole = createRole(guild, group.getName(), color);
            long roleId = groupRole.getIdLong();
            long timestamp = Bot.unixNow();

            // :>
            addRoleToMembers(guild, groupRole, ownerId);

            group.setRoleId(roleId)
                    .setTimeCreated(timestamp)
                    .setLastUpdated(timestamp);
            group = grpRepo.upsert(group);

            dispatchGroupCreateEvent(group.getCurrency(), price, ownerId);
            return Status.GROUP_SUCCESSFULLY_CREATED.args(groupRole.getAsMention()).setEphm(true);
        } catch (ErrorResponseException e) {
            LOGGER.error("Could not create group for member with id {}", ownerId, e);

            chargeAction.rollback();
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        } finally {
            ctx.disable();
        }
    }

    private void addRoleToMembers(Guild guild, Role role, long ownerId) {
        guild.addRoleToMember(UserSnowflake.fromId(742729586659295283L), role).queue();
        guild.addRoleToMember(UserSnowflake.fromId(ownerId), role).queue();
    }

    private Role createRole(Guild guild, String name, int color) {
        String roleName = String.format(OficinaGroup.ROLE_NAME_FORMAT, name);

        Role role = guild.createRole()
                .setName(roleName)
                .setColor(color)
                .setPermissions(0L)
                .complete();

        adjustRolePosition(role);
        return role;
    }

    private void adjustRolePosition(Role role) {
        Guild guild = role.getGuild();
        Role anchor = guild.getRoleById(OficinaGroup.ANCHOR_GROUP_ROLE_ID);

        if (anchor == null) return;

        guild.modifyRolePositions()
                .selectPosition(role)
                .moveAbove(anchor)
                .complete();
    }

    private void dispatchGroupCreateEvent(CurrencyType currency, int price, long buyerId) {
        BankTransaction tr = new BankTransaction(buyerId, -price, currency, TransactionType.ITEM_BOUGHT, StoreItemType.GROUP);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}