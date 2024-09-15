package ofc.bot.handlers.commands.responses;

import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface InteractionResponseData extends MessageData {

    @NotNull
    List<FileUpload> getAttachments();

    @NotNull
    MessageCreateData toCreateData();

    @NotNull
    MessageEditData toEditData();

    boolean isEphemeral();

    CommandResult send(Status status, Object... args);

    default CommandResult send() {
        return send(Status.PASSED);
    }
}