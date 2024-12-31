package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.AnnouncedGame;
import ofc.bot.domain.tables.AnnouncedGamesTable;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;

import java.util.concurrent.TimeUnit;

/**
 * Repository for {@link AnnouncedGame} entity.
 */
public class AnnouncedGameRepository {
    private static final AnnouncedGamesTable ANNOUNCED_GAMES = AnnouncedGamesTable.ANNOUNCED_GAMES;
    private final DSLContext ctx;

    public AnnouncedGameRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public void save(AnnouncedGame game) {
        ctx.insertInto(ANNOUNCED_GAMES)
                .set(game.intoMap())
                .execute();
    }

    public boolean existsAfterByGameId(String gameId, long period, TimeUnit unit) {
        long secs = unit.toSeconds(period);
        long startingPoint = Bot.unixNow() - secs;

        return ctx.fetchExists(ANNOUNCED_GAMES,
                ANNOUNCED_GAMES.GAME_ID.eq(gameId)
                        .and(ANNOUNCED_GAMES.NOTIFIED_AT.gt(startingPoint))
        );
    }
}
