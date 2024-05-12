package ofc.bot.handlers.commands.contexts;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.commands.responses.InteractionResponseData;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class SlashCommandContext implements CommandContext {
    private static final Consumer<Throwable> DEFAULT_ERROR_HANDLER = (e) -> new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE);
    private final SlashCommandInteraction interaction;
    private final MessageChannelUnion channel;
    private final Guild guild;
    private final Member issuer;

    public SlashCommandContext(SlashCommandInteraction interaction) {

        Checks.notNull(interaction, "Context Interaction");

        MessageChannelUnion channel = interaction.getChannel();
        Guild guild = interaction.getGuild();
        Member issuer = interaction.getMember();

        Checks.notNull(channel, "SlashCommand channel");
        Checks.notNull(guild, "Guild");
        Checks.notNull(issuer, "SlashCommand issuer");

        this.interaction = interaction;
        this.channel = channel;
        this.guild = guild;
        this.issuer = issuer;
    }

    @Override
    public boolean isAcknowledged() {
        return this.interaction.isAcknowledged();
    }

    @NotNull
    @Override
    public SlashCommandInteraction getInteraction() {
        return this.interaction;
    }

    @Override
    public void ack(boolean ephemeral) {

        if (!isAcknowledged())
            this.interaction.deferReply(ephemeral).queue();
    }

    @Override
    public long getId() {
        return this.interaction.getIdLong();
    }

    @NotNull
    @Override
    public MessageChannelUnion getChannel() {
        return this.channel;
    }

    @NotNull
    @Override
    public Member getIssuer() {
        return this.issuer;
    }

    @NotNull
    @Override
    public User getUser() {
        return this.issuer.getUser();
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return this.guild;
    }

    @Override
    public <T> T getOption(String name, T fallback, Function<? super OptionMapping, ? extends T> resolver) {
        T option = getInteraction().getOption(name, resolver);

        return option == null
                ? fallback
                : option;
    }

    @Override
    public void reply(CommandResult result) {
        if (isAcknowledged())
            interaction.getHook()
                    .editOriginal(result.getContent())
                    .queue(null, DEFAULT_ERROR_HANDLER);
        else
            interaction.reply(result.getContent())
                    .setEphemeral(result.isEphemeral())
                    .queue();
    }

    @Override
    public void reply(@NotNull String content, boolean ephemeral) {
        if (isAcknowledged())
            interaction.getHook()
                    .editOriginal(content)
                    .queue(null, DEFAULT_ERROR_HANDLER);
        else
            interaction.reply(content)
                    .setEphemeral(ephemeral)
                    .queue();
    }

    @Override
    public void replyEmbeds(@NotNull MessageEmbed... embeds) {
        if (isAcknowledged())
            interaction.getHook()
                    .editOriginalEmbeds(embeds)
                    .queue(null, DEFAULT_ERROR_HANDLER);
        else
            interaction.replyEmbeds(List.of(embeds)).queue();
    }

    @Override
    public void replyFiles(@NotNull FileUpload... files) {
        if (isAcknowledged())
            interaction.getHook()
                    .editOriginalAttachments(files)
                    .queue(null, DEFAULT_ERROR_HANDLER);
        else
            interaction.replyFiles(files).queue();
    }

    @Override
    public void replyModal(@NotNull Modal modal) {

        if (isAcknowledged())
            throw new IllegalStateException("Cannot reply a Modal to an acknowledged interaction.");

        interaction.replyModal(modal).queue();
    }

    @Override
    public String toString() {
        List<OptionMapping> options = interaction.getOptions();
        String prettyOptions = Bot.format(options, (opt) -> " :" + opt.getName());

        return "/" + interaction.getName() + prettyOptions;
    }

    @Override
    public void reply(InteractionResponseData data) {
        if (isAcknowledged())
            interaction.getHook()
                    .editOriginal(data.toEditData())
                    .queue(null, DEFAULT_ERROR_HANDLER);
        else
            interaction.reply(data.toCreateData())
                    .setEphemeral(data.isEphemeral())
                    .queue();
    }
}