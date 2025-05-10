package ofc.bot.handlers.commands.contexts.impl;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.commands.contexts.OptionsContainer;
import ofc.bot.handlers.commands.options.ArgumentMapper;
import ofc.bot.handlers.commands.options.LegacyOption;
import ofc.bot.handlers.commands.responses.MessageResponseBuilder;
import ofc.bot.handlers.commands.responses.ResponseData;
import ofc.bot.handlers.commands.responses.states.InteractionResult;
import ofc.bot.handlers.commands.responses.states.Status;
import ofc.bot.handlers.interactions.actions.AcknowledgeableAction;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageCommandContext
        implements AcknowledgeableAction<Message, Message, MessageResponseBuilder>, OptionsContainer<ArgumentMapper> {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"([^\"]*)\"|(\\S+)");
    private final Message message;
    private final Member issuer;
    private final Map<String, ArgumentMapper> args;

    public MessageCommandContext(Message message, List<LegacyOption> opts) {
        this.message = message;
        this.issuer = message.getMember();
        this.args = resolveArguments(message, opts);
        Checks.notNull(this.issuer, "Member");
    }

    @Override
    public boolean hasOption(String name) {
        return this.args.containsKey(name);
    }

    @Override
    public boolean hasOptions() {
        return !this.args.isEmpty();
    }

    @Override
    public <T> T getOption(@NotNull String name, T fallback, @NotNull Function<? super ArgumentMapper, ? extends T> resolver) {
        ArgumentMapper mapper = this.args.get(name);
        return mapper == null ? null : resolver.apply(mapper);
    }

    @Override
    public <T extends Enum<T>> T getEnumOption(@NotNull String name, @NotNull Function<String, T> resolver) {
        String val = getOption(name, ArgumentMapper::getAsString);
        return val == null ? null : resolver.apply(val);
    }

    @Override
    public long getId() {
        return this.message.getIdLong();
    }

    @Override
    public @NotNull MessageChannel getChannel() {
        return this.message.getChannel();
    }

    @Override
    public long getChannelId() {
        return this.message.getChannelIdLong();
    }

    @Override
    public @NotNull Member getIssuer() {
        return this.issuer;
    }

    @Override
    public @NotNull User getUser() {
        return this.message.getAuthor();
    }

    @Override
    public long getUserId() {
        return this.message.getAuthor().getIdLong();
    }

    @Override
    public @NotNull Guild getGuild() {
        return this.issuer.getGuild();
    }

    @Override
    public long getGuildId() {
        return this.issuer.getGuild().getIdLong();
    }

    @Override
    public @NotNull OffsetDateTime getTimeCreated() {
        return this.message.getTimeCreated();
    }

    @Override
    public boolean isAcknowledged() {
        return false;
    }

    @Override
    public @NotNull Message getSource() {
        return this.message;
    }

    @Override
    public void ack(boolean ephemeral) {
        getChannel().sendTyping().queue();
    }

    @Override
    public @NotNull MessageResponseBuilder create(boolean ephemeral) {
        return new MessageResponseBuilder(this);
    }

    @Override
    public @NotNull InteractionResult reply(@NotNull String content, boolean ephemeral) {
        return this.create(false).setContent(content).send();
    }

    @Override
    public @NotNull InteractionResult reply(@NotNull String content) {
        return reply(content, false);
    }

    @Override
    public @NotNull InteractionResult edit(@NotNull String content) {
        return this.create(false).setContent(content).edit();
    }

    @Override
    public @NotNull InteractionResult replyEmbeds(boolean ephemeral, @NotNull MessageEmbed... embeds) {
        return this.replyEmbeds(Status.OK, ephemeral, embeds);
    }

    @Override
    public @NotNull InteractionResult replyEmbeds(InteractionResult result, boolean ephemeral, @NotNull MessageEmbed... embeds) {
        return this.create(false).setEmbeds(embeds).send(result);
    }

    @Override
    public @NotNull InteractionResult editEmbeds(@NotNull MessageEmbed... embeds) {
        return this.create(false).setEmbeds(embeds).edit();
    }

    @Override
    public @NotNull InteractionResult replyFiles(@NotNull FileUpload... files) {
        return this.create().setFiles(files).send();
    }

    @Override
    public @NotNull InteractionResult editFiles(@NotNull FileUpload... files) {
        return this.create().setFiles(files).edit();
    }

    @Override
    public @NotNull InteractionResult replyModal(@NotNull Modal modal) {
        throw new UnsupportedOperationException("This command type (Legacy/Message) does not support Modals");
    }

    @Override
    public @NotNull InteractionResult edit(ResponseData<Message> data) {
        this.message.editMessage(data.toEditData())
                .queue(data.getSuccessSend(), data.getFailureSend());
        return Status.OK;
    }

    @Override
    public @NotNull InteractionResult reply(ResponseData<Message> data) {
        this.message.reply(data.toCreateData())
                .queue(data.getSuccessSend(), data.getFailureSend());

        return Status.OK;
    }

    private Map<String, ArgumentMapper> resolveArguments(Message msg, List<LegacyOption> opts) {
        List<String> tokens = tokenize(msg.getContentRaw());
        if (tokens.size() <= 1) {
            return Map.of(); // No args
        }

        // Remove command name (assumed first token)
        List<String> argsContent = tokens.subList(1, tokens.size());
        Map<String, ArgumentMapper> args = new HashMap<>();

        int argIndex = 0;
        for (int i = 0; i < opts.size(); i++) {
            LegacyOption opt = opts.get(i);
            String name = opt.getName();

            if (argIndex >= argsContent.size()) {
                if (opt.isRequired()) {
                    throw new IllegalArgumentException("Missing required argument: <" + name + ">");
                }
                break;
            }

            // If it's the last defined argument, and there are remaining tokens, capture all of them
            boolean isLast = (i == opts.size() - 1);
            if (isLast) {
                String joined = String.join(" ", argsContent.subList(argIndex, argsContent.size()));
                args.put(name, new ArgumentMapper(joined));
                break;
            }

            args.put(name, new ArgumentMapper(argsContent.get(argIndex)));
            argIndex++;
        }
        return Collections.unmodifiableMap(args);
    }

    private List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1)); // Quoted part
            } else {
                tokens.add(matcher.group(2)); // Normal part
            }
        }
        return tokens;
    }
}