package ofc.bot.listeners.discord.logs.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.domain.entity.DiscordMessage;
import ofc.bot.domain.sqlite.repository.DiscordMessageRepository;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

@DiscordEventHandler
public class MessageCreatedLogger extends ListenerAdapter {
    private final DiscordMessageRepository msgRepo;

    public MessageCreatedLogger(DiscordMessageRepository msgRepo) {
        this.msgRepo = msgRepo;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        DiscordMessage msg = DiscordMessage.fromMessage(message);
        msgRepo.upsert(msg);
    }
}