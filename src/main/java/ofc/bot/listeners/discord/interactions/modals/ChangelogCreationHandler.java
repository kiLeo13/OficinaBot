package ofc.bot.listeners.discord.interactions.modals;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import ofc.bot.handlers.requests.RequestMapper;
import ofc.bot.handlers.requests.Route;
import ofc.bot.util.content.Channels;
import ofc.bot.util.content.Roles;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@DiscordEventHandler
public class ChangelogCreationHandler extends ListenerAdapter {

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void onModalInteraction(ModalInteractionEvent e) {
        String id = e.getModalId();
        if (!"changelog-entry".equals(id)) return;

        AtomicInteger increment = new AtomicInteger();
        NewsChannel chan = Channels.CHANGELOG.channel(NewsChannel.class);
        User user = e.getUser();
        String content = e.getValue("content").getAsString();
        List<FileUpload> imgs = resolveAttachments(e.getValue("attachments")).stream()
                .map(img -> FileUpload.fromData(img, String.format("image-%d.png", increment.getAndIncrement())))
                .toList();

        chan.sendMessageFormat("""
                %s
                
                -# — %s
                <@&%s>
                """, content, user.getName(), Roles.NOTIFY_BOT.id())
                .addFiles(imgs)
                .queue();

        e.reply("Enviado com sucesso!")
                .setEphemeral(true)
                .queue();
    }

    private List<InputStream> resolveAttachments(ModalMapping field) {
        String value = field == null ? "" : field.getAsString();
        if (value.isBlank()) return List.of();

        String[] values = value.split("\n");
        if (values.length > Message.MAX_FILE_AMOUNT)
            throw new IllegalArgumentException("Messages cannot contain more than 10 attachments");

        List<InputStream> imgs = new ArrayList<>(values.length);
        for (String url : values) {
            // We do not make this call in a try-catch with resources because it will be sent
            // to Discord, and JDA's Requester class closes this stream automatically once the request is complete.
            InputStream img = Route.get(url).create().send(RequestMapper::asInputStream);
            imgs.add(img);
        }
        return imgs;
    }
}