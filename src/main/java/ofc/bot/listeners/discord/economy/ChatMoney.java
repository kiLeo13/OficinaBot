package ofc.bot.listeners.discord.economy;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.UserEconomy;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.events.impl.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Random;

@DiscordEventHandler
public class ChatMoney extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatMoney.class);
    private static final Random random = new Random();
    private static final int PERIOD = 15000;
    private static final int MAX_AMOUNT = 10;
    private static final HashMap<Long, Long> cooldown = new HashMap<>();
    private final UserEconomyRepository ecoRepo;

    public ChatMoney(UserEconomyRepository ecoRepo) {
        this.ecoRepo = ecoRepo;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        long userId = author.getIdLong();
        boolean isCooldown = isCooldown(userId);

        if (author.isBot() || isCooldown || !event.isFromGuild()) return;

        long now = System.currentTimeMillis();
        int amount = getRandom();

        try {
            UserEconomy userEconomy = ecoRepo.findByUserId(userId, UserEconomy.fromUserId(userId))
                    .modifyBalance(amount)
                    .tickUpdate();
            ecoRepo.upsert(userEconomy);

            dispatchChatMoneyEvent(userId, amount);
            cooldown.put(userId, now);
        } catch (DataAccessException e) {
            LOGGER.error("Could not update money of user '{}' by chat money", userId, e);
        }
    }

    private int getRandom() {
        return random.nextInt(1, MAX_AMOUNT + 1);
    }

    private boolean isCooldown(long userId) {
        if (!cooldown.containsKey(userId))
            return false;

        long now = System.currentTimeMillis();
        long lastEarnedMessage = cooldown.get(userId);

        return now - lastEarnedMessage <= PERIOD;
    }

    private void dispatchChatMoneyEvent(long userId, int amount) {
        BankTransaction tr = new BankTransaction(userId, amount, CurrencyType.OFICINA, TransactionType.CHAT_MONEY);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}