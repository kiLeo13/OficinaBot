package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.DiscordMessage;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import static ofc.bot.domain.tables.UsersTable.USERS;
import static org.jooq.impl.DSL.*;

public class DiscordMessagesTable extends InitializableTable<DiscordMessage> {
    public static final DiscordMessagesTable DISCORD_MESSAGES = new DiscordMessagesTable();

    public final Field<Long> ID                   = createField(name("id"),                   SQLDataType.BIGINT.notNull());
    public final Field<Long> AUTHOR_ID            = createField(name("author_id"),            SQLDataType.BIGINT.notNull());
    public final Field<Long> CHANNEL_ID           = createField(name("channel_id"),           SQLDataType.BIGINT.notNull());
    public final Field<Long> MESSAGE_REFERENCE_ID = createField(name("message_reference_id"), SQLDataType.BIGINT);
    public final Field<String> CONTENT            = createField(name("content"),              SQLDataType.CHAR);
    public final Field<Long> STICKER_ID           = createField(name("sticker_id"),           SQLDataType.BIGINT);
    public final Field<Integer> DELETED           = createField(name("deleted"),              SQLDataType.INTEGER);
    public final Field<Long> DELETION_AUTHOR_ID   = createField(name("deletion_author_id"),   SQLDataType.BIGINT);
    public final Field<Long> CREATED_AT           = createField(name("created_at"),           SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT           = createField(name("updated_at"),           SQLDataType.BIGINT.notNull());

    public DiscordMessagesTable() {
        super("discord_messages");
    }

    @NotNull
    @Override
    public Class<DiscordMessage> getRecordType() {
        return DiscordMessage.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields())
                .constraints(
                        foreignKey(AUTHOR_ID).references(USERS, USERS.ID),
                        foreignKey(DELETION_AUTHOR_ID).references(USERS, USERS.ID)
                );
    }
}
