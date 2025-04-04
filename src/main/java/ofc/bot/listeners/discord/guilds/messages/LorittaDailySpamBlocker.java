package ofc.bot.listeners.discord.guilds.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

@DiscordEventHandler
public class LorittaDailySpamBlocker extends ListenerAdapter {
    private static final long LORITTA_ID = 297153970613387264L;
    private static final String DAILY_URL = "https://loritta.website/daily";
    private static final String SHOP_URL = "https://loritta.website/dashboard/sonhos-shop";

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Message msg = e.getMessage();
        MessageReference msgRef = msg.getMessageReference();
        String content = msg.getContentRaw().toLowerCase();
        User author = msg.getAuthor();
        Status commandStatus = resolveStatus(content);
        long userId = author.getIdLong();

        if (userId != LORITTA_ID || msgRef == null) return;

        if (commandStatus == Status.UNKNOWN) return;

        msgRef.resolve().queue(ref -> {
            if (commandStatus == Status.OK) {
                ref.reply(getSuccessMessage()).queue();
            } else {
                ref.reply("Você já coletou sua recompensa diária hoje.").queue();
            }
        });
        Bot.delete(msg);
    }

    private String getSuccessMessage() {
        return String.format("""
                **Link do Daily:** [`Loritta Website - Daily`](<%s>).
                **Link da Loja:** [`Loritta Website - Shop`](<%s>).
                """, DAILY_URL, SHOP_URL);
    }

    private Status resolveStatus(String content) {
        if (content.contains("receba a sua recompensa diária de sonhos aqui")) return Status.OK;
        if (content.contains("já recebeu a sua recompensa")) return Status.COLLECTED;
        return Status.UNKNOWN;
    }

    private enum Status {
        OK,
        COLLECTED,
        UNKNOWN
    }
}