package ofc.bot.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.eventsub.Conduit;
import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.events.StreamOnlineEvent;
import com.github.twitch4j.eventsub.socket.conduit.TwitchConduitSocketPool;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.TwitchHelixBuilder;
import ofc.bot.twitch.listeners.StreamOnlineListener;
import ofc.bot.util.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * All this code at {@link #init()} was based on
 * <a href="https://github.com/SharedChatModHelper/backend/blob/main/src/main/kotlin/Main.kt#L92">This Example</a>.
 */
public class TwitchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitchService.class);
    private static final ScheduledExecutorService SCHED = Executors.newSingleThreadScheduledExecutor();
    private static TwitchConduitSocketPool socketPool;

    public static void init() {
        LOGGER.info("Initializing Twitch Client...");
        String clientId = Bot.get("twitch.client.id");
        String clientSecret = Bot.get("twitch.client.secret");

        if (clientId == null || clientSecret == null) {
            LOGGER.warn("Cannot build a new Twitch Client instance without both a clientId and a clientSecret");
            return;
        }

        TwitchIdentityProvider identityProvider = new TwitchIdentityProvider(clientId, clientSecret, null);
        OAuth2Credential token = identityProvider.getAppAccessToken();
        TwitchHelix helix = TwitchHelixBuilder.builder()
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withDefaultAuthToken(token)
                .build();


        // Keep app access token healthy
        SCHED.scheduleWithFixedDelay(() -> {
            try {
                token.updateCredential(identityProvider.getAppAccessToken());
            } catch (Exception e) {
                LOGGER.warn("Failed to regenerate credential", e);
            }
        }, 7, 5, TimeUnit.DAYS);

        // Preparing conduit
        try {
            Conduit lastConduit = getOldConduit(helix);
            socketPool = TwitchConduitSocketPool.create(spect -> {
                spect.clientId(clientId);
                spect.clientSecret(clientSecret);
                spect.conduitId(lastConduit == null ? null : lastConduit.getId());
                spect.helix(helix);
                spect.poolShards(1);
            });

            if (lastConduit == null) {
                LOGGER.info("Created new conduit with ID: {}! Registering Event Subscriptions...", socketPool.getConduitId());
                registerStreamOnlineListeners();
            } else {
                LOGGER.info("Reusing existing conduit with ID: {}", lastConduit.getId());
            }

            // Registering application-level listeners
            socketPool.getEventManager().onEvent(StreamOnlineEvent.class, new StreamOnlineListener());
        } catch (Exception e) {
            LOGGER.error("Failed to create TwitchConduitSocketPool", e);
        }
    }

    private static void registerStreamOnlineListeners() throws Exception {
        String subs = Bot.get("twitch.channels.subscriptions");
        if (subs == null) {
            LOGGER.warn("No channels to subscribe");
            socketPool.close();
            return;
        }
        String[] chanIds = subs.split(",");

        for (String chanId : chanIds) {
            EventSubSubscription sub = socketPool.register(SubscriptionTypes.STREAM_ONLINE,
                    b -> b.broadcasterUserId(chanId).build()).orElse(null);

            if (sub != null) {
                LOGGER.info("Successfully registered StreamOnlineListener for channel: {} ({})", chanId, sub.getTransport().getMethod());
            } else {
                LOGGER.warn("Failed to register StreamOnlineListener for channel: {}", chanId);
            }
        }
    }

    private static Conduit getOldConduit(TwitchHelix helix) {
        List<Conduit> conduits = helix.getConduits(null).execute().getConduits();
        return conduits == null || conduits.isEmpty() ? null : conduits.getFirst();
    }
}