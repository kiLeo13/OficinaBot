package ofc.bot.listeners.logs.names;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.databases.entities.tables.UserNameUpdates;

@EventHandler
public class UserNameUpdateLogger extends ListenerAdapter {

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {

        User user = event.getUser();
        String newName = event.getNewName();
        String oldName = event.getOldName();

        UserNameUpdates.insert(user.getIdLong(), newName, oldName);
    }
}