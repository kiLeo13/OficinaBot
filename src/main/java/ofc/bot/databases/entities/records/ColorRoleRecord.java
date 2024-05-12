package ofc.bot.databases.entities.records;

import ofc.bot.databases.RecordEntity;
import ofc.bot.databases.entities.tables.ColorRoles;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;

public class ColorRoleRecord extends RecordEntity<Integer, ColorRoleRecord> {

    public static final ColorRoles COLOR_ROLES = ColorRoles.COLOR_ROLES;

    public ColorRoleRecord() {
        super(COLOR_ROLES);
    }

    public ColorRoleRecord(long userId, long guildId, long roleId) {
        this();
        long timestamp = Bot.unixNow();

        set(COLOR_ROLES.USER_ID, userId);
        set(COLOR_ROLES.GUILD_ID, guildId);
        set(COLOR_ROLES.ROLE_ID, roleId);
        set(COLOR_ROLES.CREATED_AT, timestamp);
        set(COLOR_ROLES.UPDATED_AT, timestamp);
    }

    @NotNull
    @Override
    public Field<Integer> getIdField() {
        return COLOR_ROLES.ID;
    }

    public long getUserId() {
        Long id = get(COLOR_ROLES.USER_ID);
        return id == null ? 0 : id;
    }

    public long getGuildId() {
        Long guild = get(COLOR_ROLES.GUILD_ID);
        return guild == null ? 0 : guild;
    }

    public long getRoleId() {
        Long role = get(COLOR_ROLES.ROLE_ID);
        return role == null ? 0 : role;
    }

    public long getCreated() {
        Long created = get(COLOR_ROLES.CREATED_AT);
        return created == null ? 0 : created;
    }

    public long getLastUpdated() {
        Long updated = get(COLOR_ROLES.UPDATED_AT);
        return updated == null ? 0 : updated;
    }
}