package ofc.bot.listeners.interactions;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import ofc.bot.util.content.annotations.listeners.EventHandler;
import ofc.bot.databases.entities.records.UserPreferencesRecord;

@EventHandler
public class GenericInteractionLocaleUpsert extends ListenerAdapter {

    @Override
    public void onGenericInteractionCreate(GenericInteractionCreateEvent event) {

        DiscordLocale userLocale = event.getUserLocale();
        User user = event.getUser();

        new UserPreferencesRecord(user.getIdLong(), userLocale).save();
    }
}