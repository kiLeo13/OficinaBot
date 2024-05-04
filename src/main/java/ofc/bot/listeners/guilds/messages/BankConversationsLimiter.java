package ofc.bot.listeners.guilds.messages;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.util.Bot;
import ofc.bot.util.content.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@EventHandler
public class BankConversationsLimiter extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BankConversationsLimiter.class);

    private static final long BANK_CHANNEL_ID = Channels.BANK.id();
    private static final long STAFF_ROLE_ID = 691178135596695593L;

    private static final int MESSAGE_LIMIT = 7;
    private static final int TIME_LIMIT_SECONDS = 60 * 5;

    private static final List<String> commandNames;
    private static final Map<Long, List<Long>> messageTimestamps = new HashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.isFromGuild())
            return;

        Message message = event.getMessage();
        String content = message.getContentRaw().toLowerCase();
        User author = event.getAuthor();
        Member member = message.getMember();
        long channelId = message.getChannelIdLong();

        if (!isOfftopic(content) || author.isBot())
            return;

        // "member" is null if the message was sent by a Webhook
        if (member == null || isStaff(member))
            return;

        if (channelId != BANK_CHANNEL_ID)
            return;

        LOGGER.warn("Member '{}' sent off-topic message at bank: {}", author.getName(), content);

        long memberId = member.getIdLong();
        long now = Bot.unixNow();
        List<Long> timestamps = messageTimestamps.getOrDefault(memberId, new ArrayList<>());

        // Remove timestamps older than 5 minutes
        timestamps.removeIf(timestamp -> now - timestamp > TIME_LIMIT_SECONDS);

        int messageCount = timestamps.size();

        if (messageCount == 6)
            message.reply("Use o <#743142441661759518> para conversas fora do tópico do banco.")
                    .failOnInvalidReply(true)
                    .delay(5, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));

        if (messageCount >= MESSAGE_LIMIT)
            message.delete().queue();

        timestamps.add(now);
        messageTimestamps.put(memberId, timestamps);
    }

    private boolean isOfftopic(String msg) {

        if (!msg.startsWith(")"))
            return true;

        String cmd = msg.substring(1).toLowerCase().strip();
        String[] args = cmd.split(" ");

        return !commandNames.contains(args[0]);
    }

    private boolean isStaff(Member member) {
        return member.hasPermission(Permission.ADMINISTRATOR) ||
                member.getRoles()
                        .stream()
                        .anyMatch(role -> role.getIdLong() == STAFF_ROLE_ID);
    }

    static {
        commandNames = List.of(
                "remind-me", "remindme", "remind", "set-reminder", "setreminder",
                "commands", "docs", "support-server", "supportserver", "rtfm",
                "provisions", "provision",
                "collect-income", "collectincome", "collect-role-income", "collectroleincome", "collect",
                "animal-race", "animalrace", "join-race", "joinrace",
                "blackjack", "bj",
                "roulette", "roulete", "roullete", "roullette",
                "list-reminders", "listreminders", "reminders", "reminder-list", "reminderlist", "show-reminders", "showreminders",
                "roulette-info", "rouletteinfo",
                "store", "shop",
                "forget-reminder", "delete-reminder", "reminder-forget", "reminder-delete", "forget",
                "cock-fight", "cockfight", "chicken-fight", "chickenfight", "fight-chicken", "fightchicken", "fight-cock", "fightcock", "c-f", "cf",
                "inventory", "inv",
                "russian-roulette", "russianroulette", "rr", "russian-roulete", "russianroulete", "russian-roullete", "russianroullete", "russian-roullette", "russianroullette", "rus-roulette", "rusroulette",
                "item-info", "iteminfo",
                "buy-item", "buyitem", "buy", "item-buy", "itembuy",
                "sell-item", "sellitem", "trade-item", "tradeitem", "sell",
                "use-item", "useitem", "use", "item-use", "itemuse",
                "work",
                "economy-stats", "economystats",
                "slut", // Disabled but let's not account on that forever ;-;
                "deposit", "dep",
                "crime",
                "withdraw", "with", "withdrawl", "widthdraw", "widthdrawl", "withdrawal",
                "rob",
                "roll",
                "give-money", "givemoney", "give", "money-give", "moneygive", "pay", "donate-money", "donatemoney",
                "money", "balance", "bal",
                "leaderboard", "lb", "top"
        );
    }
}