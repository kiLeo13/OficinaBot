package ofc.bot.domain.entity;

import ofc.bot.domain.tables.BirthdaysTable;
import ofc.bot.util.Bot;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Birthday extends UpdatableRecordImpl<Birthday> {
    private static final BirthdaysTable BIRTHDAYS = BirthdaysTable.BIRTHDAYS;

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    public static final String BUTTON_SCOPE = "BIRTHDAYS";
    public static final String ICON_URL = "https://cdn.discordapp.com/attachments/631974560605929493/1320446355226886244/calendar.png";
    public static final String BIRTHDAYS_FORMAT = "- %s  <@%d>\n";

    public Birthday() {
        super(BIRTHDAYS);
    }

    public Birthday(long userId, String name, LocalDate birthday, long createdAt, long updatedAt) {
        this();
        set(BIRTHDAYS.USER_ID, userId);
        set(BIRTHDAYS.NAME, name);
        set(BIRTHDAYS.BIRTHDAY, birthday);
        set(BIRTHDAYS.CREATED_AT, createdAt);
        set(BIRTHDAYS.UPDATED_AT, updatedAt);
    }

    public long getUserId() {
        return get(BIRTHDAYS.USER_ID);
    }
    
    public String getName() {
        return get(BIRTHDAYS.NAME);
    }

    public LocalDate getBirthday() {
        return get(BIRTHDAYS.BIRTHDAY);
    }

    public String getPrettyBirthday() {
        LocalDate birthday = getBirthday();

        return birthday == null
                ? null
                : FORMATTER.format(birthday);
    }

    public long getTimeCreated() {
        return get(BIRTHDAYS.CREATED_AT);
    }

    public long getLastUpdated() {
        return get(BIRTHDAYS.UPDATED_AT);
    }

    public Birthday setUserId(long userId) {
        set(BIRTHDAYS.USER_ID, userId);
        return this;
    }

    public Birthday setName(String name) {
        set(BIRTHDAYS.NAME, name);
        return this;
    }

    public Birthday setBirthday(LocalDate birthday) {
        set(BIRTHDAYS.BIRTHDAY, birthday);
        return this;
    }

    public Birthday setTimeCreated(long createdAt) {
        set(BIRTHDAYS.CREATED_AT, createdAt);
        return this;
    }

    public Birthday setLastUpdated(long updatedAt) {
        set(BIRTHDAYS.UPDATED_AT, updatedAt);
        return this;
    }

    /**
     * Shortcut for {@link #setLastUpdated(long)}.
     * Here you don't have to manually provide a timestamp value,
     * the field is updated through the {@link Bot#unixNow()} method call.
     *
     * @return The current instance, for chaining convenience.
     */
    public Birthday tickUpdate() {
        return setLastUpdated(Bot.unixNow());
    }
}