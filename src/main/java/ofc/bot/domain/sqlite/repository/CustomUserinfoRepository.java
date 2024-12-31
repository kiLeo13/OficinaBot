package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.CustomUserinfo;
import ofc.bot.domain.tables.CustomUserinfoTable;
import org.jooq.DSLContext;

/**
 * Repository for {@link ofc.bot.domain.entity.CustomUserinfo CustomUserinfo} entity.
 */
public class CustomUserinfoRepository {
    private static final CustomUserinfoTable CUSTOM_USERINFO = CustomUserinfoTable.CUSTOM_USERINFO;
    private final DSLContext ctx;

    public CustomUserinfoRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public void deleteByUserId(long userId) {
        ctx.deleteFrom(CUSTOM_USERINFO)
                .where(CUSTOM_USERINFO.USER_ID.eq(userId))
                .execute();
    }

    public void upsert(CustomUserinfo csInfo) {
        csInfo.changed(CUSTOM_USERINFO.CREATED_AT, false);
        ctx.insertInto(CUSTOM_USERINFO)
                .set(csInfo.intoMap())
                .onDuplicateKeyUpdate()
                .set(csInfo)
                .execute();
    }

    public CustomUserinfo findByUserId(long userId) {
        return ctx.selectFrom(CUSTOM_USERINFO)
                .where(CUSTOM_USERINFO.USER_ID.eq(userId))
                .fetchOne();
    }

    public CustomUserinfo findByUserId(long userId, CustomUserinfo fallback) {
        CustomUserinfo csInfo = findByUserId(userId);
        return csInfo == null ? fallback : csInfo;
    }
}