package ofc.bot.commands.reminders;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.domain.entity.Reminder;
import ofc.bot.domain.sqlite.repository.ReminderRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

@DiscordCommand(name = "remind at")
public class CreateAtReminderCommand extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateAtReminderCommand.class);
    private static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ReminderRepository remRepo;

    public CreateAtReminderCommand(ReminderRepository remRepo) {
        this.remRepo = remRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        String fmtDate = ctx.getSafeOption("date", OptionMapping::getAsString);
        String message = ctx.getSafeOption("message", OptionMapping::getAsString);
        boolean isDm = ctx.getOption("privately", true, OptionMapping::getAsBoolean);
        int timezone = ctx.getOption("utc", -3, OptionMapping::getAsInt);
        long userId = ctx.getUserId();
        User user = ctx.getUser();
        MessageChannel chan = ctx.getChannel();
        ChannelType chanType = isDm ? ChannelType.PRIVATE : chan.getType();
        long chanId = isDm ? userId : chan.getIdLong();

        int count = remRepo.countActiveByUserId(userId);
        if (count >= Reminder.MAX_PER_USER)
            return Status.HIT_MAX_REMINDERS.args(count);

        ZonedDateTime zonedFuture;
        try {
            LocalDateTime localFuture = LocalDateTime.parse(fmtDate, FORMATTER);
            zonedFuture = ZonedDateTime.of(localFuture, ZoneOffset.ofHours(timezone));
        } catch (DateTimeParseException e) {
            return Status.INVALID_DATE_FORMAT;
        }

        if (!Reminder.isChannelAllowed(chanType))
            return Status.INVALID_CHANNEL_TYPE.args(chanType);

        if (!isFuture(zonedFuture))
            return Status.DATETIME_MUST_BE_IN_THE_FUTURE.args(fmtDate);

        try {
            Reminder reminder = Reminder.ofAt(userId, chanId, chanType, message, zonedFuture);
            remRepo.save(reminder);
        } catch (DataAccessException e) {
            LOGGER.error("Failed to save reminders to the database", e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }

        MessageEmbed embed = EmbedFactory.embedAtReminder(user, zonedFuture);
        return ctx.replyEmbeds(embed);
    }

    @Override
    protected void init() {
        setDesc("Cria um lembrete para um momento específico.");
        setCooldown(false, 10, TimeUnit.SECONDS);

        String format = TIMESTAMP_FORMAT.toUpperCase().replace('Y', 'A');
        int size = format.length();

        addOpt(OptionType.STRING, "date", "Data do lembrete (Formato: " + format + ").", true, size, size);
        addOpt(OptionType.STRING, "message", "A mensagem do lembrete.", true, 2, 1000);
        addOpt(OptionType.BOOLEAN, "privately", "Se devemos enviar o lembrete no privado (Padrão: True).");
        addOpt(OptionType.INTEGER, "utc", "O seu fuso horário (Padrão: Horário de Brasília UTC-3).", -18, 18);
    }

    private boolean isFuture(ZonedDateTime time) {
        return time.isAfter(ZonedDateTime.now(time.getZone()));
    }
}