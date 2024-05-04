package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.ColorRoleRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class ColorRoles extends TableImpl<ColorRoleRecord> {
    
    public static final ColorRoles COLOR_ROLES = new ColorRoles();

    public final Field<Integer> ID      = createField(name("id"),         SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Long> USER_ID    = createField(name("user"),       SQLDataType.BIGINT.notNull());
    public final Field<Long> GUILD_ID   = createField(name("guild"),      SQLDataType.BIGINT.notNull());
    public final Field<Long> ROLE_ID    = createField(name("role"),       SQLDataType.BIGINT.notNull());
    public final Field<Long> CREATED_AT = createField(name("created_at"), SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT = createField(name("updated_at"), SQLDataType.BIGINT.notNull());

    public ColorRoles() {
        super(name("color_roles"));
    }

    @NotNull
    @Override
    public Class<ColorRoleRecord> getRecordType() {
        return ColorRoleRecord.class;
    }
}