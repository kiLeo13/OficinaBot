package ofc.bot.handlers.commands.contexts;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import ofc.bot.handlers.commands.responses.SlashCommandResponseBuilder;
import ofc.bot.handlers.commands.responses.InteractionResponseData;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import org.jetbrains.annotations.NotNull;

public interface IAcknowledgeable<T extends Interaction> {

    boolean isAcknowledged();

    @NotNull
    T getInteraction();

    /**
     * Acknowledges the interaction by sending a "{@code <Bot> is thinking...}".
     * <p>
     * This method has no effect if the interaction has been already acknowledged.
     *
     * @param ephemeral Whether this message should only be visible to the interaction user.
     */
    void ack(boolean ephemeral);

    /**
     * Acknowledges the interaction by sending a "{@code <Bot> is thinking...}".
     * <p>
     * This is non-ephemeral always. If you want to acknowledge an interaction
     * ephemerally, use {@link IAcknowledgeable#ack(boolean) IAcknowledgeable.ack(true)} instead.
     * <p>
     * This method has no effect if the interaction has been already acknowledged.
     *
     * @see IAcknowledgeable#ack(boolean)
     */
    default void ack() {
        ack(false);
    }

    default void replyFormat(@NotNull String format, @NotNull Object... args) {
        reply(String.format(format, args));
    }

    @NotNull
    default SlashCommandResponseBuilder reply(boolean ephemeral) {
        return new SlashCommandResponseBuilder(this)
                .setEphemeral(ephemeral);
    }

    @NotNull
    default SlashCommandResponseBuilder reply() {
        return reply(false);
    }

    default void reply(@NotNull String content) {
        reply(content, false);
    }

    default void replyEmbeds(boolean ephemeral, @NotNull MessageEmbed... embeds) {
        reply(ephemeral).setEmbeds(embeds).send();
    }

    void reply(CommandResult result);

    void reply(@NotNull String content, boolean ephemeral);

    void replyEmbeds(@NotNull MessageEmbed... embeds);

    void replyFiles(@NotNull FileUpload... files);

    void replyModal(@NotNull Modal modal);

    void reply(InteractionResponseData data);
}