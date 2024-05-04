package ofc.bot.listeners.logs.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.databases.entities.tables.DiscordMessages;

import java.util.List;

@EventHandler
public class MessageCreatedListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        Message message = event.getMessage();
        String content = message.getContentRaw();
        User author = message.getAuthor();
        List<StickerItem> stickers = message.getStickers();

        // If the message does not have neither a content, nor
        // any stickers, then it's an "attachment-based" message, so we ignore it
        if (content.isBlank() && stickers.isEmpty())
            return;

        if (author.isBot())
            return;

        if (!message.isFromGuild() || message.getGuildIdLong() != DiscordMessages.TARGET_GUILD)
            return;

        DiscordMessages.upsert(message);
    }
}