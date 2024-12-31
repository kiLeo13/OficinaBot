package ofc.bot.domain.entity;

import ofc.bot.domain.tables.AnnouncedGamesTable;
import ofc.bot.util.Bot;
import org.jooq.impl.TableRecordImpl;

public class AnnouncedGame extends TableRecordImpl<AnnouncedGame> {
    private static final AnnouncedGamesTable ANNOUNCED_GAMES = AnnouncedGamesTable.ANNOUNCED_GAMES;

    public AnnouncedGame() {
        super(ANNOUNCED_GAMES);
    }

    public AnnouncedGame(String gameId, String title, long notifiedAt) {
        this();
        set(ANNOUNCED_GAMES.GAME_ID, gameId);
        set(ANNOUNCED_GAMES.TITLE, title);
        set(ANNOUNCED_GAMES.NOTIFIED_AT, notifiedAt);
    }

    public AnnouncedGame(String gameId, String title) {
        this(gameId, title, Bot.unixNow());
    }

    public int getId() {
        return get(ANNOUNCED_GAMES.ID);
    }

    public String getGameId() {
        return get(ANNOUNCED_GAMES.GAME_ID);
    }

    public String getTitle() {
        return get(ANNOUNCED_GAMES.TITLE);
    }

    public long getNotificationDate() {
        return get(ANNOUNCED_GAMES.NOTIFIED_AT);
    }
}