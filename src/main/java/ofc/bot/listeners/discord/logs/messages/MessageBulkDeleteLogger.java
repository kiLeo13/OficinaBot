package ofc.bot.listeners.discord.logs.messages;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.domain.sqlite.repository.DiscordMessageRepository;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

import java.util.List;

@DiscordEventHandler
public class MessageBulkDeleteLogger extends ListenerAdapter {
    private final DiscordMessageRepository msgRepo;

    public MessageBulkDeleteLogger(DiscordMessageRepository msgRepo) {
        this.msgRepo = msgRepo;
    }

    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        Guild guild = event.getGuild();
        List<Long> deletedMessages = event.getMessageIds()
                .stream()
                .map(Long::parseLong)
                .toList();

        guild.retrieveAuditLogs().type(ActionType.MESSAGE_BULK_DELETE).limit(1).queue((entries) -> {
            AuditLogEntry entry = entries.isEmpty() ? null : entries.get(0);

            if (entry == null) return;

            long deletionAuthorId = entry.getUserIdLong();
            msgRepo.softDeleteByIdsAndAuthor(deletedMessages, deletionAuthorId);
        });
    }
}