package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.TwitchSubscription;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;

import static ofc.bot.domain.tables.UsersTable.USERS;

public class TwitchSubscriptionsTable extends InitializableTable<TwitchSubscription> {
    public static final TwitchSubscriptionsTable TWITCH_SUBSCRIPTIONS = new TwitchSubscriptionsTable();

    public final Field<Integer> ID         = newField("id",          INT.identity(true));
    public final Field<String> CHANNEL_ID  = newField("channel_id",  CHAR.notNull());
    public final Field<String> DESTINATION = newField("destination", CHAR.notNull());
    public final Field<Boolean> BROADCAST  = newField("broadcast",   BOOL.notNull());
    public final Field<Long> ADDED_BY      = newField("added_by",    BIGINT.notNull());
    public final Field<String> SUB_ID      = newField("sub_id",      CHAR.notNull());
    public final Field<Long> CREATED_AT    = newField("created_at",  BIGINT.notNull());
    public final Field<Long> UPDATED_AT    = newField("updated_at",  BIGINT.notNull());

    public TwitchSubscriptionsTable() {
        super("twitch_subscriptions");
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields())
                .unique(CHANNEL_ID, DESTINATION)
                .constraint(foreignKey(ADDED_BY).references(USERS, USERS.ID));
    }
}