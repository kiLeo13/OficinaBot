package ofc.bot.commands.relationships;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.domain.entity.Marriage;
import ofc.bot.domain.sqlite.repository.MarriageRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@DiscordCommand(name = "marriage-date", permission = Permission.MANAGE_SERVER)
public class UpdateMarriageCreationCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMarriageCreationCommand.class);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final MarriageRepository marrRepo;

    public UpdateMarriageCreationCommand(MarriageRepository marrRepo) {
        this.marrRepo = marrRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        User sender = ctx.getUser();
        User spouse = ctx.getSafeOption("spouse", OptionMapping::getAsUser);
        User partner = ctx.getOption("partner", sender, OptionMapping::getAsUser);
        String dateInput = ctx.getSafeOption("date", OptionMapping::getAsString);
        String timeInput = ctx.getOption("time", "00:00:00", OptionMapping::getAsString);
        Instant newMarriageDate = parseDate(dateInput, timeInput);
        long senderId = sender.getIdLong();
        long spouseId = spouse.getIdLong();
        long partnerId = partner.getIdLong();

        if (spouseId == senderId && partnerId == senderId)
            return Status.CANNOT_UPDATE_SELF_MARRIAGE_DATE;

        if (newMarriageDate == null)
            return Status.INVALID_DATE_FORMAT;

        Marriage relationship = marrRepo.findByUserIds(spouseId, partnerId);
        if (relationship == null)
            return Status.PROVIDED_USERS_ARE_NOT_MARRIED;

        try {
            long epochMarrDate = newMarriageDate.getEpochSecond();
            relationship
                    .setMarriedAt(epochMarrDate)
                    .tickUpdate();

            marrRepo.upsert(relationship);

            return ctx.create()
                    .setContentFormat("Data do casamento de %s com %s com atualizada com sucesso para <t:%d:d>.",
                            spouse.getAsMention(), partner.getAsMention(), epochMarrDate
                    )
                    .noMentions()
                    .send();
        } catch (DataAccessException e) {
            LOGGER.error("Could not update marriage date of '{}' with '{}'", spouseId, partnerId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @Override
    protected void init() {
        setDesc("Altere a data de um casamento. Utilize o horário de Brasília (GMT -3).");

        addOpt(OptionType.USER, "spouse", "O \"primeiro\" parceiro do casamento.", true);
        addOpt(OptionType.STRING, "date", "A data do casamento (Formato: DD/MM/AAAA).", true);
        addOpt(OptionType.USER, "partner", "O \"segundo\" parceiro do casamento (ignore esta opção se for você mesmo).");
        addOpt(OptionType.STRING, "time", "O tempo do casamento (Formato: HH:MM:SS).");
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