package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.UserPreference;
import ofc.bot.domain.tables.UsersPreferencesTable;
import org.jooq.DSLContext;

/**
 * Repository for {@link ofc.bot.domain.entity.UserPreference UserPreference} entity.
 */
public class UserPreferenceRepository {
    private static final UsersPreferencesTable USERS_PREFERENCES = UsersPreferencesTable.USERS_PREFERENCES;
    private final DSLContext ctx;

    public UserPreferenceRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public UserPreference findByUserId(long userId, UserPreference fallback) {
        UserPreference pref = findByUserId(userId);
        return pref == null ? fallback : pref;
    }

    public UserPreference findByUserId(long userId) {
        return ctx.selectFrom(USERS_PREFERENCES)
                .where(USERS_PREFERENCES.USER_ID.eq(userId))
                .fetchOne();
    }

    public void upsert(UserPreference pref) {
        pref.changed(USERS_PREFERENCES.CREATED_AT, false);
        ctx.insertInto(USERS_PREFERENCES)
                .set(pref.intoMap())
                .onDuplicateKeyUpdate()
                .set(pref)
                .execute();
    }
}