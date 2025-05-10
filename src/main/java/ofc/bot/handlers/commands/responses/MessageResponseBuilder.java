package ofc.bot.handlers.commands.responses;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import ofc.bot.handlers.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.actions.AcknowledgeableAction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class MessageResponseBuilder implements ResponseData<Message> {
    private static final Consumer<Throwable> DEFAULT_ERROR_HANDLER = new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE);
    private final MessageCreateBuilder builder;
    private final AcknowledgeableAction<Message, ?, ?> acknowledgeable;
    private Consumer<Message> successSend;
    private Consumer<Throwable> failureSend = DEFAULT_ERROR_HANDLER;

    public MessageResponseBuilder(AcknowledgeableAction<Message, ?, ?> acknowledgeable) {
        this.builder = new MessageCreateBuilder();
        this.acknowledgeable = acknowledgeable;
    }

    public MessageResponseBuilder onSend(Consumer<Message> success, Consumer<Throwable> failure) {
        this.successSend = success;
        this.failureSend = failure == null ? DEFAULT_ERROR_HANDLER : failure;
        return this;
    }

    @NotNull
    public MessageResponseBuilder setContent(String content) {
        builder.setContent(content);
        return this;
    }

    @NotNull
    public MessageResponseBuilder setContent(InteractionResult res) {
        return setContent(res.getContent());
    }

    @NotNull
    public MessageResponseBuilder setContentFormat(String format, Object... args) {
        return setContent(String.format(format, args));
    }

    @NotNull
    public MessageResponseBuilder setEmbeds(MessageEmbed... embeds) {
        builder.setEmbeds(embeds);
        return this;
    }

    @NotNull
    public MessageResponseBuilder setAllowedMentions(Collection<Message.MentionType> mentions) {
        builder.setAllowedMentions(mentions);
        return this;
    }

    @NotNull
    public MessageResponseBuilder noMentions() {
        return setAllowedMentions(List.of());
    }

    @NotNull
    public MessageResponseBuilder mentionUsers(long... ids) {
        builder.mentionUsers(ids);
        return this;
    }

    @NotNull
    public MessageResponseBuilder mentionRoles(long... ids) {
        builder.mentionRoles(ids);
        return this;
    }

    @NotNull
    public MessageResponseBuilder setFiles(FileUpload... files) {
        builder.setFiles(files);
        return this;
    }

    @NotNull
    public MessageResponseBuilder setComponents(LayoutComponent... components) {
        builder.setComponents(components);
        return this;
    }

    @NotNull
    public MessageResponseBuilder setComponents(List<? extends LayoutComponent> components) {
        builder.setComponents(components);
        return this;
    }

    @NotNull
    public MessageResponseBuilder setActionRow(List<? extends ItemComponent> components) {
        builder.setActionRow(components);
        return this;
    }

    @NotNull
    public MessageResponseBuilder setActionRow(ItemComponent... components) {
        builder.setActionRow(components);
        return this;
    }

    @NotNull
    @Override
    public String getContent() {
        return this.builder.getContent();
    }

    @NotNull
    @Override
    public List<MessageEmbed> getEmbeds() {
        return this.builder.getEmbeds();
    }

    @NotNull
    @Override
    public List<LayoutComponent> getComponents() {
        return this.builder.getComponents();
    }

    @Override
    public @NotNull List<FileUpload> getAttachments() {
        return this.builder.getAttachments();
    }

    @Override
    public boolean isSuppressEmbeds() {
        return this.builder.isSuppressEmbeds();
    }

    @NotNull
    @Override
    public Set<String> getMentionedUsers() {
        return this.builder.getMentionedUsers();
    }

    @NotNull
    @Override
    public Set<String> getMentionedRoles() {
        return this.builder.getMentionedRoles();
    }

    @NotNull
    @Override
    public EnumSet<Message.MentionType> getAllowedMentions() {
        return this.builder.getAllowedMentions();
    }

    @Override
    public boolean isMentionRepliedUser() {
        return this.builder.isMentionRepliedUser();
    }

    @Override
    public @NotNull MessageCreateData toCreateData() {
        return this.builder.build();
    }

    @Override
    public @NotNull MessageEditData toEditData() {
        return MessageEditData.fromCreateData(this.builder.build());
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public Consumer<Message> getSuccessHook() {
        return this.successSend;
    }

    @Override
    public Consumer<Message> getSuccessSend() {
        return this.successSend;
    }

    @Override
    public Consumer<Throwable> getFailureHook() {
        return this.failureSend;
    }

    @Override
    public Consumer<Throwable> getFailureSend() {
        return this.failureSend;
    }

    @Override
    public InteractionResult send(InteractionResult res) {
        return this.acknowledgeable.reply(res);
    }

    @Override
    public InteractionResult edit(InteractionResult res) {
        return this.acknowledgeable.edit(res);
    }
}