package ofc.bot.listeners.guilds.messages;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.content.annotations.listeners.EventHandler;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@EventHandler
public class SteamScamBlocker extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamScamBlocker.class);
    private static final long SAFE_ROLE = 1185468707116417134L;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {

        if (!event.isFromGuild())
            return;

        Member member = event.getMember();

        if (member == null)
            return;

        Message message = event.getMessage();
        User user = member.getUser();
        Channel channel = event.getChannel();
        Guild guild = message.getGuild();
        String content = message.getContentRaw().toLowerCase();
        Role safeRole = guild.getRoleById(SAFE_ROLE);
        int pos = safeRole == null ? 0 : safeRole.getPosition();

        if (isTrustworthy(member, pos) || user.isBot())
            return;

        if (!content.contains("https://") && !content.contains("http://"))
            return;

        if ((content.contains("50$") || content.contains("$50")) && content.contains("steam")) {
            LOGGER.info("Banned member @{} for suspicious message content at #{}: {}", user.getName(), channel.getName(), content);
            member.ban(1, TimeUnit.DAYS)
                    .reason("50$ from steam")
                    .queue();
        }
    }

    private boolean isTrustworthy(Member member, int minPos) {

        return member.getRoles()
                .stream()
                .anyMatch(r -> r.getPosition() >= minPos);
    }
}