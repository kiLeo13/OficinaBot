package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.MarriageRequestRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class MarriageRequests extends TableImpl<MarriageRequestRecord> {

    public static final MarriageRequests MARRIAGE_REQUESTS = new MarriageRequests();

    public final Field<Integer> ID        = createField(name("id"),           SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Long> REQUESTER_ID = createField(name("requester_id"), SQLDataType.BIGINT.notNull());
    public final Field<Long> TARGET_ID    = createField(name("target_id"),    SQLDataType.BIGINT.notNull());
    public final Field<Long> CREATED_AT   = createField(name("created_at"),   SQLDataType.BIGINT.notNull());

    public MarriageRequests() {
        super(name("marriage_requests"));
    }

    @NotNull
    @Override
    public Class<MarriageRequestRecord> getRecordType() {
        return MarriageRequestRecord.class;
    }
}