package ofc.bot.listeners.guilds.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.util.content.Channels;

import java.util.List;

@EventHandler
public class SuppressMEE6Warns extends ListenerAdapter {
    private static final long MEE6_ID = 758102786293497896L;
    private static final long TARGET_CHANNEL = Channels.I.id();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.isFromGuild())
            return;

        Message message = event.getMessage();
        User author = message.getAuthor();
        List<MessageEmbed> embeds = message.getEmbeds();
        long channelId = event.getChannel().getIdLong();
        long authorId = author.getIdLong();

        if (authorId != MEE6_ID || channelId != TARGET_CHANNEL)
            return;

        for (MessageEmbed embed : embeds) {

            MessageEmbed.AuthorInfo embedAuthor = embed.getAuthor();

            if (embedAuthor == null)
                continue;

            String authorName = embedAuthor.getName();

            if (authorName == null || authorName.contains("advertido")) {
                message.delete().queue();
                break;
            }
        }
    }
}