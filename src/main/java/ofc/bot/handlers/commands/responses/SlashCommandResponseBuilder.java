package ofc.bot.handlers.commands.responses;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.commands.contexts.IAcknowledgeable;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SlashCommandResponseBuilder implements InteractionResponseData {
    private final IAcknowledgeable<?> acknowledgeable;
    private final MessageCreateBuilder builder;
    private boolean ephemeral;

    public SlashCommandResponseBuilder(IAcknowledgeable<?> acknowledgeable) {
        super();

        Checks.notNull(acknowledgeable, "Acknowledgeable Slash Command");

        this.builder = new MessageCreateBuilder();
        this.ephemeral = false;
        this.acknowledgeable = acknowledgeable;
    }

    public SlashCommandResponseBuilder setEphemeral(boolean flag) {
        this.ephemeral = flag;
        return this;
    }

    public SlashCommandResponseBuilder setContent(String content) {
        builder.setContent(content);
        return this;
    }

    public SlashCommandResponseBuilder setContentFormat(String format, Object... args) {
        return setContent(String.format(format, args));
    }

    public SlashCommandResponseBuilder setEmbeds(MessageEmbed... embeds) {
        builder.setEmbeds(embeds);
        return this;
    }

    public SlashCommandResponseBuilder setAllowedMentions(Collection<Message.MentionType> mentions) {
        builder.setAllowedMentions(mentions);
        return this;
    }

    public SlashCommandResponseBuilder mentionUsers(long... ids) {
        builder.mentionUsers(ids);
        return this;
    }

    public SlashCommandResponseBuilder mentionRoles(long... ids) {
        builder.mentionRoles(ids);
        return this;
    }

    public SlashCommandResponseBuilder setFiles(FileUpload... files) {
        builder.setFiles(files);
        return this;
    }

    public SlashCommandResponseBuilder setComponents(LayoutComponent... components) {
        builder.setComponents(components);
        return this;
    }

    public SlashCommandResponseBuilder addComponents(LayoutComponent... components) {
        builder.addComponents(components);
        return this;
    }

    public SlashCommandResponseBuilder setActionRow(ItemComponent... components) {
        builder.setActionRow(components);
        return this;
    }

    @Override
    public CommandResult send(Status status, Object... args) {
        acknowledgeable.reply(this);
        return status.args(args);
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