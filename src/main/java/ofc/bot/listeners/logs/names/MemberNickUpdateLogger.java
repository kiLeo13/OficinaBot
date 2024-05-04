package ofc.bot.listeners.logs.names;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.NicknameUpdateRecord;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ofc.bot.databases.entities.tables.Nicknames.NICKNAMES;

/**
 * The reason why we do not use the {@link net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent GuildMemberUpdateNicknameEvent} here
 * is because Discord does not tell us who changed their nickname.
 */
@EventHandler
public class MemberNickUpdateLogger extends ListenerAdapter {
    private static final long TARGET_GUILD = 582430782577049600L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberNickUpdateLogger.class);

    @Override
    public void onGuildAuditLogEntryCreate(GuildAuditLogEntryCreateEvent event) {

        Guild guild = event.getGuild();
        AuditLogEntry entry = event.getEntry();
        AuditLogChange change = entry.getChangeByKey("nick");

        if (entry.getType() != ActionType.MEMBER_UPDATE || change == null)
            return;

        if (guild.getIdLong() != TARGET_GUILD)
            return;

        String newValue = change.getNewValue();
        String oldValue = change.getOldValue();
        long createdAt = entry.getTimeCreated().toEpochSecond();
        long targetId = entry.getTargetIdLong();
        long moderatorId = entry.getUserIdLong();

        Bot.fetchUser(targetId).queue(target -> {

            String newName = newValue == null ? target.getEffectiveName() : newValue;
            NicknameUpdateRecord editMapping = new NicknameUpdateRecord(newName, oldValue, targetId, moderatorId, createdAt);

            editMapping.save();
        }, e -> {
            // This is one of those scenarios that is unlikely (impossible) to happen
            // but better safe than sorry?
            LOGGER.warn("User '{}' was not found for nickname logging", targetId);
        });
    }

    private void store(long timestamp, long moderatorId, long targetId, String oldNick, String newNick) {

        DSLContext ctx = DBManager.getContext();

        try {
            ctx.insertInto(NICKNAMES)
                    .set(NICKNAMES.USER, targetId)
                    .set(NICKNAMES.MODERATOR, moderatorId)
                    .set(NICKNAMES.OLD_NICK, oldNick)
                    .set(NICKNAMES.NEW_NICK, newNick)
                    .set(NICKNAMES.CREATED_AT, timestamp)
                    .onDuplicateKeyIgnore()
                    .execute();

        } catch (DataAccessException e) {
            LOGGER.error("Could not store guild nickname update of target '{}' performed by '{}'", targetId, moderatorId, e);
        }
    }
}