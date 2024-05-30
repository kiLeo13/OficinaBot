package ofc.bot.listeners.logs.messages;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.annotations.listeners.EventHandler;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.tables.DiscordMessages;
import org.jooq.DSLContext;

import static ofc.bot.databases.entities.tables.DiscordMessages.DISCORD_MESSAGES;

@EventHandler
public class MessageDeletedListener extends ListenerAdapter {

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {

        long messageId = event.getMessageIdLong();

        if (!event.isFromGuild() || event.getGuild().getIdLong() != DiscordMessages.TARGET_GUILD)
            return;

        Guild guild = event.getGuild();

        guild.retrieveAuditLogs().limit(1).type(ActionType.MESSAGE_DELETE).queue((entries) -> {

            AuditLogEntry entry = entries.isEmpty() ? null : entries.get(0);

            if (entry == null)
                return;

            long targetId = entry.getTargetIdLong();
            long issuerId = entry.getUserIdLong();
            boolean isConsistent = ensureConsistency(messageId, targetId);

            // null either means it was not deleted by a moderator or could not be resolved
            DiscordMessages.softDelete(messageId, isConsistent ? issuerId : null);
        });
    }

    /**
     * Checks whether the deleted message is the same as the one
     * retrieved from the AuditLogs.
     * <p>
     * If it's not the same, then it merely means the message was deleted
     * by the author, and not by a moderator.
     *
     * @return {@code true} if the message exists AND is the same, {@code false} otherwise.
     */
    private boolean ensureConsistency(long messageId, long senderId) {

        DSLContext ctx = DBManager.getContext();

        return ctx.fetchExists(DISCORD_MESSAGES, DISCORD_MESSAGES.ID.eq(messageId)
                .and(DISCORD_MESSAGES.AUTHOR_ID.eq(senderId))
        );
    }
}