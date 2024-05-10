package ofc.bot.listeners.guilds.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.annotations.listeners.EventHandler;
import ofc.bot.handlers.economy.UEconomyManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EventHandler
public class ErikBalanceUpdate extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ErikBalanceUpdate.class);

    private static final long GUILD_ID = 582430782577049600L;
    private static final long ERIK_ID = 145607100100247552L;

    private static final List<String> payAlises = List.of(
            "give", "money-give", "pay", "donate-money"
    );

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        Message message = event.getMessage();
        String content = message.getContentRaw().toLowerCase();
        String channelId = message.getChannelId();
        User author = event.getAuthor();
        String authorId = author.getId();

        if (content.isBlank())
            return;

        if (!content.contains("145607100100247552") && !content.contains("erikcarr"))
            return;

        // Definitely not an UnbelievaBoat command
        if (!content.startsWith(")") && !content.startsWith("<@356950275044671499>"))
            return;

        String[] args = content.split(" ");

        for (String arg : args) {

            if (payAlises.contains(arg)) {
                UEconomyManager.resetBalance(GUILD_ID, ERIK_ID);
                logger.warn("User '{}' issued at channel id '{}' '{}' and erikcarr's balance has been reset", authorId, channelId, content);
                return;
            }
        }
    }
}