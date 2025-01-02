package ofc.bot.handlers.interactions.buttons.contexts;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.*;
import ofc.bot.domain.entity.enums.NameScope;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.handlers.interactions.buttons.ButtonManager;
import ofc.bot.util.Bot;

import java.time.Month;
import java.util.List;
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
 * <p>
 * You can change how long buttons stay in memory at {@link #setDelay(long, TimeUnit)}.
 */
public final class ButtonContextFactory {
    private static long DELAY_MILLIS = ButtonContext.DEFAULT_EXPIRE_AFTER_MILLIS;
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

    public static Button createGroupChannelConfirmationButton(OficinaGroup group, int price, Category category, StoreItemType itemType) {
        String label = String.format("Confirmar Pagamento ($%s)", Bot.fmtNum(price));
        ButtonContext confirm = ButtonContext.success(label, group.getCurrency().getEmoji())
                .setAuthorOnly(true)
                .setScope(OficinaGroup.GROUP_CHANNEL_CREATE_BUTTON_SCOPE)
                .setValidity(30, TimeUnit.SECONDS)
                .setAuthorId(group.getOwnerId())
                .put("group", group)
                .put("category", category)
                .put("item_type", itemType);

        BUTTON_MANAGER.save(confirm);
        return confirm.getButton();
    }

    public static Button createModifyGroupConfirmationButton(OficinaGroup group, String newName, int newColor, int cost) {
        String label = String.format("Confirmar Pagamento ($%s)", Bot.fmtNum(cost));
        ButtonContext confirm = ButtonContext.success(label, group.getCurrency().getEmoji())
                .setAuthorOnly(true)
                .setScope(OficinaGroup.GROUP_UPDATE_BUTTON_SCOPE)
                .setValidity(30, TimeUnit.SECONDS)
                .setAuthorId(group.getOwnerId())
                .put("group", group)
                .put("new_name", newName)
                .put("new_color", newColor)
                .put("cost", cost);

        BUTTON_MANAGER.save(confirm);
        return confirm.getButton();
    }

    public static Button createGroupBotConfirmationButton(OficinaGroup group, GroupBot bot, int price) {
        String label = String.format("Confirmat Pagamento ($%s)", Bot.fmtNum(price));
        ButtonContext confirm = ButtonContext.success(label, group.getCurrency().getEmoji())
                .setAuthorOnly(true)
                .setScope(OficinaGroup.GROUP_BOT_ADD_BUTTON_SCOPE)
                .setValidity(30, TimeUnit.SECONDS)
                .setAuthorId(group.getOwnerId())
                .put("group", group)
                .put("bot", bot);

        BUTTON_MANAGER.save(confirm);
        return confirm.getButton();
    }

    public static Button createGroupConfirmationButton(OficinaGroup partialGroup, int price, int color) {
        String label = String.format("Confirmar Pagamento ($%s)", Bot.fmtNum(price));
        ButtonContext confirm = ButtonContext.success(label, partialGroup.getCurrency().getEmoji())
                .setAuthorOnly(true)
                .setScope(OficinaGroup.GROUP_CREATE_BUTTON_SCOPE)
                .setValidity(30, TimeUnit.SECONDS)
                .setAuthorId(partialGroup.getOwnerId())
                .put("partial_group_data", partialGroup)
                .put("group_color", color);

        BUTTON_MANAGER.save(confirm);
        return confirm.getButton();
    }

    public static Button createGroupDeletionConfirmationButton(long authorId, int refund) {
        ButtonContext confirm = ButtonContext.danger("Apagar Grupo", Emoji.fromUnicode("🗑"))
                .setAuthorOnly(true)
                .setScope(OficinaGroup.GROUP_DELETE_BUTTON_SCOPE)
                .setValidity(30, TimeUnit.SECONDS)
                .setAuthorId(authorId)
                .put("refund", refund);

        BUTTON_MANAGER.save(confirm);
        return confirm.getButton();
    }

    public static Button createAddGroupMemberConfirmationButton(OficinaGroup group, Member newMember, int price) {
        String label = String.format("Confirmar Pagamento ($%s)", Bot.fmtNum(price));
        ButtonContext confirm = ButtonContext.success(label, group.getCurrency().getEmoji())
                .setAuthorOnly(true)
                .setScope(OficinaGroup.GROUP_MEMBER_ADD_BUTTON_SCOPE)
                .setValidity(30, TimeUnit.SECONDS)
                .setAuthorId(group.getOwnerId())
                .put("group", group)
                .put("price", price)
                .put("new_member", newMember);

        BUTTON_MANAGER.save(confirm);
        return confirm.getButton();
    }

    public static Button createRemoveGroupMemberConfirmationButton(OficinaGroup group, long targetId) {
        ButtonContext confirm = ButtonContext.danger("Confirmar Remoção ($0)")
                .setScope(OficinaGroup.GROUP_MEMBER_REMOVE_BUTTON_SCOPE)
                .setAuthorOnly(true)
                .setAuthorId(group.getOwnerId())
                .put("group", group)
                .put("target_id", targetId);

        BUTTON_MANAGER.save(confirm);
        return confirm.getButton();
    }

    public static long getDelayMillis() {
        return DELAY_MILLIS;
    }

    public static void setDelay(long delay, TimeUnit unit) {
        DELAY_MILLIS = unit.toMillis(delay);
    }
}
