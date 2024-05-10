package ofc.bot.databases.users.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.annotations.listeners.EventHandler;
import ofc.bot.databases.users.MembersDAO;

@EventHandler
public class MemberJoin extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        User user = event.getUser();
        String name = user.getName();
        String globalName = user.getGlobalName();
        long userId = user.getIdLong();

        MembersDAO.upsertUser(userId, name, globalName);
    }
}