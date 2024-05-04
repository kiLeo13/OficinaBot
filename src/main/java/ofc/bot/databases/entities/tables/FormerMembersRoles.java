package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.FormerMemberRoleRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class FormerMembersRoles extends TableImpl<FormerMemberRoleRecord> {

    public static final FormerMembersRoles FORMER_MEMBERS_ROLES = new FormerMembersRoles();

    public final Field<Integer> ID      = createField(name("id"),         SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Long> USER       = createField(name("user_id"),    SQLDataType.BIGINT.notNull());
    public final Field<Long> GUILD      = createField(name("guild"),      SQLDataType.BIGINT.notNull());
    public final Field<Long> ROLE       = createField(name("role"),       SQLDataType.BIGINT.notNull());
    public final Field<Long> CREATED_AT = createField(name("created_at"), SQLDataType.BIGINT.notNull());

    public FormerMembersRoles() {
        super(name("former_members_roles"));
    }

    @NotNull
    @Override
    public Class<FormerMemberRoleRecord> getRecordType() {
        return FormerMemberRoleRecord.class;
    }
}