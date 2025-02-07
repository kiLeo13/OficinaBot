package ofc.bot.listeners.discord.interactions.modals;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.Channels;
import ofc.bot.util.content.Roles;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

@DiscordEventHandler
public class ChangelogCreationHandler extends ListenerAdapter {

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void onModalInteraction(ModalInteractionEvent e) {
        String id = e.getModalId();
        if (!"changelog-entry".equals(id)) return;

        NewsChannel chan = Channels.CHANGELOG.channel(NewsChannel.class);
        User user = e.getUser();
        String content = e.getValue("content").getAsString();

        chan.sendMessageFormat("""
                %s
                
                -# — %s
                <@&%s>
                """, content, user.getName(), Roles.NOTIFY_BOT.id()).queue();

        e.reply("Enviado com sucesso!")
                .setEphemeral(true)
                .queue();
    }
}