package ofc.bot.listeners.guilds.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.annotations.listeners.EventHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

@EventHandler
public class SuppressMEE6Warns extends ListenerAdapter {
    private static final long MEE6_ID = 758102786293497896L;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.isFromGuild())
            return;

        Message message = event.getMessage();
        User author = message.getAuthor();
        List<MessageEmbed> embeds = message.getEmbeds();
        long authorId = author.getIdLong();

        if (authorId != MEE6_ID)
            return;

        for (MessageEmbed embed : embeds) {

            MessageEmbed.AuthorInfo embedAuthor = embed.getAuthor();

            if (embedAuthor == null)
                continue;

            String authorName = embedAuthor.getName();

            if (authorName == null || authorName.contains("advertido")) {
                message.delete().queueAfter(10, TimeUnit.SECONDS);
                break;
            }
        }
    }
}