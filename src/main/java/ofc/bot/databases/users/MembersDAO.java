package ofc.bot.databases.users;

import net.dv8tion.jda.api.entities.User;
import ofc.bot.databases.entities.records.UserRecord;

public class MembersDAO {

    public static void upsertUser(User user) {
        upsertUser(user.getIdLong(), user.getName(), user.getGlobalName());
    }

    public static void upsertUser(long userId, String name, String global) {

        if (name == null)
            throw new IllegalArgumentException("Received null user name for '" + userId + "' which is not permitted");

        UserRecord user = new UserRecord(userId, name, global);

        user.getSave().executeAsync();
    }
}