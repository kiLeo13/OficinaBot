package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.GroupRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class Groups extends TableImpl<GroupRecord> {

    public static final Groups GROUPS = new Groups();

    public final Field<Integer> ID            = createField(name("id"),               SQLDataType.INTEGER.identity(true));
    public final Field<Long> OWNER_ID         = createField(name("owner_id"),         SQLDataType.BIGINT.notNull());
    public final Field<Long> GUILD_ID         = createField(name("guild_id"),         SQLDataType.BIGINT.notNull());
    public final Field<Long> ROLE_ID          = createField(name("role_id"),          SQLDataType.BIGINT.notNull());
    public final Field<Long> TEXT_CHANNEL_ID  = createField(name("text_channel_id"),  SQLDataType.BIGINT);
    public final Field<Long> VOICE_CHANNEL_ID = createField(name("voice_channel_id"), SQLDataType.BIGINT);
    public final Field<String> NAME           = createField(name("name"),             SQLDataType.CHAR.notNull());
    public final Field<Integer> COLOR         = createField(name("color"),            SQLDataType.INTEGER.notNull());

    /**
     * Privileged groups are usually groups that belong to someone
     * from our staff, they are not eligible for monthly rent.
     * <p>
     * This field represents a boolean property, where 0 = {@code false} and 1 = {@code true}.
     */
    public final Field<Integer> PRIVILEGED = createField(name("privileged"), SQLDataType.INTEGER.notNull());

    public final Field<Long> CREATED_AT = createField(name("created_at"), SQLDataType.BIGINT.notNull());
    public final Field<Long> UPDATED_AT = createField(name("updated_at"), SQLDataType.BIGINT.notNull());

    public Groups() {
        super(name("groups"));
    }

    @NotNull
    @Override
    public Class<GroupRecord> getRecordType() {
        return GroupRecord.class;
    }
}
