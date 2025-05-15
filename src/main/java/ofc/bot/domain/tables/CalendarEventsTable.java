package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.CalendarEvent;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;

import static ofc.bot.domain.tables.UsersTable.USERS;

public class CalendarEventsTable extends InitializableTable<CalendarEvent> {
    public static final CalendarEventsTable CALENDAR_EVENTS = new CalendarEventsTable();

    public final Field<Integer> ID         = newField("id",          INT.identity(true));
    public final Field<String> TITLE       = newField("title",       CHAR.notNull());
    public final Field<String> DESCRIPTION = newField("description", CHAR);
    public final Field<String> EXPRESSION  = newField("expression",  CHAR.notNull());
    public final Field<Long> ADDED_BY      = newField("added_by",    BIGINT.notNull());
    public final Field<Long> CREATED_AT    = newField("created_at",  BIGINT.notNull());
    public final Field<Long> UPDATED_AT    = newField("updated_at",  BIGINT.notNull());

    public CalendarEventsTable() {
        super("calendar_events");
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields())
                .constraint(
                        foreignKey(ADDED_BY).references(USERS, USERS.ID)
                );
    }

    @NotNull
    @Override
    public Class<CalendarEvent> getRecordType() {
        return CalendarEvent.class;
    }
}