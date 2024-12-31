package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.Birthday;
import ofc.bot.domain.tables.BirthdaysTable;
import org.jooq.DSLContext;

import java.time.Month;
import java.util.List;

import static org.jooq.impl.DSL.*;

/**
 * Repository for {@link ofc.bot.domain.entity.Birthday Birthday} entity.
 */
public class BirthdayRepository {
    private final BirthdaysTable BIRTHDAYS = BirthdaysTable.BIRTHDAYS;
    private final DSLContext ctx;

    public BirthdayRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public List<Birthday> findByCurrentMonth() {
        return ctx.selectFrom(BIRTHDAYS)
                .where(day(BIRTHDAYS.BIRTHDAY).eq(day(currentDate()))
                        .and(month(BIRTHDAYS.BIRTHDAY).eq(month(currentDate())))
                )
                .fetch();
    }

    public Birthday findByUserId(long userId) {
        return ctx.selectFrom(BIRTHDAYS)
                .where(BIRTHDAYS.USER_ID.eq(userId))
                .fetchOne();
    }

    public List<Birthday> findByMonth(Month month) {
        return ctx.selectFrom(BIRTHDAYS)
                .where(month(BIRTHDAYS.BIRTHDAY).eq(month.getValue()))
                .groupBy(BIRTHDAYS.USER_ID)
                .orderBy(day(BIRTHDAYS.BIRTHDAY).asc())
                .fetch();
    }

    public void upsert(Birthday birthday) {
        birthday.changed(BIRTHDAYS.CREATED_AT, false);
        ctx.insertInto(BIRTHDAYS)
                .set(birthday.intoMap())
                .onDuplicateKeyUpdate()
                .set(birthday)
                .execute();
    }

    public void delete(Birthday birthday) {
        ctx.executeDelete(birthday, BIRTHDAYS.USER_ID.eq(birthday.getUserId()));
    }
}