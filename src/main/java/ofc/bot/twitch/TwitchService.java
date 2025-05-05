package ofc.bot.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.eventsub.Conduit;
import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.events.StreamOnlineEvent;
import com.github.twitch4j.eventsub.socket.conduit.TwitchConduitSocketPool;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.TwitchHelixBuilder;
import com.github.twitch4j.helix.domain.User;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.domain.entity.TwitchSubscription;
import ofc.bot.domain.sqlite.repository.Repositories;
import ofc.bot.domain.sqlite.repository.TwitchSubscriptionRepository;
import ofc.bot.twitch.listeners.StreamOnlineListener;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * All this code at {@link #init()} was based on
 * <a href="https://github.com/SharedChatModHelper/backend/blob/main/src/main/kotlin/Main.kt">This Example</a>.
 */
public class TwitchService {
    public static final String BASE_URL = "https://twitch.tv";
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitchService.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final TwitchSubscriptionRepository twitchSubRepo;
    private OAuth2Credential token;
    private TwitchConduitSocketPool socketPool;
    private TwitchHelix helix;

    private TwitchService(@NotNull TwitchSubscriptionRepository twitchSubRepo) {
        Checks.notNull(twitchSubRepo, "Twitch Subscription Repository");
        this.twitchSubRepo = twitchSubRepo;
    }

    public static TwitchService init() {
        String clientId = Bot.getSafe("twitch.client.id");
        String secret = Bot.getSafe("twitch.client.secret");
        return init(clientId, secret);
    }

    public static TwitchService init(@NotNull String clientId, @NotNull String secret) {
        LOGGER.debug("Initializing Twitch Client...");

        TwitchSubscriptionRepository twitchSubRepo = Repositories.getTwitchSubscriptionRepository();
        TwitchService service = new TwitchService(twitchSubRepo);
        TwitchIdentityProvider identityProvider = new TwitchIdentityProvider(clientId, secret, null);
        OAuth2Credential token = identityProvider.getAppAccessToken();
        TwitchHelix helix = TwitchHelixBuilder.builder()
                .withClientId(clientId)
                .withClientSecret(secret)
                .withDefaultAuthToken(token)
                .build();
        service.token = token;
        service.helix = helix;

        // Handle token refreshes
        service.runTokenRefreshJob(token, identityProvider);

        // Preparing conduit
        try {
            Conduit lastConduit = getOldConduit(helix);
            service.socketPool = TwitchConduitSocketPool.create(spect -> {
                spect.clientId(clientId);
                spect.clientSecret(secret);
                spect.conduitId(lastConduit == null ? null : lastConduit.getId());
                spect.helix(helix);
                spect.poolShards(1);
            });

            if (lastConduit == null) {
                LOGGER.info("Created new conduit with ID: {}! Registering Event Subscriptions...", service.socketPool.getConduitId());
                service.registerStreamOnlineListeners();
            } else {
                LOGGER.info("Reusing existing conduit with ID: {}", lastConduit.getId());
            }

            // Registering application-level listeners
            EventManager eventManager = service.socketPool.getEventManager();
            eventManager.onEvent(StreamOnlineEvent.class, new StreamOnlineListener());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TwitchConduitSocketPool", e);
        }

        return service;
    }

    /**
     * Gets the scheduler used for operations like token refresh in this {@code TwitchService} instance.
     *
     * @return The {@link ScheduledExecutorService} instance being used in this instance.
     */
    @NotNull
    public ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }

    /**
     * Gets the possibly-active Twitch Socket pool.
     * <p>
     * This method will always return {@code null} if {@link #init()} was never
     * called before on this instance.
     *
     * @return The current Twitch Socket pool being used, or {@code null} if still unset.
     */
    public TwitchConduitSocketPool getSocketPool() {
        return this.socketPool;
    }

    /**
     * Gets the possibly-active Twitch Helix interface.
     * <p>
     * This method will always return {@code null} if {@link #init()} was never
     * called before on this instance.
     *
     * @return The current Twitch Helix interface being used, or {@code null} if still unset.
     */
    public TwitchHelix getHelix() {
        return this.helix;
    }

    public OAuth2Credential getToken() {
        return this.token;
    }

    public List<User> retrieveUsers(String search) {
        return helix.getUsers(token.getAccessToken(), null, List.of(search))
                .execute()
                .getUsers();
    }

    public void close() {
        try {
            this.socketPool.close();
        } catch (Exception e) {
            LOGGER.error("Failed to close SocketPool", e);
        }
    }

    public EventSubSubscription subStreamOnline(String chanId) {
        EventSubSubscription eventSub = socketPool.register(SubscriptionTypes.STREAM_ONLINE,
                b -> b.broadcasterUserId(chanId).build()).orElse(null);

        if (eventSub != null) {
            LOGGER.info("Successfully registered StreamOnlineListener for channel: {} ({})",
                    chanId, eventSub.getTransport().getMethod());
        } else {
            LOGGER.warn("Failed to register StreamOnlineListener for channel: {}", chanId);
        }
        return eventSub;
    }

    public void ubsubStreamOnline(TwitchSubscription sub) {
        unsubStreamOnline(sub.getSubscriptionId());
    }

    public void unsubStreamOnline(String subId) {
        helix.deleteEventSubSubscription(this.token.getAccessToken(), subId).execute();
    }

    private void runTokenRefreshJob(OAuth2Credential token, TwitchIdentityProvider identityProvider) {
        int expiresIn = token.getExpiresIn();
        LOGGER.info("Twitch token will expire in {} seconds", expiresIn);

        // Keep app access token healthy
        scheduler.schedule(() -> {
            try {
                token.updateCredential(identityProvider.getAppAccessToken());
                runTokenRefreshJob(token, identityProvider);
            } catch (Exception e) {
                LOGGER.warn("Failed to regenerate credentials", e);
            }
        }, expiresIn, TimeUnit.SECONDS);
    }

    private void registerStreamOnlineListeners() {
        List<TwitchSubscription> subs = findDistinctSubscriptions();

        for (TwitchSubscription sub : subs) {
            subStreamOnline(sub.getChannelId());
        }
    }

    private static Conduit getOldConduit(TwitchHelix helix) {
        List<Conduit> conduits = helix.getConduits(null).execute().getConduits();
        return conduits == null || conduits.isEmpty() ? null : conduits.getFirst();
    }

    private List<TwitchSubscription> findDistinctSubscriptions() {
        return twitchSubRepo.findAll()
                .stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                TwitchSubscription::getChannelId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ),
                        map -> List.copyOf(map.values())
                ));
    }
}