package ofc.bot.domain.tables;

import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.AnnouncedGame;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import static org.jooq.impl.DSL.name;

public class AnnouncedGamesTable extends InitializableTable<AnnouncedGame> {
    public static final AnnouncedGamesTable ANNOUNCED_GAMES = new AnnouncedGamesTable();

    public final Field<Integer> ID       = createField(name("id"),          SQLDataType.INTEGER.identity(true));
    public final Field<String> GAME_ID   = createField(name("game_id"),     SQLDataType.CHAR.notNull());
    public final Field<String> TITLE     = createField(name("title"),       SQLDataType.CHAR.notNull());
    public final Field<Long> NOTIFIED_AT = createField(name("notified_at"), SQLDataType.BIGINT.notNull());

    public AnnouncedGamesTable() {
        super("announced_games");
    }

    @NotNull
    @Override
    public Class<AnnouncedGame> getRecordType() {
        return AnnouncedGame.class;
    }

    @Override
    public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException {
        return ctx.createTableIfNotExists(this)
                .primaryKey(ID)
                .columns(fields());
    }
}