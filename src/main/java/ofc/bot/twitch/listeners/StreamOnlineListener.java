package ofc.bot.twitch.listeners;

import com.github.twitch4j.eventsub.events.StreamOnlineEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.data.DataObject;
import ofc.bot.Main;
import ofc.bot.domain.entity.TwitchSubscription;
import ofc.bot.domain.sqlite.repository.TwitchSubscriptionRepository;
import ofc.bot.handlers.requests.RequestMapper;
import ofc.bot.handlers.requests.Route;
import ofc.bot.twitch.TwitchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class StreamOnlineListener implements Consumer<StreamOnlineEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamOnlineListener.class);
    private static final String TWITCH_ICON = "https://play-lh.googleusercontent.com/Y6epalNGUKPgWyQpDCgVL621EgmOmXBWfQoJdaM8v0irKWEII5bEDvpaWp7Mey2MVg";
    private final TwitchSubscriptionRepository twitchSubRepo;

    public StreamOnlineListener(TwitchSubscriptionRepository twitchSubRepo) {
        this.twitchSubRepo = twitchSubRepo;
    }

    @Override
    public void accept(StreamOnlineEvent e) {
        String userId = e.getBroadcasterUserId();
        String username = e.getBroadcasterUserName();
        List<TwitchSubscription> subs = twitchSubRepo.findByChannelId(userId);
        JDA api = Main.getApi();

        for (TwitchSubscription sub : subs) {
            boolean broadcast = sub.shouldBroadcast();
            String message = getMessage(broadcast, username);

            LOGGER.info("Notifying StreamOnline of {}", username);
            handleNotification(api, message, sub);
        }
    }

    private String getMessage(boolean broadcast, String username) {
        String mention = broadcast ? "@everyone" : "";
        return String.format("""
                %s %s está em live agora!
                
                > %s/%s
                """, mention, username, TwitchService.BASE_URL, username.toLowerCase());
    }

    private void handleNotification(JDA api, String msg, TwitchSubscription sub) {
        String destination = sub.getDestination();
        boolean isWebhook = destination.startsWith("https://");

        if (isWebhook) {
            postMessage(sub, msg);
            return;
        }

        MessageChannel chan = api.getChannelById(MessageChannel.class, destination);
        if (chan == null) {
            handleNotificationFailure("Channel '" + destination + "' not found", sub, true);
            return;
        }
        chan.sendMessage(msg).queue(null, err -> handleNotificationFailure(err.getMessage(), sub, false));
    }

    private void postMessage(TwitchSubscription sub, String message) {
        DataObject reqBody = DataObject.empty()
                .put("username", "Twitch")
                .put("avatar_url", TWITCH_ICON)
                .put("content", message);

        RequestMapper result = Route.post(sub.getDestination()).create()
                .setBody(reqBody)
                .send();

        if (!result.isOk()) {
            handleNotificationFailure(result.asString(), sub, true);
        }
    }

    private void handleNotificationFailure(String msg, TwitchSubscription sub, boolean unsub) {
        String decision = unsub ? "Yes" : "No";
        LOGGER.warn("Failed to handle Twitch notification! Deleting it from the database? {}: {}", decision, msg);

        if (unsub) {
            twitchSubRepo.delete(sub);
        }
    }
}