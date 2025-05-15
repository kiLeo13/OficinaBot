package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.CalendarEvent;
import ofc.bot.domain.tables.CalendarEventsTable;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Repository for {@link CalendarEvent} entity.
 */
public class CalendarEventRepository extends Repository<CalendarEvent> {
    private static final CalendarEventsTable CALENDAR_EVENTS = CalendarEventsTable.CALENDAR_EVENTS;

    public CalendarEventRepository(DSLContext ctx) {
        super(ctx);
    }

    @NotNull
    @Override
    public InitializableTable<CalendarEvent> getTable() {
        return CALENDAR_EVENTS;
    }

    public List<CalendarEvent> findAllBy(int month, int year) {
        String check = String.format("%%%02d-%d", month, year);
        return ctx.selectFrom(CALENDAR_EVENTS)
                .where(CALENDAR_EVENTS.EXPRESSION.like(check))
                .fetch();
    }
}