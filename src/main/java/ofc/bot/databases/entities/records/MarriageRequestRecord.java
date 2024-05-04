package ofc.bot.databases.entities.records;

import ofc.bot.databases.DBManager;
import ofc.bot.databases.Repository;
import ofc.bot.databases.entities.tables.MarriageRequests;
import ofc.bot.databases.entities.tables.Marriages;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;
import org.jooq.Field;

public class MarriageRequestRecord extends Repository<Integer, MarriageRequestRecord> {

    public static final MarriageRequests MARRIAGE_REQUESTS = MarriageRequests.MARRIAGE_REQUESTS;

    public MarriageRequestRecord() {
        super(MARRIAGE_REQUESTS);
    }

    @Override
    public Field<Integer> getIdField() {
        return MARRIAGE_REQUESTS.ID;
    }

    public long getRequesterId() {
        return get(MARRIAGE_REQUESTS.REQUESTER_ID);
    }

    public long getTargetId() {
        return get(MARRIAGE_REQUESTS.TARGET_ID);
    }

    public long getCreated() {
        return get(MARRIAGE_REQUESTS.CREATED_AT);
    }

    public void approve() {

        long timestamp = Bot.unixNow();
        long requester = getRequesterId();
        long target = getTargetId();
        DSLContext ctx = DBManager.getContext();

        ctx.transaction((cfg) -> {

            ctx.insertInto(Marriages.MARRIAGES)
                    .set(Marriages.MARRIAGES.REQUESTER_ID, requester)
                    .set(Marriages.MARRIAGES.TARGET_ID, target)
                    .set(Marriages.MARRIAGES.CREATED_AT, timestamp)
                    .set(Marriages.MARRIAGES.UPDATED_AT, timestamp)
                    .execute();

            ctx.deleteFrom(MARRIAGE_REQUESTS)
                    .where(MARRIAGE_REQUESTS.REQUESTER_ID.eq(requester).and(MARRIAGE_REQUESTS.TARGET_ID.eq(target)))
                    .or(MARRIAGE_REQUESTS.REQUESTER_ID.eq(target).and(MARRIAGE_REQUESTS.TARGET_ID.eq(requester)))
                    .execute();
        });
    }
}