package ofc.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.time.OficinaDuration;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DiscordCommand(name = "ban", description = "Bane um membro do servidor.", permission = Permission.BAN_MEMBERS)
public class BanCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(BanCommand.class);
    private static final Duration MAX_DELETION_TIMEFRAME = Duration.ofDays(7);

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Member target = ctx.getOption("member", OptionMapping::getAsMember);
        String reason = ctx.getSafeOption("reason", OptionMapping::getAsString);
        String fmtDuration = ctx.getOption("duration", "", OptionMapping::getAsString);
        String fmtDelTimeframe = ctx.getOption("history-deletion", "", OptionMapping::getAsString);
        Guild guild = ctx.getGuild();
        Member self = guild.getSelfMember();

        if (target == null)
            return Status.MEMBER_NOT_FOUND;

        if (!self.canInteract(target))
            return Status.BOT_CANNOT_BAN_PROVIDED_MEMBER;

        OficinaDuration delTimeframe;
        OficinaDuration banDuration; // Soon
        try {
            delTimeframe = OficinaDuration.ofPattern(fmtDelTimeframe);
            banDuration = OficinaDuration.ofPattern(fmtDuration);
        } catch (DataAccessException e) {
            return Status.INVALID_DURATION_PROVIDED;
        }

        if (delTimeframe.isLess(MAX_DELETION_TIMEFRAME))
            return Status.INVALID_DELETION_TIMEFRAME.args(
                    Bot.parseDuration(MAX_DELETION_TIMEFRAME), Bot.parsePeriod(delTimeframe.getSeconds()));

        target.ban((int) delTimeframe.getMillis(), TimeUnit.MILLISECONDS).reason(reason).queue(v -> {
            ctx.reply(Status.MEMBER_SUCCESSFULLY_BANNED.args(target.getAsMention()));
        }, (err) -> {
            LOGGER.error("Could not ban member {}", target.getId(), err);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });
        return Status.OK;
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "member", "O membro a ser banido", true),
                new OptionData(OptionType.STRING, "reason", "O motivo do banimento.", true)
                        .setRequiredLength(5, 400),

                //new OptionData(OptionType.STRING, "duration", "O tempo que o membro deve ficar banido.")
                        //.setMinLength(2),

                new OptionData(OptionType.STRING, "history-deletion",
                        "Apague o histórico de mensagens dentro do período selecionado.")
                        .setMinLength(2)
        );
    }
}