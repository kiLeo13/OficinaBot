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

    public final Field<Long> ID                   = newField("id",                   SQLDataType.BIGINT.notNull());
    public final Field<Long> AUTHOR_ID            = newField("author_id",            SQLDataType.BIGINT.notNull());
    public final Field<Long> CHANNEL_ID           = newField("channel_id",           SQLDataType.BIGINT.notNull());
    public final Field<Long> MESSAGE_REFERENCE_ID = newField("message_reference_id", SQLDataType.BIGINT);
    public final Field<String> CONTENT            = newField("content",              SQLDataType.CHAR);
    public final Field<Long> STICKER_ID           = newField("sticker_id",           SQLDataType.BIGINT);
    public final Field<Integer> DELETED           = newField("deleted",              SQLDataType.INTEGER);
    public final Field<Long> DELETION_AUTHOR_ID   = newField("deletion_author_id",   SQLDataType.BIGINT);
    public final Field<Long> CREATED_AT           = newField("created_at",           SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT           = newField("updated_at",           SQLDataType.BIGINT.notNull());

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
