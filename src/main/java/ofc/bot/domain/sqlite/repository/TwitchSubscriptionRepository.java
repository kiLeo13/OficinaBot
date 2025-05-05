package ofc.bot.domain.sqlite.repository;

import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.TwitchSubscription;
import ofc.bot.domain.tables.TwitchSubscriptionsTable;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Repository for {@link TwitchSubscription} entity.
 */
public class TwitchSubscriptionRepository extends Repository<TwitchSubscription> {
    private static final TwitchSubscriptionsTable TWITCH_SUBSCRIPTIONS = TwitchSubscriptionsTable.TWITCH_SUBSCRIPTIONS;

    public TwitchSubscriptionRepository(DSLContext ctx) {
        super(ctx);
    }

    @NotNull
    @Override
    public InitializableTable<TwitchSubscription> getTable() {
        return TWITCH_SUBSCRIPTIONS;
    }

    public TwitchSubscription findByChannelIdAndDestination(@NotNull String channelId, @NotNull String destination) {
        Checks.notNull(channelId, "Channel ID");
        Checks.notNull(destination, "Destination");
        return ctx.selectFrom(TWITCH_SUBSCRIPTIONS)
                .where(TWITCH_SUBSCRIPTIONS.CHANNEL_ID.eq(channelId))
                .and(TWITCH_SUBSCRIPTIONS.DESTINATION.eq(destination))
                .fetchOne();
    }

    public List<TwitchSubscription> findByChannelId(@NotNull String channelId) {
        Checks.notNull(channelId, "Channel ID");
        return ctx.selectFrom(TWITCH_SUBSCRIPTIONS)
                .where(TWITCH_SUBSCRIPTIONS.CHANNEL_ID.eq(channelId))
                .fetch();
    }

    public TwitchSubscription findAnyByChannelId(@NotNull String chanId) {
        return ctx.selectFrom(TWITCH_SUBSCRIPTIONS)
                .where(TWITCH_SUBSCRIPTIONS.CHANNEL_ID.eq(chanId))
                .limit(1)
                .fetchOne();
    }

    public boolean existsByChannelId(@NotNull String channelId) {
        Checks.notNull(channelId, "Channel ID");
        return ctx.fetchExists(TWITCH_SUBSCRIPTIONS, TWITCH_SUBSCRIPTIONS.CHANNEL_ID.eq(channelId));
    }

    public void delete(@NotNull TwitchSubscription sub) {
        Checks.notNull(sub, "Subscription");
        ctx.deleteFrom(TWITCH_SUBSCRIPTIONS)
                .where(TWITCH_SUBSCRIPTIONS.ID.eq(sub.getId()))
                .execute();
    }
}