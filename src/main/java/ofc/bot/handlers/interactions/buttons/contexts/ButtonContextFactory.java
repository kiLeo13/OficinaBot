package ofc.bot.handlers.interactions.buttons.contexts;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import ofc.bot.domain.entity.*;
import ofc.bot.domain.entity.enums.NameScope;
import ofc.bot.handlers.interactions.buttons.ButtonManager;
import ofc.bot.util.Bot;

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
                .setScope(Birthday.BUTTON_SCOPE)
                .put("month", previousMonth)
                .setEnabled(hasPrevious);

        ButtonContext nextButton = ButtonContext.primary(Emoji.fromUnicode("▶"))
                .setScope(Birthday.BUTTON_SCOPE)
                .put("month", nextMonth)
                .setEnabled(hasNext);

        BUTTON_MANAGER.save(prevButton, nextButton);
        return List.of(prevButton.getButton(), nextButton.getButton());
    }

    public static List<Button> createLeaderboardButtons(int pageIndex, boolean hasNext) {
        boolean hasPrevious = pageIndex > 0;

        ButtonContext prev = ButtonContext.primary("Previous")
                .setScope(UserEconomy.LEADERBOARD_BUTTON_SCOPE)
                .put("page_index", pageIndex - 1)
                .setEnabled(hasPrevious);

        ButtonContext next = ButtonContext.primary("Next")
                .setScope(UserEconomy.LEADERBOARD_BUTTON_SCOPE)
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
                .setScope(UserNameUpdate.NAME_UPDATE_SCOPE)
                .put("offset", previousOffset)
                .put("type", type)
                .put("target_id", targetId)
                .setPermission(Permission.MANAGE_SERVER)
                .setEnabled(hasPrevious);

        ButtonContext next = ButtonContext.primary("Next")
                .setScope(UserNameUpdate.NAME_UPDATE_SCOPE)
                .put("offset", nextOffset)
                .put("type", type)
                .put("target_id", targetId)
                .setPermission(Permission.MANAGE_SERVER)
                .setEnabled(hasNext);

        BUTTON_MANAGER.save(prev, next);
        return List.of(prev.getButton(), next.getButton());
    }

    public static List<Button> createMarriageListButtons(long targetId, int page, boolean hasNext) {
        boolean hasPrevious = page > 1;

        ButtonContext prev = ButtonContext.primary("Previous")
                .setScope(Marriage.MARRIAGE_BUTTON_SCOPE)
                .put("page", page - 1)
                .put("target_id", targetId)
                .setEnabled(hasPrevious);

        ButtonContext next = ButtonContext.primary("Next")
                .setScope(Marriage.MARRIAGE_BUTTON_SCOPE)
                .put("page", page + 1)
                .put("target_id", targetId)
                .setEnabled(hasNext);

        BUTTON_MANAGER.save(prev, next);
        return List.of(prev.getButton(), next.getButton());
    }

    public static List<Button> createProposalsListButtons(int page, long userId, boolean hasNext, String type) {
        int previousPage = page - 1;
        int nextPage = page + 1;
        boolean hasPrevious = page > 1;

        ButtonContext prev = ButtonContext.primary("Previous")
                .setScope(MarriageRequest.MARRIAGE_BUTTON_SCOPE)
                .setAuthorId(userId)
                .put("page", previousPage)
                .put("type", type)
                .setAuthorOnly(true)
                .setEnabled(hasPrevious);

        ButtonContext next = ButtonContext.primary("Next")
                .setScope(MarriageRequest.MARRIAGE_BUTTON_SCOPE)
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
                OficinaGroup.GROUP_CHANNEL_CREATE_BUTTON_SCOPE,
                group.getCurrency().getEmoji(),
                price,
                Bot.map("channel_type", channelType)
        );
    }

    public static Button createModifyGroupConfirm(OficinaGroup group, String newName, int newColor, int price) {
        return createGroupItemPaymentConfirm(
                group,
                OficinaGroup.GROUP_UPDATE_BUTTON_SCOPE,
                group.getCurrency().getEmoji(),
                price,
                Bot.map("new_name", newName, "new_color", newColor)
        );
    }

    public static Button createGroupBotAddConfirm(OficinaGroup group, GroupBot bot, int price) {
        return createGroupItemPaymentConfirm(
                group,
                OficinaGroup.GROUP_BOT_ADD_BUTTON_SCOPE,
                group.getCurrency().getEmoji(),
                price,
                Bot.map("bot", bot)
        );
    }

    public static Button createGroupConfirm(OficinaGroup partialGroup, int color) {
        return createGroupItemPaymentConfirm(
                partialGroup,
                OficinaGroup.GROUP_CREATE_BUTTON_SCOPE,
                partialGroup.getCurrency().getEmoji(),
                partialGroup.getAmountPaid(),
                Bot.map("group_color", color)
        );
    }

    public static Button createGroupDeletionConfirm(OficinaGroup group, int refund) {
        return createGroupItemDeletionConfirm(
                group,
                OficinaGroup.GROUP_DELETE_BUTTON_SCOPE,
                Emoji.fromUnicode("🗑"),
                refund,
                null
        );
    }

    public static Button createAddGroupMemberConfirm(OficinaGroup group, Member newMember, int price) {
        return createGroupItemPaymentConfirm(
                group,
                OficinaGroup.GROUP_MEMBER_ADD_BUTTON_SCOPE,
                group.getCurrency().getEmoji(),
                price,
                Bot.map("new_member", newMember)
        );
    }

    public static Button createRemoveGroupMemberConfirm(OficinaGroup group, long targetId) {
        return createGroupItemRemotionConfirm(
                group,
                OficinaGroup.GROUP_MEMBER_REMOVE_BUTTON_SCOPE,
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

    private static Button createGroupItemDeletionConfirm(
            OficinaGroup group, String scope, Emoji emoji, int price, Map<String, Object> payload
    ) {
        return genericConfirmButton(group, ButtonStyle.DANGER, "Deleção", emoji, scope, price, payload);
    }

    private static Button genericConfirmButton(
            OficinaGroup group, ButtonStyle style, String act, Emoji emoji,
            String scope, int price, Map<String, Object> payload
    ) {
        String label = String.format("Confirmar %s (%s)", act, Bot.fmtMoney(price));
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
