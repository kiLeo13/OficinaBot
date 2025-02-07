package ofc.bot.handlers.interactions.buttons.contexts;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import ofc.bot.domain.entity.*;
import ofc.bot.domain.entity.enums.GroupPermission;
import ofc.bot.domain.entity.enums.NameScope;
import ofc.bot.handlers.interactions.buttons.ButtonManager;
import ofc.bot.util.Bot;
import ofc.bot.util.Scopes;

import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for generating buttons for all commands,
 * whether its for pagination or confirmation.
 * <p>
 * Just like that room you leave all your stuff you are unsure where to put, to keep the rest of
 * your house organized.
 * <p>
 * <b>Note:</b> All methods in this class are a part of the {@link ButtonContext}
 * feature, that is, when the {@link Button} instance is returned, its already being handled
 * by the {@link ofc.bot.handlers.interactions.buttons.ButtonManager ButtonManager} and ready
 * to be sent to the end-user.
 */
public final class ButtonContextFactory {
    private static final ButtonManager BUTTON_MANAGER = ButtonManager.getManager();

    private ButtonContextFactory() {}

    public static List<Button> createBirthdayListButtons(Month currMonth) {
        Month previousMonth = currMonth.minus(1);
        Month nextMonth = currMonth.plus(1);
        boolean hasPrevious = currMonth != Month.JANUARY;
        boolean hasNext = currMonth != Month.DECEMBER;

        ButtonContext prevButton = ButtonContext.primary(Emoji.fromUnicode("◀"))
                .setScope(Scopes.Misc.PAGINATE_BIRTHDAYS)
                .put("month", previousMonth)
                .setEnabled(hasPrevious);

        ButtonContext nextButton = ButtonContext.primary(Emoji.fromUnicode("▶"))
                .setScope(Scopes.Misc.PAGINATE_BIRTHDAYS)
                .put("month", nextMonth)
                .setEnabled(hasNext);

        BUTTON_MANAGER.save(prevButton, nextButton);
        return List.of(prevButton.getButton(), nextButton.getButton());
    }

    public static List<Button> createInfractionsButtons(long userId, int pageIndex, boolean hasNext) {
        boolean hasPrevious = pageIndex > 0;

        ButtonContext prev = ButtonContext.primary("Previous")
                .setScope(Scopes.Punishments.VIEW_INFRACTIONS)
                .put("page_index", pageIndex - 1)
                .setEnabled(hasPrevious);

        ButtonContext next = ButtonContext.primary("Next")
                .setScope(Scopes.Punishments.VIEW_INFRACTIONS)
                .put("page_index", pageIndex + 1)
                .setEnabled(hasNext);

        ButtonContext delete = ButtonContext.danger(Emoji.fromUnicode("🗑"))
                .setScope(Scopes.Punishments.DELETE_INFRACTION)
                .put("user_id", userId)
                .setEnabled(userId != 0);

        BUTTON_MANAGER.save(prev, next, delete);
        return List.of(prev.getButton(), next.getButton());
    }

    public static List<Button> createLeaderboardButtons(int pageIndex, boolean hasNext) {
        boolean hasPrevious = pageIndex > 0;

        ButtonContext prev = ButtonContext.primary("Previous")
                .setScope(Scopes.Misc.PAGINATE_LEADERBOARD)
                .put("page_index", pageIndex - 1)
                .setEnabled(hasPrevious);

        ButtonContext next = ButtonContext.primary("Next")
                .setScope(Scopes.Misc.PAGINATE_LEADERBOARD)
                .put("page_index", pageIndex + 1)
                .setEnabled(hasNext);

        BUTTON_MANAGER.save(prev, next);
        return List.of(prev.getButton(), next.getButton());
    }

    public static List<Button> createLevelsButtons(long authorId, int pageIndex, boolean hasNext) {
        boolean hasPrevious = pageIndex > 0;

        ButtonContext prev = ButtonContext.secondary(Bot.Emojis.GRAY_ARROW_LEFT)
                .setScope(Scopes.Misc.PAGINATE_LEVELS)
                .setAuthorId(authorId)
                .put("page_index", pageIndex - 1)
                .setEnabled(hasPrevious);

        ButtonContext next = ButtonContext.secondary(Bot.Emojis.GRAY_ARROW_RIGHT)
                .setScope(Scopes.Misc.PAGINATE_LEVELS)
                .setAuthorId(authorId)
                .put("page_index", pageIndex + 1)
                .setEnabled(hasNext);

        BUTTON_MANAGER.save(prev, next);
        return List.of(prev.getButton(), next.getButton());
    }

    public static List<Button> createNamesHistoryButtons(NameScope type, long targetId, int currentOffset, boolean hasNext) {
        int previousOffset = currentOffset - 10;
        int nextOffset = currentOffset + 10;
        boolean hasPrevious = currentOffset >= 10;

        ButtonContext prev = ButtonContext.primary("Previous")
                .setScope(Scopes.Misc.PAGINATE_NAME_UPDATE)
                .put("offset", previousOffset)
                .put("type", type)
                .put("target_id", targetId)
                .setPermission(Permission.MANAGE_SERVER)
                .setEnabled(hasPrevious);

        ButtonContext next = ButtonContext.primary("Next")
                .setScope(Scopes.Misc.PAGINATE_NAME_UPDATE)
                .put("offset", nextOffset)
                .put("type", type)
                .put("target_id", targetId)
                .setPermission(Permission.MANAGE_SERVER)
                .setEnabled(hasNext);

        BUTTON_MANAGER.save(prev, next);
        return List.of(prev.getButton(), next.getButton());
    }

    public static List<Button> createProposalsListButtons(int page, long userId, boolean hasNext, String type) {
        int previousPage = page - 1;
        int nextPage = page + 1;
        boolean hasPrevious = page > 1;

        ButtonContext prev = ButtonContext.primary("Previous")
                .setScope(Scopes.Misc.PAGINATE_MARRIAGE_REQUESTS)
                .setAuthorId(userId)
                .put("page", previousPage)
                .put("type", type)
                .setAuthorOnly(true)
                .setEnabled(hasPrevious);

        ButtonContext next = ButtonContext.primary("Next")
                .setScope(Scopes.Misc.PAGINATE_MARRIAGE_REQUESTS)
                .setAuthorId(userId)
                .put("page", nextPage)
                .put("type", type)
                .setAuthorOnly(true)
                .setEnabled(hasNext);

        BUTTON_MANAGER.save(prev, next);
        return List.of(prev.getButton(), next.getButton());
    }

    public static Button createGroupChannelConfirm(OficinaGroup group, ChannelType channelType, int price) {
        return createGroupItemPaymentConfirm(
                group,
                Scopes.Group.CREATE_CHANNEL,
                group.getCurrency().getEmoji(),
                price,
                Bot.map("channel_type", channelType)
        );
    }

    public static Button createInvoiceConfirm(OficinaGroup group, int amount) {
        return createGroupItemPaymentConfirm(
                group,
                Scopes.Group.PAY_INVOICE,
                group.getCurrency().getEmoji(),
                amount,
                Bot.map()
        );
    }

    public static Button createPermissionConfirm(OficinaGroup group, GroupPermission perm, int amount) {
        return createGroupItemPaymentConfirm(
                group,
                Scopes.Group.ADD_PERMISSION,
                group.getCurrency().getEmoji(),
                amount,
                Bot.map("permission", perm)
        );
    }

    public static Button createMessagePinConfirm(OficinaGroup group, long messageId, int price) {
        return createGroupItemPaymentConfirm(
                group,
                Scopes.Group.PIN_MESSAGE,
                group.getCurrency().getEmoji(),
                price,
                Bot.map(
                        "message_id", messageId,
                        "is_pin", true,
                        "group", group
                )
        );
    }

    public static Button createMessageUnpinConfirm(OficinaGroup group, long messageId) {
        return createGroupItemRemotionConfirm(
                group,
                Scopes.Group.PIN_MESSAGE,
                Emoji.fromUnicode("🗑"),
                0,
                Bot.map(
                        "message_id", messageId,
                        "is_pin", false,
                        "group", group
                )
        );
    }

    public static Button createModifyGroupConfirm(OficinaGroup group, String newName, int newColor, int price) {
        return createGroupItemRemotionConfirm(
                group,
                Scopes.Group.UPDATE_GROUP,
                group.getCurrency().getEmoji(),
                price,
                Bot.map("new_name", newName, "new_color", newColor)
        );
    }

    public static Button createGroupBotAddConfirm(OficinaGroup group, GroupBot bot, int price) {
        return createGroupItemPaymentConfirm(
                group,
                Scopes.Group.ADD_BOT,
                group.getCurrency().getEmoji(),
                price,
                Bot.map("bot", bot)
        );
    }

    public static Button createGroupConfirm(OficinaGroup partialGroup, int color) {
        return createGroupItemPaymentConfirm(
                partialGroup,
                Scopes.Group.CREATE_GROUP,
                partialGroup.getCurrency().getEmoji(),
                partialGroup.getAmountPaid(),
                Bot.map("group_color", color)
        );
    }

    public static Button createAddGroupMemberConfirm(OficinaGroup group, Member newMember, int price) {
        return createGroupItemPaymentConfirm(
                group,
                Scopes.Group.ADD_MEMBER,
                group.getCurrency().getEmoji(),
                price,
                Bot.map("new_member", newMember)
        );
    }

    public static Button createRemoveGroupMemberConfirm(OficinaGroup group, long targetId) {
        return createGroupItemRemotionConfirm(
                group,
                Scopes.Group.REMOVE_MEMBER,
                null,
                0,
                Bot.map("target_id", targetId)
        );
    }

    private static Button createGroupItemPaymentConfirm(
            OficinaGroup group, String scope, Emoji emoji, int price, Map<String, Object> payload
    ) {
        return genericConfirmButton(group, ButtonStyle.SUCCESS, "Pagamento", emoji, scope, price, payload);
    }

    private static Button createGroupItemRemotionConfirm(
            OficinaGroup group, String scope, Emoji emoji, int price, Map<String, Object> payload
    ) {
        return genericConfirmButton(group, ButtonStyle.DANGER, "Remoção", emoji, scope, price, payload);
    }

    private static Button genericConfirmButton(
            OficinaGroup group, ButtonStyle style, String act, Emoji emoji,
            String scope, int price, Map<String, Object> payload
    ) {
        String label = String.format("Confirmar %s", act);
        ButtonContext confirm = ButtonContext.of(style, label, emoji)
                .setAuthorOnly(true)
                .setScope(scope)
                .setValidity(30, TimeUnit.SECONDS)
                .setAuthorId(group.getOwnerId())
                .put("group", group)
                .put("amount", price)
                .putAll(payload == null ? Map.of() : payload);

        BUTTON_MANAGER.save(confirm);
        return confirm.getButton();
    }
}
