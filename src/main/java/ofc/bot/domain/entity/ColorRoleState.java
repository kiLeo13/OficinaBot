package ofc.bot.domain.entity;

import ofc.bot.domain.tables.ColorRolesStateTable;
import ofc.bot.util.Bot;
import org.jooq.impl.TableRecordImpl;

public class ColorRoleState extends TableRecordImpl<ColorRoleState> {
    private static final ColorRolesStateTable COLOR_ROLES_STATES = ColorRolesStateTable.COLOR_ROLES_STATES;

    public ColorRoleState() {
        super(COLOR_ROLES_STATES);
    }

    public ColorRoleState(long userId, long guildId, long roleId, long createdAt, long updatedAt) {
        this();
        set(COLOR_ROLES_STATES.USER_ID, userId);
        set(COLOR_ROLES_STATES.GUILD_ID, guildId);
        set(COLOR_ROLES_STATES.ROLE_ID, roleId);
        set(COLOR_ROLES_STATES.CREATED_AT, createdAt);
        set(COLOR_ROLES_STATES.UPDATED_AT, updatedAt);
    }

    public static ColorRoleState fromBase(long userId, long guildId, long roleId) {
        long now = Bot.unixNow();
        return new ColorRoleState(userId, guildId, roleId, now, now);
    }

    public long getUserId() {
        return get(COLOR_ROLES_STATES.USER_ID);
    }

    public long getGuildId() {
        return get(COLOR_ROLES_STATES.GUILD_ID);
    }

    public long getRoleId() {
        return get(COLOR_ROLES_STATES.ROLE_ID);
    }

    public long getTimeCreated() {
        return get(COLOR_ROLES_STATES.CREATED_AT);
    }

    public long getLastUpdated() {
        return get(COLOR_ROLES_STATES.UPDATED_AT);
    }

    public ColorRoleState setUserId(long userId) {
        set(COLOR_ROLES_STATES.USER_ID, userId);
        return this;
    }

    public ColorRoleState setGuildId(long guildId) {
        set(COLOR_ROLES_STATES.GUILD_ID, guildId);
        return this;
    }

    public ColorRoleState setRoleId(long roleId) {
        set(COLOR_ROLES_STATES.ROLE_ID, roleId);
        return this;
    }

    public ColorRoleState setTimeCreated(long createdAt) {
        set(COLOR_ROLES_STATES.CREATED_AT, createdAt);
        return this;
    }

    public ColorRoleState setLastUpdated(long updatedAt) {
        set(COLOR_ROLES_STATES.UPDATED_AT, updatedAt);
        return this;
    }

    /**
     * Shortcut for {@link #setLastUpdated(long)}.
     * Here you don't have to manually provide a timestamp value,
     * the field is updated through the {@link Bot#unixNow()} method call.
     *
     * @return The current instance, for chaining convenience.
     */
    public ColorRoleState tickUpdate() {
        return setLastUpdated(Bot.unixNow());
    }
}