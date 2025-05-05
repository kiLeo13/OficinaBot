package ofc.bot.domain.entity;

import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.domain.tables.TwitchSubscriptionsTable;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public class TwitchSubscription extends OficinaRecord<TwitchSubscription> {
    private static final TwitchSubscriptionsTable TWITCH_SUBSCRIPTIONS = TwitchSubscriptionsTable.TWITCH_SUBSCRIPTIONS;

    public TwitchSubscription() {
        super(TWITCH_SUBSCRIPTIONS);
    }

    public TwitchSubscription(String channelId, String dest, boolean broadcast,
                              String subId, long addedBy, long createdAt, long updatedAt) {
        this();
        Checks.notNull(subId, "Subscription ID");
        set(TWITCH_SUBSCRIPTIONS.CHANNEL_ID, channelId);
        set(TWITCH_SUBSCRIPTIONS.DESTINATION, dest);
        set(TWITCH_SUBSCRIPTIONS.BROADCAST, broadcast);
        set(TWITCH_SUBSCRIPTIONS.ADDED_BY, addedBy);
        set(TWITCH_SUBSCRIPTIONS.SUB_ID, subId);
        set(TWITCH_SUBSCRIPTIONS.CREATED_AT, createdAt);
        set(TWITCH_SUBSCRIPTIONS.UPDATED_AT, updatedAt);
        checkDestination();
    }

    public int getId() {
        return get(TWITCH_SUBSCRIPTIONS.ID);
    }

    /**
     * The ID of the Twitch's channel ID.
     *
     * @return The ID of the channel at Twitch.
     */
    public String getChannelId() {
        return get(TWITCH_SUBSCRIPTIONS.CHANNEL_ID);
    }

    public String getDestination() {
        return get(TWITCH_SUBSCRIPTIONS.DESTINATION);
    }

    public boolean shouldBroadcast() {
        return get(TWITCH_SUBSCRIPTIONS.BROADCAST);
    }

    public long getAddedBy() {
        return get(TWITCH_SUBSCRIPTIONS.ADDED_BY);
    }

    public String getSubscriptionId() {
        return get(TWITCH_SUBSCRIPTIONS.SUB_ID);
    }

    public long getTimeCreated() {
        return get(TWITCH_SUBSCRIPTIONS.CREATED_AT);
    }

    @Override
    public long getLastUpdated() {
        return get(TWITCH_SUBSCRIPTIONS.UPDATED_AT);
    }

    public TwitchSubscription setChannelId(String channelId) {
        set(TWITCH_SUBSCRIPTIONS.CHANNEL_ID, channelId);
        return this;
    }

    public TwitchSubscription setDestination(@NotNull String destination) {
        set(TWITCH_SUBSCRIPTIONS.DESTINATION, destination);
        return this;
    }

    public TwitchSubscription setBroadcast(boolean broadcast) {
        set(TWITCH_SUBSCRIPTIONS.BROADCAST, broadcast);
        return this;
    }

    public TwitchSubscription setAddedBy(long addedBy) {
        set(TWITCH_SUBSCRIPTIONS.ADDED_BY, addedBy);
        return this;
    }

    public TwitchSubscription setSubscriptionId(@NotNull String subscriptionId) {
        set(TWITCH_SUBSCRIPTIONS.SUB_ID, subscriptionId);
        return this;
    }

    public TwitchSubscription setTimeCreated(long timestamp) {
        set(TWITCH_SUBSCRIPTIONS.CREATED_AT, timestamp);
        return this;
    }

    @NotNull
    @Override
    public TwitchSubscription setLastUpdated(long timestamp) {
        set(TWITCH_SUBSCRIPTIONS.UPDATED_AT, timestamp);
        return this;
    }

    private void checkDestination() {
        String destination = getDestination();
        if (destination.startsWith("https://")) {
            checkWebhook(destination);
        } else {
            Checks.isSnowflake(destination);
        }
    }

    private void checkWebhook(String url) {
        Matcher matcher = Webhook.WEBHOOK_URL.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Webhook URL: " + url);
        }
    }
}