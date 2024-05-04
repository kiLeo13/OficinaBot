package ofc.bot.commands.birthdays.subcommands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.commands.birthdays.Birthday;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.databases.entities.records.BirthdayRecord;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@DiscordCommand(name = "add", description = "Adiciona um aniversário ao registro.")
public class BirthdayAdd extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(BirthdayAdd.class);

    @Option(required = true)
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "O usuário a ter o aniversário salvo.");

    @Option(required = true)
    private static final OptionData NAME = new OptionData(OptionType.STRING, "name", "O nome que o usuário gosta de ser chamado.")
            .setRequiredLength(2, 128);

    @Option(required = true)
    private static final OptionData BIRTHDAY = new OptionData(OptionType.STRING, "birthday", "A data de nascimento do usuário fornecido (Formato: DD/MM/AAAA).");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        String name = ctx.getSafeOption("name", OptionMapping::getAsString);
        String dateInput = ctx.getSafeOption("birthday", OptionMapping::getAsString);
        User user = ctx.getSafeOption("user", OptionMapping::getAsUser);
        long userId = user.getIdLong();

        try {
            LocalDate date = LocalDate.parse(dateInput, Birthday.END_USER_DATE_FORMATTER);
            BirthdayRecord birthday = new BirthdayRecord(userId, name, date);

            birthday.save();

            return Status.BIRTHDAY_ADDED_SUCCESSFULLY.args(user.getAsMention(), name);
        } catch (DateTimeParseException e) {

            return Status.INVALID_DATE_FORMAT;
        } catch (DataAccessException e) {

            LOGGER.error("Could not store birthday of '{}'", userId, e);
            return Status.COULD_NOT_ADD_BIRTHDAY;
        }
    }
}