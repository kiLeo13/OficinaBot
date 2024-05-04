package ofc.bot.databases.users.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.databases.users.MembersDAO;

@EventHandler
public class UserNameUpdate extends ListenerAdapter {

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        upsertUser(event.getUser());
    }

    @Override
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent event) {
        upsertUser(event.getUser());
    }

    private void upsertUser(User user) {

        String name = user.getName();
        String globalName = user.getGlobalName();
        long userId = user.getIdLong();

        MembersDAO.upsertUser(userId, name, globalName);
    }
}