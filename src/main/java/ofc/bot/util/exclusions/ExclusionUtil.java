package ofc.bot.util.exclusions;

import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.UserExclusionRecord;
import org.jooq.DSLContext;

import java.util.List;

import static ofc.bot.databases.entities.tables.UsersExclusions.USERS_EXCLUSIONS;

public class ExclusionUtil {

    public static List<Long> fetchExcluded(ExclusionType type) {

        DSLContext ctx = DBManager.getContext();

        return ctx.select(USERS_EXCLUSIONS.USER_ID)
                .from(USERS_EXCLUSIONS)
                .where(USERS_EXCLUSIONS.TYPE.eq(type.name()))
                .fetchInto(long.class);
    }

    public static boolean isExcluded(long userId, ExclusionType type) {

        DSLContext ctx = DBManager.getContext();

        return ctx.fetchExists(
                USERS_EXCLUSIONS,
                USERS_EXCLUSIONS.USER_ID.eq(userId)
                        .and(USERS_EXCLUSIONS.TYPE.eq(type.name()))
        );
    }

    public static void createExclusion(long userId, ExclusionType type) {
        new UserExclusionRecord(type.name(), userId).save();
    }

    public static void removeExclusion(long userId, ExclusionType type) {

        DSLContext ctx = DBManager.getContext();

        ctx.deleteFrom(USERS_EXCLUSIONS)
                .where(USERS_EXCLUSIONS.USER_ID.eq(userId))
                .and(USERS_EXCLUSIONS.TYPE.eq(type.name()))
                .execute();
    }
}