package ofc.bot.commands.userinfo.custom.subcommands;

import net.dv8tion.jda.api.entities.User;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.tables.CustomUserinfo;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "reset", description = "Redefine todos os dados customizados do userinfo.")
public class Reset extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reset.class);

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User sender = ctx.getUser();
        long userId = sender.getIdLong();

        try {
            reset(userId);

            return Status.USERINFO_RESET_SUCCESSFULLY;
        } catch (DataAccessException e) {
            LOGGER.error("Could not reset userinfo customization of user '{}'", userId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private void reset(long userId) {

        DSLContext ctx = DBManager.getContext();

        ctx.deleteFrom(CustomUserinfo.CUSTOM_USERINFO)
                .where(CustomUserinfo.CUSTOM_USERINFO.USER_ID.eq(userId))
                .execute();
    }
}