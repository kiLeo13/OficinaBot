package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.MarriageRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class Marriages extends TableImpl<MarriageRecord> {

    public static final String USERINFO_FORMAT = "💕 %s (<t:%d:d>)\n";

    public static final Marriages MARRIAGES = new Marriages();

    public final TableField<MarriageRecord, String> USER_NAME = createField(name("name"), SQLDataType.CHAR);
    public final TableField<MarriageRecord, String> USER_GLOBAL_NAME = createField(name("global_name"), SQLDataType.CHAR);

    public final Field<Integer> ID        = createField(name("id"),           SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Long> REQUESTER_ID = createField(name("requester_id"), SQLDataType.BIGINT.notNull());
    public final Field<Long> TARGET_ID    = createField(name("target_id"),    SQLDataType.BIGINT.notNull());
    public final Field<Long> CREATED_AT   = createField(name("created_at"),   SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT   = createField(name("updated_at"),   SQLDataType.BIGINT.notNull());

    public Marriages() {
        super(name("marriages"));
    }

    @NotNull
    @Override
    public Class<MarriageRecord> getRecordType() {
        return MarriageRecord.class;
    }
}