package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.DiscordMessageUpdate;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import static ofc.bot.domain.tables.DiscordMessagesTable.DISCORD_MESSAGES;
import static org.jooq.impl.DSL.*;

public class DiscordMessageUpdatesTable extends InitializableTable<DiscordMessageUpdate> {
    public static final DiscordMessageUpdatesTable DISCORD_MESSAGE_UPDATES = new DiscordMessageUpdatesTable();

    public final Field<Integer> ID         = createField(name("id"),         SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Long> MESSAGE_ID    = createField(name("message_id"), SQLDataType.BIGINT.notNull());
    public final Field<String> OLD_CONTENT = createField(name("old_value"),  SQLDataType.CHAR.notNull());
    public final Field<String> NEW_CONTENT = createField(name("new_value"),  SQLDataType.CHAR.notNull());
    public final Field<Long> CREATED_AT    = createField(name("created_at"), SQLDataType.BIGINT.notNull());

    public DiscordMessageUpdatesTable() {
        super("discord_message_updates");
    }

    @NotNull
    @Override
    public Class<DiscordMessageUpdate> getRecordType() {
        return DiscordMessageUpdate.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields())
                .constraint(
                        foreignKey(MESSAGE_ID).references(DISCORD_MESSAGES, DISCORD_MESSAGES.ID)
                );
    }
}