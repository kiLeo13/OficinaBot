package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.Birthday;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import java.time.LocalDate;

import static org.jooq.impl.DSL.name;

public class BirthdaysTable extends InitializableTable<Birthday> {
    public static final BirthdaysTable BIRTHDAYS = new BirthdaysTable();

    public final Field<Long> USER_ID       = createField(name("user_id"),    SQLDataType.BIGINT.notNull());
    public final Field<String> NAME        = createField(name("name"),       SQLDataType.CHAR.notNull());
    public final Field<LocalDate> BIRTHDAY = createField(name("birthday"),   SQLDataType.LOCALDATE.notNull());
    public final Field<Long> CREATED_AT    = createField(name("created_at"), SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT    = createField(name("updated_at"), SQLDataType.BIGINT.notNull());

    public BirthdaysTable() {
        super("birthdays");
    }

    @NotNull
    @Override
    public Class<Birthday> getRecordType() {
        return Birthday.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(USER_ID)
                .columns(fields());
    }
}