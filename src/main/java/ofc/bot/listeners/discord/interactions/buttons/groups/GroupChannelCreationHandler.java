package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
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
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ButtonHandler(
        scope = OficinaGroup.GROUP_CHANNEL_CREATE_BUTTON_SCOPE,
        autoResponseType = AutoResponseType.THINKING
)
public class GroupChannelCreationHandler implements BotButtonListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupChannelCreationHandler.class);
    private final OficinaGroupRepository grpRepo;

    public GroupChannelCreationHandler(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        OficinaGroup group = ctx.get("group");
        Category category = ctx.get("category");
        StoreItemType itemType = ctx.get("item_type");
        PaymentManager bank = PaymentManagerProvider.fromType(group.getCurrency());
        Guild guild = ctx.getGuild();
        boolean isFree = group.hasFreeAccess();
        long ownerId = group.getOwnerId();
        long guildId = guild.getIdLong();
        int price = isFree ? 0 : itemType.getPrice();

        BankAction chargeAction = bank.charge(ownerId, guildId, 0, price, "Group channel created");
        if (!chargeAction.isOk()) {
            return Status.INSUFFICIENT_BALANCE;
        }

        try {
            MessageChannel channel = createChannel(group.getRoleId(), itemType, category,
                    itemType == StoreItemType.GROUP_TEXT_CHANNEL ? group.getTextChannelName() : group.getVoiceChannelName()
            );

            if (itemType == StoreItemType.GROUP_TEXT_CHANNEL)
                group.setTextChannelId(channel.getIdLong()).tickUpdate();
            else
                group.setVoiceChannelId(channel.getIdLong()).tickUpdate();

            grpRepo.upsert(group);

            dispatchChannelBoughtEvent(group.getCurrency(), price, itemType, ownerId);
            return Status.GROUP_CHANNEL_SUCCESSFULLY_CREATED.args(channel.getAsMention()).setEphm(true);
        } catch (ErrorResponseException e) {
            LOGGER.error("Could not create channel for group with id {}", group.getId(), e);

            chargeAction.rollback();
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        } finally {
            ctx.disable();
        }
    }

    private MessageChannel createChannel(long groupRoleId, StoreItemType type, Category category, String name) {
        Guild guild = category.getGuild();
        long publicRoleID = guild.getPublicRole().getIdLong();
        return type == StoreItemType.GROUP_TEXT_CHANNEL
                ? createTextChannel(groupRoleId, publicRoleID, category, name)
                : createVoiceChannel(groupRoleId, publicRoleID, category, name);
    }

    private MessageChannel createTextChannel(long roleId, long publicRoleId, Category category, String name) {
        return category.createTextChannel(name)
                .addRolePermissionOverride(publicRoleId, null, List.of(Permission.VIEW_CHANNEL))
                .addRolePermissionOverride(roleId, OficinaGroup.PERMS_ALLOW_TEXT_CHANNEL, null)
                .complete();
    }

    private MessageChannel createVoiceChannel(long roleId, long publicRoleId, Category category, String name) {
        return category.createVoiceChannel(name)
                .addRolePermissionOverride(publicRoleId, null, List.of(Permission.VOICE_CONNECT))
                .addRolePermissionOverride(roleId, OficinaGroup.PERMS_ALLOW_VOICE_CHANNEL, null)
                .setUserlimit(OficinaGroup.DEFAULT_VOICE_USERS_LIMIT)
                .complete();
    }

    private void dispatchChannelBoughtEvent(CurrencyType currencyType, int price, StoreItemType item, long buyerId) {
        BankTransaction tr = new BankTransaction(buyerId, -price, currencyType, TransactionType.ITEM_BOUGHT, item);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}