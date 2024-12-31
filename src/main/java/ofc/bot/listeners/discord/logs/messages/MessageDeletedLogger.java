package ofc.bot.listeners.discord.logs.messages;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.domain.entity.DiscordMessage;
import ofc.bot.domain.sqlite.repository.DiscordMessageRepository;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

@DiscordEventHandler
public class MessageDeletedLogger extends ListenerAdapter {
    private final DiscordMessageRepository msgRepo;

    public MessageDeletedLogger(DiscordMessageRepository msgRepo) {
        this.msgRepo = msgRepo;
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (!event.isFromGuild() || event.getGuild().getIdLong() != DiscordMessage.TARGET_GUILD) return;

        long messageId = event.getMessageIdLong();
        Guild guild = event.getGuild();
        guild.retrieveAuditLogs().limit(1).type(ActionType.MESSAGE_DELETE).queue((entries) -> {
            AuditLogEntry entry = entries.isEmpty() ? null : entries.get(0);

            if (entry == null) return;

            long targetId = entry.getTargetIdLong();
            long issuerId = entry.getUserIdLong();
            boolean isConsistent = msgRepo.existsByMessageAndAuthorId(messageId, targetId);

            // null either means it was not deleted by a moderator or could not be resolved
            msgRepo.softDeleteByIdAndAuthor(messageId, isConsistent ? issuerId : null);
        });
    }
}