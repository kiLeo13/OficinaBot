package ofc.bot.listeners.guilds.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.annotations.listeners.EventHandler;

@EventHandler
public class ErikPingReactionHelper extends ListenerAdapter {
    private static final long ERIK_ID = 145607100100247552L;
    private static final long TARGET_CHANNEL_ID = 1130626690792570970L;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

        Message msg = e.getMessage();
        String content = msg.getContentRaw();
        User author = e.getAuthor();
        long channelId = e.getChannel().getIdLong();
        long authorId = author.getIdLong();

        if (authorId != ERIK_ID || channelId != TARGET_CHANNEL_ID)
            return;

        if (!content.contains("@everyone"))
            return;

        msg.addReaction(Emoji.fromUnicode("👀")).queue();
    }
}