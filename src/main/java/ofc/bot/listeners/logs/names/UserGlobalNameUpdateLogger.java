package ofc.bot.listeners.logs.names;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.annotations.listeners.EventHandler;
import ofc.bot.databases.entities.tables.UserGlobalNameUpdates;

@EventHandler
public class UserGlobalNameUpdateLogger extends ListenerAdapter {

    @Override
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent event) {

        User user = event.getUser();
        String newName = event.getNewGlobalName();
        String oldName = event.getOldGlobalName();

        UserGlobalNameUpdates.insert(user.getIdLong(), newName, oldName);
    }
}