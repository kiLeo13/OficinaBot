package ofc.bot.databases.entities.records;

import ofc.bot.databases.Repository;
import ofc.bot.databases.entities.tables.Birthdays;
import ofc.bot.util.Bot;
import org.jooq.Field;

import java.time.LocalDate;

public class BirthdayRecord extends Repository<Long, BirthdayRecord> {

    public static final Birthdays BIRTHDAYS = Birthdays.BIRTHDAYS;

    public BirthdayRecord() {
        super(BIRTHDAYS);
    }

    public BirthdayRecord(long userId, String name, LocalDate birthday) {
        this();
        long timestamp = Bot.unixNow();

        set(BIRTHDAYS.USER_ID, userId);
        set(BIRTHDAYS.NAME, name);
        set(BIRTHDAYS.BIRTHDAY, birthday);
        set(BIRTHDAYS.CREATED_AT, timestamp);
        set(BIRTHDAYS.UPDATED_AT, timestamp);
    }

    public Field<Long> getIdField() {
        return BIRTHDAYS.USER_ID;
    }

    public long getUserId() {
        return getId();
    }
    
    public String getName() {
        return get(BIRTHDAYS.NAME);
    }

    public LocalDate getBirthday() {
        return get(BIRTHDAYS.BIRTHDAY);
    }

    public String getPrettyDate() {
        LocalDate birthday = getBirthday();

        return birthday == null
                ? null
                : Birthdays.FORMATTER.format(birthday);
    }

    public long getCreated() {
        Long created = get(BIRTHDAYS.CREATED_AT);
        return created == null
                ? 0
                : created;
    }

    public long getLastUpdated() {
        Long updated = get(BIRTHDAYS.UPDATED_AT);
        return updated == null
                ? 0
                : updated;
    }
}