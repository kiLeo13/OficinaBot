package ofc.bot.listeners.logs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.annotations.listeners.EventHandler;
import ofc.bot.util.content.Channels;
import ofc.bot.util.content.Staff;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

@EventHandler
public class VoiceActivity extends ListenerAdapter {
    private static final int MAX_RETRIES = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceActivity.class);
    private static final long CHANNEL = Channels.A.id();
    private static final List<String> REQUIRED_ROLES = Staff.getIdsByArea(Staff.Field.MOV_CALL);
    
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {

        AudioChannelUnion channelJoined = event.getChannelJoined();
        AudioChannelUnion channelLeft = event.getChannelLeft();
        Member member = event.getMember();

        if (!shouldLog(member))
            return;

        // Leave
        if (channelJoined == null)
            send(null, channelLeft, event, VoiceAction.LEAVE);

        // Join
        else if (channelLeft == null)
            send(channelJoined, null, event, VoiceAction.JOIN);

        // Move
        else
            send(channelJoined, channelLeft, event, VoiceAction.MOVE);
    }

    @SuppressWarnings("DataFlowIssue")
    private void send(AudioChannel join, AudioChannel leave, GuildVoiceUpdateEvent event, VoiceAction action) {

        EmbedBuilder builder = new EmbedBuilder();
        Member member = event.getMember();
        User user = member.getUser();
        Guild guild = event.getGuild();
        TextChannel log = guild.getTextChannelById(CHANNEL);
        int joinAmount = join == null ? 0 : join.getMembers().size();
        int leaveAmount = leave == null ? 0 : leave.getMembers().size();

        if (log == null) {
            LOGGER.warn("Could not find log channel for id {}", CHANNEL);
            return;
        }

        switch (action)     {
            case JOIN -> builder
                    .setAuthor(user.getEffectiveName() + " entrou em " + join.getName(), null, user.getEffectiveAvatarUrl())
                    .addField("👥 Conectados", "`" + (joinAmount < 10 ? "0" + joinAmount : joinAmount) + "`", true)
                    .addField("👑 Staff", "`" + member.getId() + "`", true)
                    .addField("🔊 Canal", "`" + join.getId() + "`", true)
                    .setFooter(guild.getName(), guild.getIconUrl())
                    .setColor(Color.GREEN);

            case LEAVE -> builder
                    .setAuthor(user.getEffectiveName() + " saiu de " + leave.getName(), null, user.getEffectiveAvatarUrl())
                    .addField("👥 Conectados", "`" + (leaveAmount < 10 ? "0" + leaveAmount : leaveAmount) + "`", true)
                    .addField("👑 Staff", "`" + member.getId() + "`", true)
                    .addField("🔊 Canal", "`" + leave.getId() +  "`", true)
                    .setFooter(guild.getName(), guild.getIconUrl())
                    .setColor(Color.RED);

            // User moved from one voice channel to another
            default -> builder
                    .setAuthor(user.getEffectiveName() + " se mudou para " + join.getName(), null, user.getEffectiveAvatarUrl())
                    .addField("🔈 Saiu de", "`" + leave.getName() + "`\n`" + leave.getId() + "`", true)
                    .addField("👥 Conectados", "Anterior: `" + (leaveAmount < 10 ? "0" + leaveAmount : leaveAmount) + "`\nAtual: `" + (joinAmount < 10 ? "0" + joinAmount : joinAmount) + "`", true)
                    .addField("👑 Staff", "`" + member.getId() + "`", true)
                    .addField("🔊 Canais", "`" + join.getId() + " -> " + leave.getId() + "`", true)
                    .setFooter(guild.getName(), guild.getIconUrl())
                    .setColor(Color.YELLOW);
        }

        send(log, join, leave, member, builder.build(), 0);
    }
    
    private void send(TextChannel log, AudioChannel join, AudioChannel leave, Member target, MessageEmbed embed, int retries) {
        int nextRetry = retries + 1;

        log.sendMessageEmbeds(embed).queue((msg) -> {

            if (retries != 0)
                LOGGER.info("Retry successfully sent the message!");
                
        }, (error) -> {
            
            LOGGER.error("Could not log activity of member '{}' from '{}' to '{}' because {}",
                    target.getId(),
                    leave == null ? "unknown" : leave.getId(),
                    join == null ? "unknown" : join.getId(),
                    error.getMessage()
            );

            if (nextRetry > MAX_RETRIES) {
                LOGGER.error("Retries failed more than {} times??? WHAT? Okay, aborting it.", MAX_RETRIES);
                return;
            }

            LOGGER.warn("Retrying...");
            LOGGER.warn("Going to #{} retry", nextRetry);

            send(log, join, leave, target, embed, nextRetry);
        });
    }

    private boolean shouldLog(Member member) {
        for (Role role : member.getRoles())
            if (REQUIRED_ROLES.contains(role.getId()))
                return true;
        return false;
    }

    private enum VoiceAction {
        JOIN,
        LEAVE,
        MOVE
    }
}