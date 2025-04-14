package ofc.bot.util;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.GroupPerk;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.GroupPerkRepository;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.events.impl.BankTransactionEvent;
import ofc.bot.handlers.economy.CurrencyType;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupHelper.class);
    private static GroupPerkRepository grpPerkRepo;

    private GroupHelper() {}

    public static void registerBotAdded(OficinaGroup group, int price) {
        registerPurchase(group, StoreItemType.ADDITIONAL_BOT, price);
    }

    public static void registerChannelCreated(OficinaGroup group, ChannelType type, int price) {
        StoreItemType item = type == ChannelType.TEXT ?
                StoreItemType.GROUP_TEXT_CHANNEL
                : StoreItemType.GROUP_VOICE_CHANNEL;

        registerPurchase(group, item, price);
    }

    public static  void registerGroupCreated(OficinaGroup group, int price) {
        registerPurchase(group, StoreItemType.GROUP, price);
    }

    public static void registerInvoicePaid(OficinaGroup group, int price) {
        registerAction(group, TransactionType.INVOICE_PAID, null, price);
    }

    public static void registerMemberAdded(OficinaGroup group, int price) {
        registerPurchase(group, StoreItemType.GROUP_SLOT, price);
    }

    // We don't call registerAction() because it does not support removals.
    // So we reinvent the wheel here :(
    public static void registerMemberRemoved(OficinaGroup group) {
        long ownerId = group.getOwnerId();
        CurrencyType currency = group.getCurrency();

        BankTransaction tr = new BankTransaction(ownerId, 0, currency, TransactionType.ITEM_SOLD, StoreItemType.GROUP_SLOT);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }

    public static void registerPermissionAdded(OficinaGroup group, int price) {
        registerPurchase(group, StoreItemType.GROUP_PERMISSION, price);
    }

    public static void registerMessagePinned(OficinaGroup group, int price) {
        registerPurchase(group, StoreItemType.PIN_MESSAGE, price);
    }

    public static void registerGroupUpdated(OficinaGroup group, int price) {
        registerPurchase(group, StoreItemType.UPDATE_GROUP, price);
    }

    public static boolean hasFreeSlots(OficinaGroup group) {
        return grpPerkRepo.countFree(group.getId(), StoreItemType.GROUP_SLOT) < OficinaGroup.INITIAL_SLOTS;
    }

    public static int calcPurchases(OficinaGroup group) {
        int groupId = group.getId();
        return grpPerkRepo.sumPerksByGroupId(groupId);
    }

    public static void setRepositories(GroupPerkRepository grpPerkRepo) {
        GroupHelper.grpPerkRepo = grpPerkRepo;
    }

    private static void registerPurchase(OficinaGroup group, StoreItemType item, int price) {
        registerAction(group, TransactionType.ITEM_BOUGHT, item, price);
    }

    private static void registerAction(OficinaGroup group, TransactionType type, StoreItemType item, int amount) {
        checkTransactionType(type);
        CurrencyType currency = group.getCurrency();
        long ownerId = group.getOwnerId();

        // It is safe to negate "amount" here because the "groups_perks" table
        // only logs additions (e.g., purchases or invoice payments) and never removals
        // (like member removal, channel deletion, etc.).
        // In other words, every transaction in a group context is a positive inflow,
        // so we negate it to reflect an outgoing charge.
        BankTransaction tr = new BankTransaction(ownerId, -amount, currency, type);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));

        try {
            if (item != null) {
                // Save the perk purchase
                GroupPerk perk = new GroupPerk(group, ownerId, item, amount, currency);
                grpPerkRepo.save(perk);
            }
        } catch (DataAccessException e) {
            LOGGER.error("Could not save perk {} for group {}", item.name(), group.getId(), e);
        }
    }

    private static void checkTransactionType(TransactionType type) {
        if (type != TransactionType.ITEM_BOUGHT && type != TransactionType.INVOICE_PAID)
            throw new IllegalArgumentException(String.format(
                    "Groups' Perks do not support transaction types other than \"%s\" or \"%s\", provided: %s",
                    TransactionType.ITEM_BOUGHT.name(), TransactionType.INVOICE_PAID.name(), type));
    }
}