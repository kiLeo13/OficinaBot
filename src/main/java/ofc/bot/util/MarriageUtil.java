package ofc.bot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import ofc.bot.commands.marriages.Marry;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.MarriageRecord;
import ofc.bot.util.content.Roles;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static ofc.bot.databases.entities.tables.MarriageRequests.MARRIAGE_REQUESTS;
import static ofc.bot.databases.entities.tables.Marriages.MARRIAGES;
import static ofc.bot.databases.entities.tables.Marriages.USERINFO_FORMAT;

public class MarriageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarriageUtil.class);
    private static final DSLContext CTX = DBManager.getContext();

    public static void createProposal(long from, long to) {

        long timestamp = Bot.unixNow();

        CTX.insertInto(MARRIAGE_REQUESTS)
                .set(MARRIAGE_REQUESTS.REQUESTER_ID, from)
                .set(MARRIAGE_REQUESTS.TARGET_ID, to)
                .set(MARRIAGE_REQUESTS.CREATED_AT, timestamp)
                .onDuplicateKeyIgnore()
                .execute();
    }

    public static boolean areMarried(long spouse, long anotherSpouse) {

        return CTX.fetchExists(MARRIAGES,
                MARRIAGES.REQUESTER_ID.eq(spouse)
                        .and(MARRIAGES.TARGET_ID.eq(anotherSpouse))
                        .or(MARRIAGES.REQUESTER_ID.eq(anotherSpouse)
                                .and(MARRIAGES.TARGET_ID.eq(spouse)))
        );
    }

    public static boolean isPending(long user, long anotherUser) {

        return CTX.fetchExists(MARRIAGE_REQUESTS,
                MARRIAGE_REQUESTS.REQUESTER_ID.eq(user).and(MARRIAGE_REQUESTS.TARGET_ID.eq(anotherUser))
                        .or(MARRIAGE_REQUESTS.REQUESTER_ID.eq(anotherUser).and(MARRIAGE_REQUESTS.TARGET_ID.eq(user)))
        );
    }

    public static boolean hasEnoughBalance(long targetId) {

        long balance = EconomyUtil.fetchBalance(targetId);

        return balance >= Marry.INITIAL_MARRIAGE_COST;
    }

    public static boolean hasHitLimit(Member member) {

        if (member.hasPermission(Permission.MANAGE_SERVER))
            return false;

        boolean privileged = isPrivileged(member);
        int marriageCount = MarriageUtil.getMarriageCount(member.getIdLong());

        return privileged
                ? marriageCount >= Marry.MAX_PRIVILEGED_MARRIAGES
                : marriageCount >= Marry.MAX_GENERAL_MARRIAGES;
    }

    public static int getMarriageCount(long userId) {

        try {
            return CTX.fetchCount(MARRIAGES, MARRIAGES.REQUESTER_ID.eq(userId).or(MARRIAGES.TARGET_ID.eq(userId)));
        } catch (DataAccessException e) {
            LOGGER.error("Could not get marriage count of user '{}'", userId, e);
        }

        return 0;
    }

    public static boolean isPartnered(long userId) {
        return CTX.fetchExists(MARRIAGES, MARRIAGES.REQUESTER_ID.eq(userId).or(MARRIAGES.TARGET_ID.eq(userId)));
    }

    public static String format(List<MarriageRecord> marriages) {
        return Bot.format(marriages, (mr) -> String.format(USERINFO_FORMAT, mr.getSelectedUserEffectiveName(), mr.getCreated()));
    }

    private static boolean isPrivileged(Member member) {

        List<Role> roles = member.getRoles();
        Guild guild = member.getGuild();
        Role salada = Roles.SALADA.toRole(guild);

        return salada != null && roles.contains(salada);
    }
}