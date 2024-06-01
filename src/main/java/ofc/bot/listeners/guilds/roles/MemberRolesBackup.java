package ofc.bot.listeners.guilds.roles;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.Levels;
import ofc.bot.util.content.annotations.listeners.EventHandler;
import ofc.bot.databases.entities.records.FormerMemberRoleRecord;
import ofc.bot.databases.services.BatchInsertService;
import ofc.bot.util.content.Roles;
import ofc.bot.util.content.Staff;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EventHandler
public class MemberRolesBackup extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberRolesBackup.class);
    private static final long TARGET_GUILD = 582430782577049600L;

    private static final List<Levels> ACCEPTED_LEVELS = Levels.fromAbove(Levels.SABITO);

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {

        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild.getIdLong() != TARGET_GUILD)
            return;

        Member self = guild.getSelfMember();
        boolean privileged = isPrivileged(member);
        boolean shouldPersist = isMemberIncluded(member) || privileged;

        if (!shouldPersist)
            return;

        List<Role> savedRoles = member.getRoles()
                .stream()
                .filter(self::canInteract)
                .toList();

        saveRoles(member, savedRoles, privileged);
    }

    private void saveRoles(Member member, List<Role> roles, boolean privileged) {

        BatchInsertService service = new BatchInsertService();
        String username = member.getUser().getName();
        long userId = member.getIdLong();
        long guildId = member.getGuild().getIdLong();

        for (Role r : roles) {
            FormerMemberRoleRecord rec = new FormerMemberRoleRecord(userId, guildId, r.getIdLong(), privileged);

            service.add(rec.getSave(false));
        }

        try {
            int rolesSaved = service.commit();

            LOGGER.info("Successfully stored {} roles of @{}", rolesSaved, username);
        } catch (DataAccessException e) {
            LOGGER.error("Could not save {} roles of {}", roles.size(), userId, e);
        }
    }

    /*
     * Privileged members are members who will have their roles persisted forever,
     * the ExpiredBackupsRemover#EXPIRY_DAYS will not affect their backed-up roles.
     */
    private boolean isPrivileged(Member member) {

        Guild guild = member.getGuild();
        List<Role> roles = member.getRoles();
        Role salada = Roles.SALADA.toRole(guild);

        if (Staff.isStaff(member))
            return true;

        return salada != null && roles.contains(salada);
    }

    /*
     * This method checks whether the given Member should have its roles backed up
     * in the database according to their MEE6 level, or if Member#isBoosting() returns true.
     */
    private boolean isMemberIncluded(Member member) {
        return member.isBoosting() || hasExpectedLevel(member);
    }

    private boolean hasExpectedLevel(Member member) {

        Guild guild = member.getGuild();
        List<Role> roles = member.getRoles();

        return ACCEPTED_LEVELS.stream()
                .anyMatch(l -> roles.contains(l.role(guild)));
    }
}