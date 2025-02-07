package ofc.bot.jobs;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import ofc.bot.domain.entity.AnnouncedGame;
import ofc.bot.domain.sqlite.repository.AnnouncedGameRepository;
import ofc.bot.domain.sqlite.repository.RepositoryFactory;
import ofc.bot.handlers.requests.Route;
import ofc.bot.internal.data.BotData;
import ofc.bot.util.content.Channels;
import ofc.bot.util.content.annotations.jobs.CronJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

@CronJob(expression = "0 0 14 ? * * *")
public class EpicGamesPromotionAdvertiser implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(EpicGamesPromotionAdvertiser.class);
    private static final String EPICSTORE_GAME_ROUTE_FORMAT = "https://store.epicgames.com/p/%s";
    private final AnnouncedGameRepository gamesRepo = RepositoryFactory.getAnnouncedGameRepository();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        TextChannel channel = Channels.F.channel(TextChannel.class);
        if (channel == null) {
            LOGGER.warn("Free games announcement channel was not found");
            return;
        }

        List<GameData> freeGames = retrieveFreeGames();
        if (freeGames == null || freeGames.isEmpty()) {
            LOGGER.warn("Free games could not be fetched from EpicStore or none was found");
            return;
        }

        String gamesTitles = String.join("\n", mapToTitles(freeGames));
        String gamesUrls = String.join("\n", mapToUrls(freeGames));
        channel.sendMessageFormat("""
                **BOM DIA**
                
                Joguinho(s) de graça na Epic Games para vocês, chamado(s) %s.
                
                > %s
                
                || @everyone || 🍒
                """, gamesTitles, gamesUrls).queue();

        // Persisting to database
        for (GameData gameData : freeGames) {
            AnnouncedGame annGame = new AnnouncedGame(gameData.id, gameData.title);
            gamesRepo.save(annGame);
        }
    }

    private List<GameData> retrieveFreeGames() {
        String key = BotData.get("rapidapi.key");

        if (key == null)
            throw new RuntimeException("Rapid API key cannot be null");

        EpicStoreView view = Route.Games.GET_EPICSTORE_FREE_GAMES.create()
                .addHeader("x-rapidapi-host", "free-epic-games.p.rapidapi.com")
                .addHeader("x-rapidapi-key", key)
                .send()
                .json(EpicStoreView.class);

        if (view == null) return null;

        return excludeIneligibleGames(view.freeGames.current);
    }

    private List<GameData> excludeIneligibleGames(List<GameData> freeGames) {
        return freeGames.stream()
                .filter(GameData::isBaseGame)
                .filter(g -> !gamesRepo.existsAfterByGameId(g.id, 30, TimeUnit.DAYS))
                .toList();
    }

    private List<String> mapToTitles(List<GameData> games) {
        return games.stream()
                .map((g) -> String.format("`%s`", g.title))
                .toList();
    }

    private List<String> mapToUrls(List<GameData> games) {
        return games.stream()
                .map(GameData::getUrl)
                .toList();
    }

    private record EpicStoreView(
            CurrentGames freeGames
    ) {}

    private record CurrentGames(
            List<GameData> current
    ) {}

    private record OfferMapping(
            String pageSlug,
            String pageType
    ) {}

    private record GameData(
            String id,
            String title,
            String offerType,
            String urlSlug,
            List<OfferMapping> offerMappings
    ) {
        String getUrl() {
            return String.format(EPICSTORE_GAME_ROUTE_FORMAT, resolveRoute());
        }

        String resolveRoute() {
            return offerMappings != null && !offerMappings.isEmpty()
                    ? offerMappings.get(0).pageSlug
                    : urlSlug;
        }

        boolean isBaseGame() {
            // FOR SOME UNKNOWN GODDAMN REASON, THE OFFER TYPE
            // IN THIS API MAY SOMETIMES BE "OTHER".
            // AND NOW I ASK YOU WHY? WHY??? WHAT EVEN IS "OTHER"???????
            return !"ADD_ON".equals(offerType);
        }
    }
}
