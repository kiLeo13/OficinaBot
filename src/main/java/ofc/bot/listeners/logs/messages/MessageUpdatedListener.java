package ofc.bot.listeners.logs.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.databases.entities.tables.DiscordMessageUpdates;
import ofc.bot.databases.entities.tables.DiscordMessages;

@EventHandler
public class MessageUpdatedListener extends ListenerAdapter {

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {

        boolean fromGuild = event.isFromGuild();
        Message message = event.getMessage();
        User author = message.getAuthor();

        if (author.isBot())
            return;

        if (!fromGuild || message.getGuildIdLong() != DiscordMessages.TARGET_GUILD)
            return;

        DiscordMessageUpdates.update(message);
    }
}