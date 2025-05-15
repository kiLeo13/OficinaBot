package ofc.bot.domain.entity;

import ofc.bot.domain.tables.CalendarEventsTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class CalendarEvent extends OficinaRecord<CalendarEvent> {
    private static final CalendarEventsTable CALENDAR_EVENTS = CalendarEventsTable.CALENDAR_EVENTS;

    public CalendarEvent() {
        super(CALENDAR_EVENTS);
    }

    public CalendarEvent(@NotNull String title, @Nullable String description, @NotNull String expression,
                         long addedBy, long createdAt, long updatedAt) {
        this();
        set(CALENDAR_EVENTS.TITLE, title);
        set(CALENDAR_EVENTS.DESCRIPTION, description);
        set(CALENDAR_EVENTS.EXPRESSION, expression);
        set(CALENDAR_EVENTS.ADDED_BY, addedBy);
        set(CALENDAR_EVENTS.CREATED_AT, createdAt);
        set(CALENDAR_EVENTS.UPDATED_AT, updatedAt);
    }

    public int getId() {
        return get(CALENDAR_EVENTS.ID);
    }

    public String getTitle() {
        return get(CALENDAR_EVENTS.TITLE);
    }

    public String getDescription() {
        return get(CALENDAR_EVENTS.DESCRIPTION);
    }

    public String getRawExpression() {
        return get(CALENDAR_EVENTS.EXPRESSION);
    }

    public Expression getExpression() {
        return new Expression(getRawExpression());
    }

    public long getAddedBy() {
        return get(CALENDAR_EVENTS.ADDED_BY);
    }

    public long getTimeCreated() {
        return get(CALENDAR_EVENTS.CREATED_AT);
    }

    @Override
    public long getLastUpdated() {
        return get(CALENDAR_EVENTS.UPDATED_AT);
    }

    public static class Expression {
        public static final String FORMAT = "%s-%s-%s";
        private final String dayExp;
        private final String monthExp;
        private final String yearExp;

        private Expression(@NotNull String expression) {
            if (!check(expression)) {
                throw new IllegalArgumentException("Invalid expression format: " + expression);
            }

            String[] tokens = expression.split("-");
            this.dayExp = tokens[0];
            this.monthExp = tokens[1];
            this.yearExp = tokens[2];
        }

        @NotNull
        public String getRaw() {
            return String.format(FORMAT, this.dayExp, this.monthExp, this.yearExp);
        }

        public boolean matches(@NotNull TemporalAccessor date) {
            int day = date.get(ChronoField.DAY_OF_MONTH);
            int month = date.get(ChronoField.MONTH_OF_YEAR);
            int year = date.get(ChronoField.YEAR);

            return (dayExp.equals("*") || Integer.parseInt(dayExp) == day)
                    && (monthExp.equals("*") || Integer.parseInt(monthExp) == month)
                    && (yearExp.equals("*") || Integer.parseInt(yearExp) == year);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        public static boolean check(String expression) {
            String[] parts = expression.split("-");
            if (parts.length != 3) return false;

            for (String part : parts) {
                if (!part.matches("\\d{2}") && !part.equals("*")) {
                    return false;
                }
            }

            // We call `LocalDate#of` here to validate if the date actually exists
            try {
                int day = parts[0].equals("*") ? 1 : Integer.parseInt(parts[0]);
                int month = parts[1].equals("*") ? 1 : Integer.parseInt(parts[1]);
                int year = parts[2].equals("*") ? 2000 : Integer.parseInt(parts[2]); // arbitrary leap-safe year
                LocalDate.of(year, month, day);
                return true;
            } catch (DateTimeException e) {
                return false;
            }
        }
    }
}