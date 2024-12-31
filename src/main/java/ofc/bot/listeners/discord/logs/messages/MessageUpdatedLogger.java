package ofc.bot.listeners.discord.logs.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.domain.entity.DiscordMessage;
import ofc.bot.domain.entity.DiscordMessageUpdate;
import ofc.bot.domain.sqlite.repository.DiscordMessageRepository;
import ofc.bot.domain.sqlite.repository.DiscordMessageUpdateRepository;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

@DiscordEventHandler
public class MessageUpdatedLogger extends ListenerAdapter {
    private final DiscordMessageRepository msgRepo;
    private final DiscordMessageUpdateRepository updRepo;

    public MessageUpdatedLogger(DiscordMessageRepository msgRepo, DiscordMessageUpdateRepository updRepo) {
        this.msgRepo = msgRepo;
        this.updRepo = updRepo;
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        Message message = event.getMessage();
        User author = message.getAuthor();
        boolean fromGuild = event.isFromGuild();
        long msgId = message.getIdLong();

        if (author.isBot()) return;

        if (!fromGuild || message.getGuildIdLong() != DiscordMessage.TARGET_GUILD) return;

        DiscordMessage msg = msgRepo.findById(msgId, DiscordMessage.fromMessage(message));
        String newContent = message.getContentRaw();
        String oldContnet = msg.getContent();
        DiscordMessageUpdate msgUpd = new DiscordMessageUpdate(msgId, oldContnet, newContent, Bot.unixNow());

        // Users can only edit the content of a message
        msg.setContent(newContent).tickUpdate();
        msgRepo.upsert(msg);
        updRepo.save(msgUpd);
    }
}