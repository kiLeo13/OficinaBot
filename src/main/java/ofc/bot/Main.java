package ofc.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import ofc.bot.handlers.*;
import ofc.bot.databases.DatabaseInitializer;
import ofc.bot.internal.data.BotData;
import ofc.bot.internal.data.BotFiles;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;

public final class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static long initTime;
    private static JDA api;

    public static void main(String[] args) {

        try {
            api = JDABuilder.createDefault(BotData.loadProperties().token(),
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                            GatewayIntent.GUILD_MODERATION,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.AUTO_MODERATION_EXECUTION,
                            GatewayIntent.SCHEDULED_EVENTS,
                            GatewayIntent.DIRECT_MESSAGES
                    )
                    .setBulkDeleteSplittingEnabled(false)
                    .setEventPassthrough(true)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.VOICE_STATE, CacheFlag.ONLINE_STATUS)
                    .setActivity(Activity.playing("Oficina"))
                    .build()
                    .awaitReady();

            initTime = Instant.now().toEpochMilli();

            ConsoleQueryHandler.init();
            EntityHandlersInitializers.initializeCronJobs();

            BotFiles.loadFiles();
            DatabaseInitializer.init();

        } catch (DataAccessException e) {

            LOGGER.error("Could not perform database operation", e);
        } catch (IOException e) {

            LOGGER.error("Could not load bot files", e);
        } catch (ParseException e) {

            LOGGER.error("Could not parse Cron Expression", e);
            return;
        } catch (InterruptedException e) {

            LOGGER.error("Failed to login", e);
            return;
        }

        // Registers
        EntityHandlersInitializers.registerListeners();
        EntityHandlersInitializers.registerCommands();
        EntityHandlersInitializers.registerButtons();
    }

    public static JDA getApi() {
        return api;
    }

    public static long getInitTime() {
        return initTime;
    }
}