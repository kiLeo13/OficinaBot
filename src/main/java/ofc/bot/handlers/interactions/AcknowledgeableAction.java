package ofc.bot.handlers.interactions;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.interactions.commands.responses.InteractionResponseBuilder;
import ofc.bot.handlers.interactions.commands.responses.InteractionResponseData;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

public abstract class AcknowledgeableAction<T extends Interaction & IReplyCallback> {
    private final T itr;
    private final Member issuer;
    private final Guild guild;

    public AcknowledgeableAction(T interaction) {
        this.itr = interaction;
        this.issuer = interaction.getMember();
        this.guild = interaction.getGuild();

        Checks.notNull(this.issuer, "Interaction issuer");
        Checks.notNull(this.guild, "Interaction guild");
    }

    /**
     * The id of the entity, usually an {@link Interaction}.
     *
     * @return The unique id of the entity.
     */
    public final long getId() {
        return this.itr.getIdLong();
    }

    @NotNull
    public final MessageChannel getChannel() {
        return this.itr.getMessageChannel();
    }

    public final long getChannelId() {
        return getChannel().getIdLong();
    }

    @NotNull
    public final Member getIssuer() {
        return this.issuer;
    }

    @NotNull
    public final User getUser() {
        return this.itr.getUser();
    }

    public final long getUserId() {
        return getUser().getIdLong();
    }

    @NotNull
    public final Guild getGuild() {
        return this.guild;
    }

    public final long getGuildId() {
        return getGuild().getIdLong();
    }

    @NotNull
    public final OffsetDateTime getTimeCreated() {
        return TimeUtil.getTimeCreated(this::getId);
    }

    public boolean isAcknowledged() {
        return this.itr.isAcknowledged();
    }

    @NotNull
    public T getInteraction() {
        return this.itr;
    }

    /**
     * Acknowledges the interaction by sending a "{@code <Bot> is thinking...}".
     * <p>
     * This method has no effect if the interaction has been already acknowledged.
     *
     * @param ephemeral Whether this message should only be visible to the interaction user.
     */
    public abstract void ack(boolean ephemeral);

    /**
     * Acknowledges the interaction by sending a "{@code <Bot> is thinking...}".
     * <p>
     * This is non-ephemeral always. If you want to acknowledge an interaction
     * ephemerally, use {@link AcknowledgeableAction#ack(boolean) IAcknowledgeable.ack(true)} instead.
     * <p>
     * This method has no effect if the interaction has been already acknowledged.
     *
     * @see AcknowledgeableAction#ack(boolean)
     */
    public final void ack() {
        ack(false);
    }

    @NotNull
    public final InteractionResult replyFormat(@NotNull String format, @NotNull Object... args) {
        return reply(String.format(format, args));
    }

    /**
     * Creates a new response builder to this interaction.
     * <p>
     * If the final operation is a call to {@link #edit(InteractionResponseData)},
     * the {@code ephemeral} argument will be ignored, as you cannot change this
     * property after the response is sent.
     *
     * @param ephemeral whether the response should be an ephemeral message.
     * @return a new {@link InteractionResponseBuilder} instance to better structure the response.
     */
    @NotNull
    public final InteractionResponseBuilder create(boolean ephemeral) {
        return new InteractionResponseBuilder(this)
                .setEphemeral(ephemeral);
    }

    @NotNull
    public final InteractionResponseBuilder create() {
        return create(false);
    }

    @NotNull
    public final InteractionResult reply(@NotNull String content, boolean ephemeral) {
        return create(ephemeral).setContent(content).send();
    }

    @NotNull
    public final InteractionResult reply(@NotNull String content) {
        return reply(content, false);
    }

    public final InteractionResult edit(@NotNull String content) {
        return create().setContent(content).edit();
    }

    @NotNull
    public final InteractionResult replyEmbeds(boolean ephemeral, @NotNull MessageEmbed... embeds) {
        return create(ephemeral).setEmbeds(embeds).send();
    }

    public final InteractionResult editEmbeds(@NotNull MessageEmbed... embeds) {
        return create().setEmbeds(embeds).edit();
    }

    @NotNull
    public final InteractionResult reply(InteractionResult res, boolean ephemeral) {
        return reply(res.getContent(), ephemeral);
    }

    @NotNull
    public final InteractionResult reply(InteractionResult result) {
        return reply(result.getContent(), result.isEphemeral());
    }

    @NotNull
    public final InteractionResult edit(InteractionResult res) {
        return edit(res.getContent());
    }

    @NotNull
    public final InteractionResult replyEmbeds(@NotNull MessageEmbed... embeds) {
        return replyEmbeds(false, embeds);
    }

    @NotNull
    public final InteractionResult replyFiles(@NotNull FileUpload... files) {
        return create().setFiles(files).send();
    }

    @NotNull
    public final InteractionResult editFiles(@NotNull FileUpload... files) {
        return create().setFiles().send();
    }

    public final InteractionResult replyFile(@NotNull byte[] data, @NotNull String fileName) {
        return replyFiles(FileUpload.fromData(data, fileName));
    }

    @NotNull
    public final InteractionResult replyModal(@NotNull Modal modal) {
        if (isAcknowledged())
            throw new UnsupportedOperationException("Cannot reply a Modal to an acknowledged interaction");

        if (!(itr instanceof IModalCallback modalItr))
            throw new UnsupportedOperationException("This interaction does not support Modals");

        modalItr.replyModal(modal).queue();
        return Status.OK;
    }

    @NotNull
    public final InteractionResult edit(InteractionResponseData data) {
        itr.getHook()
                .editOriginal(data.toEditData())
                .queue(data.getSuccessHook(), data.getFailureHook());

        return Status.OK;
    }

    @NotNull
    public final InteractionResult reply(InteractionResponseData data) {
        if (!isAcknowledged()) {
            itr.reply(data.toCreateData())
                    .setEphemeral(data.isEphemeral())
                    .queue(data.getSuccessSend(), data.getFailureSend());
        } else {
            itr.getHook()
                    .sendMessage(data.toCreateData())
                    .setEphemeral(data.isEphemeral())
                    .queue(data.getSuccessHook(), data.getFailureHook());
        }
        return Status.OK;
    }
}