package ofc.bot.commands.marriages;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.databases.DBManager;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.CommandPermission;
import ofc.bot.util.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.MarriageUtil;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;

import static ofc.bot.databases.entities.tables.Marriages.MARRIAGES;

@DiscordCommand(name = "marriage_date", description = "Altere a data de um casamento. Utilize o horário de Brasília (GMT -3).")
@CommandPermission(Permission.MANAGE_SERVER)
public class UpdateCreation extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCreation.class);

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Option(required = true)
    private static final OptionData SPOUSE = new OptionData(OptionType.USER, "spouse", "O \"primeiro\" parceiro do casamento.");

    @Option
    private static final OptionData PARTNER = new OptionData(OptionType.USER, "partner", "O \"segundo\" parceiro do casamento (ignore esta opção se for você mesmo).");

    @Option(required = true)
    private static final OptionData DATE = new OptionData(OptionType.STRING, "date", "A data do casamento (Formato: DD/MM/AAAA).");

    @Option
    private static final OptionData TIME = new OptionData(OptionType.STRING, "time", "O tempo do casamento (Formato: HH:MM:SS).");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User sender = ctx.getUser();
        User spouse = ctx.getSafeOption("spouse", OptionMapping::getAsUser);
        User partner = ctx.getOption("partner", sender, OptionMapping::getAsUser);
        String dateInput = ctx.getSafeOption("date", OptionMapping::getAsString);
        String timeInput = ctx.getOption("time", "00:00:00", OptionMapping::getAsString);
        Instant newMarriageDate = parseDate(dateInput, timeInput);
        long senderId = sender.getIdLong();
        long spouseId = spouse.getIdLong();
        long partnerId = partner.getIdLong();
        boolean areUsersMarried = MarriageUtil.areMarried(spouseId, partnerId);

        if (spouseId == senderId && partnerId == senderId)
            return Status.CANNOT_UPDATE_SELF_MARRIAGE_DATE;

        if (newMarriageDate == null)
            return Status.INVALID_DATE_FORMAT;

        if (!areUsersMarried)
            return Status.PROVIDED_USERS_ARE_NOT_MARRIED;

        try {
            long dateUnixTimestamp = newMarriageDate.getEpochSecond();
            updateMarriageDate(spouseId, partnerId, dateUnixTimestamp);

            ctx.reply()
                    .setContentFormat("Data do casamento de %s com %s com atualizada com sucesso para <t:%d:d>.", spouse.getAsMention(), partner.getAsMention(), dateUnixTimestamp)
                    .setAllowedMentions(Collections.emptyList())
                    .send();

            return Status.PASSED;

        } catch (DataAccessException e) {
            LOGGER.error("Could not update marriage date of '{}' with '{}'", spouseId, partnerId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private void updateMarriageDate(long spouseId, long anotherSpouseId, long newDate) {

        DSLContext ctx = DBManager.getContext();

        ctx.update(MARRIAGES)
                .set(MARRIAGES.CREATED_AT, newDate)
                .set(MARRIAGES.UPDATED_AT, newDate)
                .where(MARRIAGES.REQUESTER_ID.eq(spouseId).and(MARRIAGES.TARGET_ID.eq(anotherSpouseId)))
                .or(MARRIAGES.REQUESTER_ID.eq(anotherSpouseId).and(MARRIAGES.TARGET_ID.eq(spouseId)))
                .execute();
    }

    private Instant parseDate(String date, String time) {

        try {
            LocalDateTime parsed = LocalDateTime.parse(date + " " + time, DATETIME_FORMATTER);

            return parsed.toInstant(ZoneOffset.ofHours(-3));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}