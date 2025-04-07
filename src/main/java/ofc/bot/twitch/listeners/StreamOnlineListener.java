package ofc.bot.twitch.listeners;

import com.github.twitch4j.eventsub.events.StreamOnlineEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import ofc.bot.handlers.requests.Route;
import ofc.bot.util.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class StreamOnlineListener implements Consumer<StreamOnlineEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamOnlineListener.class);
    private static final String TWITCH_ICON = "https://play-lh.googleusercontent.com/Y6epalNGUKPgWyQpDCgVL621EgmOmXBWfQoJdaM8v0irKWEII5bEDvpaWp7Mey2MVg";
    private static final String BASE_URL = "https://twitch.tv/";

    @Override
    public void accept(StreamOnlineEvent e) {
        String userId = e.getBroadcasterUserId();
        String message = Bot.get("twitch.stream.on.message." + userId);
        String endpoint = Bot.get("twitch.stream.on.webhooks." + userId);
        String username = e.getBroadcasterUserName();
        String broadcastUrl = BASE_URL + username;
        if (message == null) {
            LOGGER.warn("We've received a new stream notification from {} but no messages were found", username);
            return;
        }

        if (endpoint == null) {
            LOGGER.warn("We've received a new stream notification from {} but no endpoints were found", username);
            return;
        }

        // Placeholders
        message = message.replace("{link}", broadcastUrl)
                .replace("{username}", username)
                .replace("{br}", "\n");

        LOGGER.info("Notifying StreamOnline of {}", username);
        postMessage(endpoint, message);
    }

    private void postMessage(String url, String message) {
        DataObject reqBody = DataObject.empty()
                .put("username", "Twitch Announcement")
                .put("avatar_url", TWITCH_ICON)
                .put("content", message);

        Route.post(url).create()
                .setBody(reqBody)
                .send();
    }
}