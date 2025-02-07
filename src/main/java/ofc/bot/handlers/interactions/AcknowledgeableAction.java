package ofc.bot.handlers.interactions;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.interactions.commands.contexts.MultipleResponsesPolicy;
import ofc.bot.handlers.interactions.commands.responses.InteractionResponseBuilder;
import ofc.bot.handlers.interactions.commands.responses.InteractionResponseData;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

public abstract class AcknowledgeableAction<T extends Interaction & IReplyCallback> {
    public static final MultipleResponsesPolicy DEFAULT_MULTIPLE_RESPONSES_POLICY = MultipleResponsesPolicy.EDIT;
    private static final Consumer<Throwable> DEFAULT_ERROR_HANDLER = (e) -> new ErrorHandler().ignore(ErrorResponse.UNKNOWN_INTERACTION);
    private final T itr;
    private final Member issuer;
    private final Guild guild;
    private MultipleResponsesPolicy multipleResponsesPolicy;

    public AcknowledgeableAction(T interaction) {
        this.itr = interaction;
        this.issuer = interaction.getMember();
        this.guild = interaction.getGuild();
        this.multipleResponsesPolicy = DEFAULT_MULTIPLE_RESPONSES_POLICY;

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
     * Sometimes your command may need to send more than one response,
     * somtimes you just want to edit the one you sent previously.
     * <p>
     * Here you can set this behavior:
     * <ul>
     *   <li>{@link MultipleResponsesPolicy#EDIT} will edit the last message, on calling {@link AcknowledgeableAction#create()} multiple times.</li>
     *   <li>
     *     {@link MultipleResponsesPolicy#SEND} will send a new response
     *      when calling {@link AcknowledgeableAction#create()} multiple times.
     *   </li>
     * </ul>
     * <p>
     * When the policy is set to {@link MultipleResponsesPolicy#SEND} you <b>CAN</b> use
     * features like ephemeral messages or modals. Since this entity will be treated
     * as a new response.
     * <p>
     * The default value is defined at {@link #DEFAULT_MULTIPLE_RESPONSES_POLICY}.
     *
     * @param policy The new policy to be set.
     */
    public final void setMultipleResponsesPolicy(@NotNull MultipleResponsesPolicy policy) {
        this.multipleResponsesPolicy = policy;
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
    public final InteractionResult reply(@NotNull String content) {
        return reply(content, false);
    }

    @NotNull
    public final InteractionResult replyEmbeds(boolean ephemeral, @NotNull MessageEmbed... embeds) {
        return create(ephemeral).setEmbeds(embeds).send();
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
    public final InteractionResult reply(@NotNull String content, boolean ephemeral) {
        return create(ephemeral).setContent(content).send();
    }

    @NotNull
    public final InteractionResult replyEmbeds(@NotNull MessageEmbed... embeds) {
        return replyEmbeds(false, embeds);
    }

    public final InteractionResult replyFiles(@NotNull FileUpload... files) {
        return create().setFiles(files).send();
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
        setMultipleResponsesPolicy(MultipleResponsesPolicy.EDIT);
        return reply(data);
    }

    @NotNull
    public final InteractionResult reply(InteractionResponseData data) {
        if (!isAcknowledged()) {
            itr.reply(data.toCreateData())
                    .setEphemeral(data.isEphemeral())
                    .queue();
            return Status.OK;
        }

        if (multipleResponsesPolicy == MultipleResponsesPolicy.EDIT) {
            itr.getHook()
                    .editOriginal(data.toEditData())
                    .queue(null, DEFAULT_ERROR_HANDLER);
        } else {
            itr.getHook()
                    .sendMessage(data.toCreateData())
                    .setEphemeral(data.isEphemeral())
                    .queue(null, DEFAULT_ERROR_HANDLER);
        }
        return Status.OK;
    }
}