package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.UserExclusionRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class UsersExclusions extends TableImpl<UserExclusionRecord> {

    public static final UsersExclusions USERS_EXCLUSIONS = new UsersExclusions();

    public final Field<Integer> ID      = createField(name("id"),             SQLDataType.INTEGER.identity(true));
    public final Field<Long> USER_ID    = createField(name("user_id"),        SQLDataType.BIGINT.notNull());
    public final Field<String> TYPE     = createField(name("exclusion_type"), SQLDataType.CHAR.notNull());
    public final Field<Long> CREATED_AT = createField(name("created_at"),     SQLDataType.BIGINT.notNull());

    public UsersExclusions() {
        super(name("users_exclusions"));
    }

    @NotNull
    @Override
    public Class<UserExclusionRecord> getRecordType() {
        return UserExclusionRecord.class;
    }
}