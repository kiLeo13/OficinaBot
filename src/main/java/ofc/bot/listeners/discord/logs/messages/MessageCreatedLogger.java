package ofc.bot.listeners.discord.logs.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.domain.entity.DiscordMessage;
import ofc.bot.domain.sqlite.repository.DiscordMessageRepository;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

import java.util.List;

@DiscordEventHandler
public class MessageCreatedLogger extends ListenerAdapter {
    private final DiscordMessageRepository msgRepo;

    public MessageCreatedLogger(DiscordMessageRepository msgRepo) {
        this.msgRepo = msgRepo;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        List<StickerItem> stickers = message.getStickers();

        // If the message does not have neither a content, nor
        // any stickers, then it's an "attachment-based" message, so we ignore it
        if (content.isBlank() && stickers.isEmpty())
            return;

        if (!message.isFromGuild() || message.getGuildIdLong() != DiscordMessage.TARGET_GUILD)
            return;

        DiscordMessage msg = DiscordMessage.fromMessage(message);
        msgRepo.upsert(msg);
    }
}