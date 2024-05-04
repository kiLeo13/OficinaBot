package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.BirthdayRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.jooq.impl.DSL.name;

public class Birthdays extends TableImpl<BirthdayRecord> {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    public static final Birthdays BIRTHDAYS = new Birthdays();

    public final Field<Long> USER_ID       = createField(name("user_id"),    SQLDataType.BIGINT.notNull().identity(true));
    public final Field<String> NAME        = createField(name("name"),       SQLDataType.CHAR.notNull());
    public final Field<LocalDate> BIRTHDAY = createField(name("birthday"),   SQLDataType.LOCALDATE.notNull());
    public final Field<Long> CREATED_AT    = createField(name("created_at"), SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT    = createField(name("updated_at"), SQLDataType.BIGINT.notNull());

    public Birthdays() {
        super(name("birthdays"));
    }

    @NotNull
    @Override
    public Class<BirthdayRecord> getRecordType() {
        return BirthdayRecord.class;
    }
}