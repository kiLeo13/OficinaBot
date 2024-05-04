package ofc.bot.commands.birthdays.subcommands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.databases.DBManager;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ofc.bot.databases.entities.tables.Birthdays.BIRTHDAYS;

@DiscordCommand(name = "remove", description = "Remove um aniversário do registro.")
public class BirthdayRemove extends SlashSubcommand {
    private final Logger LOGGER = LoggerFactory.getLogger(BirthdayRemove.class);

    @Option(required = true)
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "O ID do usuário a ser removido.", true);

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User target = ctx.getSafeOption("user", OptionMapping::getAsUser);
        long targetId = target.getIdLong();

        if (!isPresent(targetId))
            return Status.USER_IS_NOT_IN_BIRTHDAY_LIST;

        try {
            remove(targetId);

            return Status.BIRTHDAY_DELETED_SUCCESSFULLY.args(targetId);
        } catch (DataAccessException e) {
            LOGGER.error("Could not remove user '{}' from birthday list", targetId, e);
            return Status.COULD_NOT_REMOVE_BIRTHDAY;
        }
    }

    private boolean isPresent(long userId) {

        DSLContext ctx = DBManager.getContext();

        return ctx.fetchExists(BIRTHDAYS, BIRTHDAYS.USER_ID.eq(userId));
    }

    private void remove(long userId) {

        DSLContext ctx = DBManager.getContext();

        ctx.deleteFrom(BIRTHDAYS)
                .where(BIRTHDAYS.USER_ID.eq(userId))
                .execute();
    }
}