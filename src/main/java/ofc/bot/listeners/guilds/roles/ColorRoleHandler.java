package ofc.bot.listeners.guilds.roles;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.databases.entities.records.ColorRoleRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EventHandler
public class ColorRoleHandler extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorRoleHandler.class);

    private static final List<Long> colorRolesIds = List.of(
            946061433060347915L,
            946061781284032612L,
            946062089011752960L,
            946063870903074816L,
            946064125916753960L
    );

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {

        Member member = event.getMember();
        List<Role> addedRoles = event.getRoles();

        if (!hasColorRole(addedRoles))
            return;

        // Having any roles that are not color roles at this point
        // is nearly impossible, but why not expect the unexpected?
        List<Role> colorRoles = addedRoles.stream()
                .filter(this::isColorRole)
                .toList();

        for (Role role : colorRoles)
            register(member, role);
    }

    private void register(Member member, Role role) {

        Guild guild = member.getGuild();
        long userId = member.getIdLong();
        long guildId = guild.getIdLong();
        long roleId = role.getIdLong();

        ColorRoleRecord colorRole = new ColorRoleRecord(userId, guildId, roleId);

        colorRole.save();

        LOGGER.info("User '{}' has received color role '{}'", userId, role.getName());
    }

    private boolean isColorRole(Role role) {
        return colorRolesIds.contains(role.getIdLong());
    }

    private boolean hasColorRole(List<Role> roles) {

        return roles.stream()
                .anyMatch(r -> colorRolesIds.contains(r.getIdLong()));
    }
}