package ofc.bot.handlers.interactions.commands.responses;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.interactions.AcknowledgeableAction;
import ofc.bot.handlers.interactions.commands.contexts.MultipleResponsesPolicy;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InteractionResponseBuilder implements InteractionResponseData {
    private final AcknowledgeableAction<?> acknowledgeable;
    private final MessageCreateBuilder builder;
    private boolean ephemeral;

    public InteractionResponseBuilder(AcknowledgeableAction<?> acknowledgeable) {
        super();

        Checks.notNull(acknowledgeable, "Acknowledgeable Interaction");

        this.builder = new MessageCreateBuilder();
        this.ephemeral = false;
        this.acknowledgeable = acknowledgeable;
    }

    public InteractionResponseBuilder setEphemeral(boolean flag) {
        this.ephemeral = flag;
        return this;
    }

    public InteractionResponseBuilder setContent(String content) {
        builder.setContent(content);
        return this;
    }

    public InteractionResponseBuilder setContent(InteractionResult res) {
        return setContent(res.getContent());
    }

    public InteractionResponseBuilder setContentFormat(String format, Object... args) {
        return setContent(String.format(format, args));
    }

    public InteractionResponseBuilder setEmbeds(MessageEmbed... embeds) {
        builder.setEmbeds(embeds);
        return this;
    }

    public InteractionResponseBuilder setAllowedMentions(Collection<Message.MentionType> mentions) {
        builder.setAllowedMentions(mentions);
        return this;
    }

    public InteractionResponseBuilder noMentions() {
        return setAllowedMentions(Collections.emptyList());
    }

    public InteractionResponseBuilder mentionUsers(long... ids) {
        builder.mentionUsers(ids);
        return this;
    }

    public InteractionResponseBuilder mentionRoles(long... ids) {
        builder.mentionRoles(ids);
        return this;
    }

    public InteractionResponseBuilder setFiles(FileUpload... files) {
        builder.setFiles(files);
        return this;
    }

    public InteractionResponseBuilder setComponents(LayoutComponent... components) {
        builder.setComponents(components);
        return this;
    }

    public InteractionResponseBuilder setActionRow(List<? extends ItemComponent> components) {
        builder.setActionRow(components);
        return this;
    }

    public InteractionResponseBuilder setActionRow(ItemComponent... components) {
        builder.setActionRow(components);
        return this;
    }

    @Override
    public InteractionResult send(InteractionResult res) {
        acknowledgeable.reply(this);
        return res;
    }

    @Override
    public InteractionResult edit(InteractionResult res) {
        acknowledgeable.setMultipleResponsesPolicy(MultipleResponsesPolicy.EDIT);
        return send(res);
    }

    @Override
    public boolean isEphemeral() {
        return this.ephemeral;
    }

    @NotNull
    @Override
    public String getContent() {
        return builder.getContent();
    }

    @NotNull
    @Override
    public List<MessageEmbed> getEmbeds() {
        return builder.getEmbeds();
    }

    @NotNull
    @Override
    public List<LayoutComponent> getComponents() {
        return builder.getComponents();
    }

    @NotNull
    @Override
    public List<FileUpload> getAttachments() {
        return builder.getAttachments();
    }

    @NotNull
    @Override
    public MessageCreateData toCreateData() {
        return builder.build();
    }

    @NotNull
    @Override
    public MessageEditData toEditData() {
        return MessageEditData.fromCreateData(toCreateData());
    }

    @Override
    public boolean isSuppressEmbeds() {
        return builder.isSuppressEmbeds();
    }

    @NotNull
    @Override
    public Set<String> getMentionedUsers() {
        return builder.getMentionedUsers();
    }

    @NotNull
    @Override
    public Set<String> getMentionedRoles() {
        return builder.getMentionedRoles();
    }

    @NotNull
    @Override
    public EnumSet<Message.MentionType> getAllowedMentions() {
        return builder.getAllowedMentions();
    }

    @Override
    public boolean isMentionRepliedUser() {
        return builder.isMentionRepliedUser();
    }
}