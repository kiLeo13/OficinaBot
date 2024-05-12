package ofc.bot.databases.entities.records;

import ofc.bot.databases.RecordEntity;
import ofc.bot.databases.entities.tables.FormerMembersRoles;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;

public class FormerMemberRoleRecord extends RecordEntity<Integer, FormerMemberRoleRecord> {

    public static final FormerMembersRoles FORMER_MEMBER_ROLES = FormerMembersRoles.FORMER_MEMBERS_ROLES;

    public FormerMemberRoleRecord() {
        super(FORMER_MEMBER_ROLES);
    }

    public FormerMemberRoleRecord(long userId, long guildId, long roleId) {
        this();
        set(FORMER_MEMBER_ROLES.USER, userId);
        set(FORMER_MEMBER_ROLES.GUILD, guildId);
        set(FORMER_MEMBER_ROLES.ROLE, roleId);
        set(FORMER_MEMBER_ROLES.CREATED_AT, Bot.unixNow());
    }

    @NotNull
    public Field<Integer> getIdField() {
        return FORMER_MEMBER_ROLES.ID;
    }

    public long getUserId() {
        Long user = get(FORMER_MEMBER_ROLES.USER);
        return user == null ? 0 : user;
    }

    public long getGuildId() {
        Long guild = get(FORMER_MEMBER_ROLES.GUILD);
        return guild == null ? 0 : guild;
    }

    public long getRoleId() {
        Long role = get(FORMER_MEMBER_ROLES.ROLE);
        return role == null ? 0 : role;
    }

    public long getCreated() {
        Long created = get(FORMER_MEMBER_ROLES.CREATED_AT);
        return created == null ? 0 : created;
    }
}