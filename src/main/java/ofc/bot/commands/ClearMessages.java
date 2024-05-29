package ofc.bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.content.annotations.commands.CommandPermission;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@DiscordCommand(name = "clear", description = "Limpa de 2 a 100 mensagens do chat de uma só vez.", autoDefer = true, deferEphemeral = true, cooldown = 1)
@CommandPermission(Permission.MESSAGE_MANAGE)
public class ClearMessages extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClearMessages.class);

    @Option(required = true)
    private static final OptionData AMOUNT = new OptionData(OptionType.INTEGER, "amount", "A quantidade de mensagens a ser limpada.")
            .setRequiredRange(2, 100);

    @Option
    private static final OptionData CHANNEL = new OptionData(OptionType.CHANNEL, "channel", "O canal ser limpado as mensagens (Padrão: atual).")
            .setChannelTypes(getTextChannelTypes());

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        GuildMessageChannel current = ctx.getChannel().asGuildMessageChannel();
        GuildMessageChannel channel = (GuildMessageChannel) ctx.getOption("channel", current, OptionMapping::getAsChannel);
        int amount = ctx.getSafeOption("amount", OptionMapping::getAsInt);

        channel.getHistory().retrievePast(amount).queue(msgs -> {

            int msgCount = msgs.size();

            channel.deleteMessages(msgs).queue(s -> {

                ctx.reply(Status.MESSAGES_SUCCESSFULLY_DELETED.args(msgCount, channel.getName()));
            }, err -> {

                LOGGER.error("Could not clear {} messages from #{}", msgCount, channel.getId(), err);
                ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
            });
        });

        return Status.PASSED;
    }

    private static ChannelType[] getTextChannelTypes() {
        return Arrays.stream(ChannelType.values())
                .filter(ChannelType::isMessage)
                .filter(ChannelType::isGuild)
                .toArray(ChannelType[]::new);
    }
}