package ofc.bot.databases.entities.tables;

import ofc.bot.databases.entities.records.NicknameUpdateRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public class Nicknames extends TableImpl<NicknameUpdateRecord> {

    public static final Nicknames NICKNAMES = new Nicknames();

    public final Field<Integer> ID      = createField(name("id"),           SQLDataType.INTEGER.notNull().identity(true));
    public final Field<Long> USER       = createField(name("user_id"),      SQLDataType.BIGINT.notNull());
    public final Field<Long> MODERATOR  = createField(name("moderator_id"), SQLDataType.BIGINT);
    public final Field<String> OLD_NICK = createField(name("old_nick"),     SQLDataType.CHAR);
    public final Field<String> NEW_NICK = createField(name("new_nick"),     SQLDataType.CHAR);
    public final Field<Long> CREATED_AT = createField(name("created_at"),   SQLDataType.BIGINT.notNull());

    public Nicknames() {
        super(name("nicknames"));
    }

    @NotNull
    @Override
    public Class<NicknameUpdateRecord> getRecordType() {
        return NicknameUpdateRecord.class;
    }
}