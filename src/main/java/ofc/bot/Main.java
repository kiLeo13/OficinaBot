package ofc.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import ofc.bot.domain.sqlite.DB;
import ofc.bot.handlers.ConsoleQueryHandler;
import ofc.bot.handlers.EntityInitializerManager;
import ofc.bot.internal.data.BotData;
import ofc.bot.internal.data.BotFiles;
import ofc.bot.twitch.TwitchService;
import ofc.bot.util.Bot;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static long initTime;
    private static JDA api;

    public static void main(String[] args) {
        try {
            BotFiles.loadFiles();
            DB.init();

            api = JDABuilder.createDefault(BotData.get("app.token"), Bot.getIntents())
                    .setEventPassthrough(true)
                    .setBulkDeleteSplittingEnabled(false)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY)
                    .setActivity(Activity.playing("Oficina"))
                    .build()
                    .awaitReady();

            initTime = Bot.unixNow();

            ConsoleQueryHandler.init();
            EntityInitializerManager.initializeCronJobs();
        } catch (DataAccessException e) {
            LOGGER.error("Could not perform database operation", e);
        } catch (IOException e) {
            LOGGER.error("Could not load bot files", e);
        } catch (InterruptedException e) {
            LOGGER.error("Failed to login", e);
            return;
        }

        // Registers
        EntityInitializerManager.initServices();
        EntityInitializerManager.registerListeners();
        EntityInitializerManager.registerSlashCommands();
        EntityInitializerManager.registerComposedInteractions();

        // Twitch
        TwitchService.init();
    }

    public static JDA getApi() {
        return api;
    }

    public static long getInitTime() {
        return initTime;
    }
}