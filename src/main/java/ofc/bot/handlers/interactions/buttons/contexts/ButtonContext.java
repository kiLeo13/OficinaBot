package ofc.bot.handlers.interactions.buttons.contexts;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ButtonContext {
    public static final long DEFAULT_EXPIRE_AFTER_MILLIS = 5 * 60 * 1000; // 5 minutes
    public static final String AUTHOR_KEY = "author_id";
    private final Map<String, Object> data;
    private Button button;
    private String scope;
    private Permission permission;
    private long expireAfterMillis;
    private boolean authorOnly;

    public ButtonContext(Button button, String scope, Permission permission, boolean authorOnly) {
        this.data = new HashMap<>();
        this.button = button;
        this.scope = scope;
        this.permission = permission;
        this.authorOnly = authorOnly;
        this.expireAfterMillis = DEFAULT_EXPIRE_AFTER_MILLIS;
    }

    private ButtonContext() {
        this(null, null, null, false);
    }

    public static ButtonContext of(ButtonStyle style, String label, Emoji emoji) {
        Button btn = Button.of(style, UUID.randomUUID().toString(), label, emoji);
        return new ButtonContext()
                .setButton(btn);
    }

    public static ButtonContext of(ButtonStyle style, String label) {
        return of(style, label, null);
    }

    public static ButtonContext of(ButtonStyle style, Emoji emoji) {
        return of(style, null, emoji);
    }

    public static ButtonContext primary(String label, Emoji emoji) {
        return of(ButtonStyle.PRIMARY, label, emoji);
    }

    public static ButtonContext primary(String label) {
        return primary(label, null);
    }

    public static ButtonContext primary(Emoji emoji) {
        return primary(null, emoji);
    }

    public static ButtonContext secondary(String label, Emoji emoji) {
        return of(ButtonStyle.SECONDARY, label, emoji);
    }

    public static ButtonContext secondary(String label) {
        return secondary(label, null);
    }

    public static ButtonContext secondary(Emoji emoji) {
        return secondary(null, emoji);
    }

    public static ButtonContext success(String label, Emoji emoji) {
        return of(ButtonStyle.SUCCESS, label, emoji);
    }

    public static ButtonContext success(String label) {
        return success(label, null);
    }

    public static ButtonContext success(Emoji emoji) {
        return success(null, emoji);
    }

    public static ButtonContext danger(String label, Emoji emoji) {
        return of(ButtonStyle.DANGER, label, emoji);
    }

    public static ButtonContext danger(String label) {
        return danger(label, null);
    }

    public static ButtonContext danger(Emoji emoji) {
        return danger(null, emoji);
    }

    public ButtonContext put(String key, Object value) {
        Checks.notNull(key, "Key");
        this.data.put(key, value);
        return this;
    }

    public ButtonContext putAll(Map<String, Object> values) {
        Checks.notNull(values, "Map");
        values.forEach(this::put);
        return this;
    }

    public ButtonContext setAuthorId(long authorId) {
        return put(AUTHOR_KEY, authorId);
    }

    public ButtonContext setButton(@NotNull Button button) {
        Checks.notNull(button, "Button");
        this.button = button;
        return this;
    }

    public ButtonContext setDisabled(boolean disabled) {
        this.button = this.button.withDisabled(disabled);
        return this;
    }

    public ButtonContext setEnabled(boolean enabled) {
        return setDisabled(!enabled);
    }

    public ButtonContext disabled() {
        return setDisabled(true);
    }

    public ButtonContext enabled() {
        return setDisabled(false);
    }

    public ButtonContext setScope(@NotNull String scope) {
        Checks.notNull(scope, "ButtonContext scope");
        this.scope = scope;
        return this;
    }

    public ButtonContext setPermission(@Nullable Permission permission) {
        if (permission == Permission.UNKNOWN)
            throw new IllegalArgumentException("UNKNOWN permission is not allowed for a button context");

        this.permission = permission;
        return this;
    }

    public ButtonContext setValidity(long delay, @NotNull TimeUnit unit) {
        this.expireAfterMillis = unit.toMillis(delay);
        return this;
    }

    public ButtonContext setAuthorOnly(boolean authorOnly) {
        this.authorOnly = authorOnly;
        return this;
    }

    public long getAuthorId() {
        Long authorId = find(AUTHOR_KEY);
        return authorId == null ? 0 : authorId;
    }

    public Button getButton() {
        return this.button;
    }

    public String getScope() {
        return this.scope;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public long getValidityMillis() {
        return this.expireAfterMillis;
    }

    public boolean isAuthorOnly() {
        return this.authorOnly;
    }

    /**
     * Determines whether the specified entity is permitted to click or interact with this button.
     * <p>
     * Permission is granted based on two conditions:
     * <ul>
     *   <li>If a specific {@link #setPermission(Permission)} is provided, the entity must have it.</li>
     *   <li>If the button's ({@code authorOnly} is {@code true}), the entity must be the author.</li>
     * </ul>
     * Both conditions must be satisfied for this method to return {@code true}.
     * <p>
     * <b>Note:</b> If {@code authorOnly} is {@code true}, the entity is the author, but does not have
     * the required permission, this method will return {@code false}.
     *
     * @param entity the entity to evaluate against the permission requirements.
     * @return {@code true} if the entity meets all necessary conditions to interact with the button,
     *         {@code false} otherwise.
     */
    public boolean isPermitted(@NotNull IPermissionHolder entity) {
        if (permission != null && !entity.hasPermission(permission)) return false;
        return !authorOnly || isAuthor(entity.getIdLong());
    }

    /**
     * Checks if the author (identified by the {@code author_id} key) matches the given {@code id}.
     * <p>
     * This method performs the validation irrespective of whether {@link #isAuthorOnly()}
     * is enabled, that is, regardless of any factors, if the provided ID matches the value that
     * {@code author_id} maps to, it will return {@code true}.
     *
     * @param id the ID to be checked against the stored author ID.
     * @return {@code true} if the provided {@code id} matches the stored author ID, {@code false} otherwise.
     */
    public boolean isAuthor(long id) {
        Long authorId = find(AUTHOR_KEY);
        return authorId != null && id == authorId;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T get(String key) {
        Object obj = this.data.get(key);
        if (obj == null)
            throw new UnsupportedOperationException("No data found for key: " + key);

        return (T) obj;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T find(String key) {
        Object obj = this.data.get(key);
        return (T) obj;
    }

    @SuppressWarnings("unchecked")
    @Contract("null, null -> null")
    public <T> T getOrDefault(String key, T defaultValue) {
        Object obj = this.data.get(key);
        return obj == null ? defaultValue : (T) obj;
    }
}