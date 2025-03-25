package ofc.bot.commands.reminders;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
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
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@DiscordCommand(name = "remind every")
public class CreateFixedReminderCommand extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateFixedReminderCommand.class);
    private final ReminderRepository remRepo;

    public CreateFixedReminderCommand(ReminderRepository remRepo) {
        this.remRepo = remRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        String message = ctx.getSafeOption("message", OptionMapping::getAsString);
        Month month = ctx.getEnumOption("month", null, Month.class);
        int day = ctx.getOption("day", -1, OptionMapping::getAsInt);
        int hour = ctx.getOption("hour", -1, OptionMapping::getAsInt);
        int minute = ctx.getOption("minute", -1, OptionMapping::getAsInt);
        boolean isDm = ctx.getOption("privately", true, OptionMapping::getAsBoolean);
        long userId = ctx.getUserId();
        User user = ctx.getUser();
        MessageChannel chan = ctx.getChannel();
        ChannelType chanType = isDm ? ChannelType.PRIVATE : chan.getType();
        String expression = buildExpression(month, day, hour, minute);
        long chanId = isDm ? userId : chan.getIdLong();

        int count = remRepo.countByUserId(userId);
        if (count >= Reminder.MAX_PER_USER)
            return Status.HIT_MAX_REMINDERS.args(count);

        if (remRepo.existsByExpressionAndUserId(userId, expression))
            return Status.SAME_EXPRESSION_REMINDER_FOUND;

        if (!CronExpression.isValidExpression(expression)) {
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }

        try {
            // Called to validate the expression
            new CronExpression(expression);

            Reminder reminder = Reminder.ofCron(userId, chanId, chanType, message, expression);
            remRepo.save(reminder);

            MessageEmbed embed = EmbedFactory.embedCronReminder(user, expression);
            return ctx.replyEmbeds(embed);
        } catch (ParseException e) {
            LOGGER.warn("We built an invalid expression: {}", expression, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        } catch (DateTimeParseException e) {
            LOGGER.error("Failed to save reminder to the database", e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @Override
    protected void init() {
        setDesc("Cria um lembrete para momentos fixos.");
        setCooldown(false, 10, TimeUnit.SECONDS);

        addOpt(OptionType.STRING, "message", "A mensagem do lembrete.", true, 2, 1000);
        addOpt(OptionType.BOOLEAN, "privately", "Se devemos enviar o lembrete no privado (Padrão: True).");
        addOpt(OptionType.STRING, "month", "Qual mês?", (it) -> it.addChoices(getMonths()));
        addOpt(OptionType.INTEGER, "day", "Qual dia do mês?", 1, 31);
        addOpt(OptionType.INTEGER, "hour", "Em qual horário?", 0, 23);
        addOpt(OptionType.INTEGER, "minute", "Em qual minuto?", 1, 59);
    }

    private String buildExpression(Month month, int day, int hour, int minute) {
        String expMon = month == null ? "*" : month.name().substring(0, 3);
        String expMin = minute == -1  ? "*" : String.valueOf(minute);
        String expDay = day == -1     ? "*" : String.valueOf(day);
        String expHour = hour == -1   ? "*" : String.valueOf(hour);

        return String.format("0 %s %s %s %s ? *", expMin, expHour, expDay, expMon);
    }

    private List<Command.Choice> getMonths() {
        return Stream.of(Month.values())
                .map(m -> new Command.Choice(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH), m.name()))
                .toList();
    }
}