package ofc.bot.listeners.guilds.roles;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {

        Member member = event.getMember();

        if (member == null)
            return;

        Guild guild = event.getGuild();
        Member self = guild.getSelfMember();
        boolean shouldPersist = shouldPersist(member);

        if (!shouldPersist || guild.getIdLong() != TARGET_GUILD)
            return;

        List<Role> savedRoles = member.getRoles()
                .stream()
                .filter(self::canInteract)
                .toList();

        persist(member, savedRoles);
    }

    private void persist(Member member, List<Role> roles) {

        BatchInsertService service = new BatchInsertService();
        String username = member.getUser().getName();
        long userId = member.getIdLong();
        long guildId = member.getGuild().getIdLong();

        for (Role r : roles) {

            FormerMemberRoleRecord rec = new FormerMemberRoleRecord(userId, guildId, r.getIdLong());

            service.add(rec.getSave());
        }

        try {
            int rolesSaved = service.commit();

            LOGGER.info("Successfully stored {} roles of @{}", rolesSaved, username);
        } catch (DataAccessException e) {
            LOGGER.error("Could not save {} roles of {}", roles.size(), userId, e);
        }
    }

    private boolean shouldPersist(Member member) {

        Guild guild = member.getGuild();
        List<Role> roles = member.getRoles();
        Role salada = Roles.SALADA.toRole(guild);
        Role staff = Staff.GENERAL.toRole(guild);
        boolean isManageServer = member.hasPermission(Permission.MANAGE_SERVER);

        if (isManageServer)
            return true;

        if (salada != null && roles.contains(salada))
            return true;

        return staff != null && roles.contains(staff);
    }
}