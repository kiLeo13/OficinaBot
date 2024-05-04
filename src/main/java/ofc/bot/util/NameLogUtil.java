package ofc.bot.util;

import ofc.bot.commands.administration.name_history.NamesHistoryData;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.INameChangeLog;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

import java.util.List;

import static ofc.bot.databases.entities.tables.Nicknames.NICKNAMES;
import static ofc.bot.databases.entities.tables.UserGlobalNameUpdates.USER_GLOBAL_NAME_UPDATES;
import static ofc.bot.databases.entities.tables.UserNameUpdates.USER_NAME_UPDATES;
import static org.jooq.impl.DSL.field;

public class NameLogUtil {
    private static final Field<Long> USER_ID = field("user_id", SQLDataType.BIGINT);
    private static final Field<Integer> ID = field("id", SQLDataType.INTEGER);
    private static final Field<Long> TIMESTAMP = field("created_at", SQLDataType.BIGINT);

    public static NamesHistoryData retrieveNamesOfUser(String type, long userId, int offset) {

        return switch (type) {

            case "nick" -> fetchNameHistory(userId, offset, NICKNAMES);

            case "global" -> fetchNameHistory(userId, offset, USER_GLOBAL_NAME_UPDATES);

            case "name" -> fetchNameHistory(userId, offset, USER_NAME_UPDATES);

            default -> throw new IllegalStateException("Unexpected name logging type: " + type);
        };
    }

    @SuppressWarnings("unchecked")
    private static NamesHistoryData fetchNameHistory(long userId, int offset, Table<?> table) {

        if (table.getRecordType().isAssignableFrom(INameChangeLog.class))
            throw new IllegalArgumentException("Name logging Record Type must implement interface " + INameChangeLog.class.getName());

        DSLContext ctx = DBManager.getContext();
        int page = (int) (Math.floor(offset / 10.0) + 1);

        List<? extends INameChangeLog> entries = (List<? extends INameChangeLog>) ctx.selectFrom(table)
                .where(USER_ID.eq(userId))
                .groupBy(ID)
                .orderBy(TIMESTAMP.desc())
                .limit(10)
                .offset(offset)
                .fetchInto(table.getRecordType());

        int rowCount = countRows(ctx, table, userId);

        return new NamesHistoryData(entries, rowCount, offset, page);
    }

    private static int countRows(DSLContext ctx, Table<?> table, long userId) {
        return ctx.fetchCount(table, USER_ID.eq(userId));
    }
}