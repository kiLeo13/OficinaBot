package ofc.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.domain.entity.TempBan;
import ofc.bot.domain.entity.enums.PunishmentType;
import ofc.bot.domain.sqlite.repository.TempBanRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;
import ofc.bot.util.time.OficinaDuration;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@DiscordCommand(name = "ban", permission = Permission.BAN_MEMBERS)
public class BanCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(BanCommand.class);
    private static final Duration MAX_DELETION_TIMEFRAME = Duration.ofDays(7);
    private final TempBanRepository tmpBanRepo;

    public BanCommand(TempBanRepository tmpBanRepo) {
        this.tmpBanRepo = tmpBanRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        User target = ctx.getSafeOption("user", OptionMapping::getAsUser);
        String reason = ctx.getSafeOption("reason", OptionMapping::getAsString);
        String fmtDuration = ctx.getOption("duration", "", OptionMapping::getAsString);
        String fmtDelTimeframe = ctx.getOption("history-deletion", "", OptionMapping::getAsString);
        Guild guild = ctx.getGuild();
        Member self = guild.getSelfMember();
        long targetId = target.getIdLong();
        long guildId = guild.getIdLong();

        Member memberTarget = guild.getMember(target);
        if (memberTarget != null && !self.canInteract(memberTarget))
            return Status.BOT_CANNOT_BAN_PROVIDED_MEMBER;

        OficinaDuration delTimeframe;
        OficinaDuration banDuration;
        try {
            delTimeframe = OficinaDuration.ofPattern(fmtDelTimeframe);
            banDuration = OficinaDuration.ofPattern(fmtDuration);
        } catch (DataAccessException e) {
            return Status.INVALID_DURATION_PROVIDED;
        }

        if (delTimeframe.isLess(MAX_DELETION_TIMEFRAME))
            return Status.INVALID_DELETION_TIMEFRAME.args(
                    Bot.parseDuration(MAX_DELETION_TIMEFRAME), Bot.parsePeriod(delTimeframe.getSeconds()));

        ctx.ack();
        guild.ban(target, (int) delTimeframe.getMillis(), TimeUnit.MILLISECONDS).reason(reason).queue(v -> {
            long secs = banDuration.getSeconds();
            long expiresAt = Bot.unixNow() + secs;
            MessageEmbed embed = EmbedFactory.embedPunishment(target, PunishmentType.BAN, reason, secs);

            if (secs != 0) {
                TempBan ban = new TempBan(targetId, guildId, expiresAt);
                tmpBanRepo.save(ban);
            }
            ctx.replyEmbeds(embed);
        }, (err) -> {
            LOGGER.error("Could not ban member {}", target.getId(), err);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });
        return Status.OK;
    }

    @Override
    protected void init() {
        setDesc("Bane um membro do servidor.");

        addOpt(OptionType.USER, "user", "O usuário a ser banido", true);
        addOpt(OptionType.STRING, "reason", "O motivo do banimento.", true, 5, 400);
        addOpt(OptionType.STRING, "duration", "O tempo que o membro deve ficar banido.", (it) -> it.setMinLength(2));
        addOpt(OptionType.STRING, "history-deletion", "Apague o histórico de mensagens dentro do período selecionado.", (it) -> it.setMinLength(2));
    }
}