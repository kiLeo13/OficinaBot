package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.EconomyRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class Economy extends TableImpl<EconomyRecord> {

    public static final String SYMBOL = "<a:coin:1160091618746060811>";
    public static final String BANK_ICON = "https://media.discordapp.net/attachments/506838906872922145/506899959816126493/h5D6Ei0.png";

    public static final Economy ECONOMY = new Economy();

    public final TableField<EconomyRecord, String> USER_GLOBAL_NAME = createField(name("global_name"), SQLDataType.CHAR);
    public final TableField<EconomyRecord, String> USER_NAME = createField(name("name"), SQLDataType.CHAR);

    public final Field<Long> USER_ID       = createField(name("user_id"),       SQLDataType.BIGINT.notNull().identity(true));
    public final Field<Long> BALANCE       = createField(name("balance"),       SQLDataType.BIGINT);
    public final Field<Long> LAST_DAILY_AT = createField(name("last_daily_at"), SQLDataType.BIGINT);
    public final Field<Long> LAST_WORK_AT  = createField(name("last_work_at"),  SQLDataType.BIGINT);
    public final Field<Long> CREATED_AT    = createField(name("created_at"),    SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT    = createField(name("updated_at"),    SQLDataType.BIGINT.notNull());

    public Economy() {
        super(name("economy"));
    }

    @NotNull
    @Override
    public Class<EconomyRecord> getRecordType() {
        return EconomyRecord.class;
    }
}