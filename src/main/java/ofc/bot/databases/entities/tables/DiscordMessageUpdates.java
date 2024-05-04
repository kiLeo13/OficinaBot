package ofc.bot.databases.entities.tables;

import net.dv8tion.jda.api.entities.Message;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.DiscordMessageUpdateRecord;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class DiscordMessageUpdates extends TableImpl<DiscordMessageUpdateRecord> {

    public static final DiscordMessageUpdates DISCORD_MESSAGE_UPDATES = new DiscordMessageUpdates();

    public final Field<Integer> ID         = createField(name("id"),         SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Long> MESSAGE_ID    = createField(name("message_id"), SQLDataType.BIGINT.notNull());
    public final Field<String> OLD_CONTENT = createField(name("old_value"),  SQLDataType.CHAR.notNull());
    public final Field<String> NEW_CONTENT = createField(name("new_value"),  SQLDataType.CHAR.notNull());
    public final Field<Long> CREATED_AT    = createField(name("created_at"), SQLDataType.BIGINT.notNull());

    public DiscordMessageUpdates() {
        super(name("discord_message_updates"));
    }

    public static void update(Message message) {

        long id = message.getIdLong();
        long timestamp = Bot.unixNow();
        DSLContext ctx = DBManager.getContext();
        String newContent = message.getContentRaw();
        String oldContent = DiscordMessages.fetchContentById(id);

        DiscordMessages.upsert(message);

        ctx.insertInto(DISCORD_MESSAGE_UPDATES)
                .set(DISCORD_MESSAGE_UPDATES.MESSAGE_ID, id)
                .set(DISCORD_MESSAGE_UPDATES.OLD_CONTENT, oldContent)
                .set(DISCORD_MESSAGE_UPDATES.NEW_CONTENT, newContent)
                .set(DISCORD_MESSAGE_UPDATES.CREATED_AT, timestamp)
                .execute();
    }

    @NotNull
    @Override
    public Class<DiscordMessageUpdateRecord> getRecordType() {
        return DiscordMessageUpdateRecord.class;
    }
}