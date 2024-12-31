package ofc.bot.listeners.discord.guilds.roles;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

@DiscordEventHandler
public class GroupRoleDeletionHandler extends ListenerAdapter {
    private final OficinaGroupRepository grpRepo;

    public GroupRoleDeletionHandler(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent e) {
        Role role = e.getRole();
        OficinaGroup group = grpRepo.findByRoleId(role.getIdLong());

        if (group == null) return;

        grpRepo.delete(group);

        TextChannel textChan = group.getTextChannel();
        VoiceChannel voiceChan = group.getVoiceChannel();

        if (textChan != null)
            textChan.delete().queue();

        if (voiceChan != null)
            voiceChan.delete().queue();
    }
}