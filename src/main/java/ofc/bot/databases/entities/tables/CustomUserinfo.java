package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.CustomUserinfoRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class CustomUserinfo extends TableImpl<CustomUserinfoRecord> {

    public static final String DEFAULT_DESCRIPTION_FORMAT = "Informações de `%s` <a:M_Myuu:643942157325041668>";

    public static final CustomUserinfo CUSTOM_USERINFO = new CustomUserinfo();

    public final Field<Long> USER_ID       = createField(name("user"),        SQLDataType.BIGINT.notNull());
    public final Field<Integer> COLOR      = createField(name("color"),       SQLDataType.INTEGER.notNull());
    public final Field<String> DESCRIPTION = createField(name("description"), SQLDataType.CHAR.notNull());
    public final Field<String> FOOTER      = createField(name("footer"),      SQLDataType.CHAR.notNull());
    public final Field<Long> CREATED_AT    = createField(name("created_at"),  SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT    = createField(name("updated_at"),  SQLDataType.BIGINT.notNull());

    public CustomUserinfo() {
        super(name("custom_userinfo"));
    }

    @NotNull
    @Override
    public Class<CustomUserinfoRecord> getRecordType() {
        return CustomUserinfoRecord.class;
    }
}