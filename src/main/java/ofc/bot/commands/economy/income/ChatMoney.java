package ofc.bot.commands.economy.income;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.databases.users.MembersDAO;
import ofc.bot.util.EconomyUtil;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Random;

@EventHandler
public class ChatMoney extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatMoney.class);
    private static final Random random = new Random();
    private static final int PERIOD = 15000;
    private static final int MAX = 10;
    private static final HashMap<Long, Long> cooldown = new HashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        User author = event.getAuthor();
        long userId = author.getIdLong();
        boolean isCooldown = isCooldown(userId);

        if (author.isBot() || isCooldown || !event.isFromGuild())
            return;

        long now = System.currentTimeMillis();
        int amount = getRandom();

        try {
            EconomyUtil.updateBalance(userId, amount);
            MembersDAO.upsertUser(author);

            cooldown.put(userId, now);
        } catch (DataAccessException e) {
            LOGGER.error("Could not update money of user '{}' by chat money", userId, e);
        }
    }

    private int getRandom() {
        return random.nextInt(1, MAX + 1);
    }

    private boolean isCooldown(long userId) {

        if (!cooldown.containsKey(userId))
            return false;

        long now = System.currentTimeMillis();
        long lastEarnedMessage = cooldown.get(userId);

        return now - lastEarnedMessage <= PERIOD;
    }
}