package ofc.bot.databases.entities.records;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import ofc.bot.databases.RecordEntity;
import ofc.bot.databases.entities.tables.UsersPreferences;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;

public class UserPreferencesRecord extends RecordEntity<Long, UserPreferencesRecord> {

    public static final UsersPreferences USERS_PREFERENCES = UsersPreferences.USERS_PREFERENCES;

    public UserPreferencesRecord() {
        super(USERS_PREFERENCES);
    }

    public UserPreferencesRecord(long userId, DiscordLocale locale) {
        this();
        long timestamp = Bot.unixNow();

        set(USERS_PREFERENCES.USER_ID, userId);
        set(USERS_PREFERENCES.LOCALE, locale.getLocale());
        set(USERS_PREFERENCES.UPDATED_AT, timestamp);
        set(USERS_PREFERENCES.UPDATED_AT, timestamp);
    }

    @NotNull
    @Override
    public Field<Long> getIdField() {
        return USERS_PREFERENCES.USER_ID;
    }

    public long getUserId() {
        return getId();
    }

    public DiscordLocale getLocale() {
        String locale = get(USERS_PREFERENCES.LOCALE);
        return locale == null ? DiscordLocale.UNKNOWN : DiscordLocale.from(locale);
    }

    public long getCreated() {
        Long created = get(USERS_PREFERENCES.CREATED_AT);
        return created == null ? 0 : created;
    }

    public long getLastUpdated() {
        Long updated = get(USERS_PREFERENCES.UPDATED_AT);
        return updated == null ? 0 : updated;
    }
}