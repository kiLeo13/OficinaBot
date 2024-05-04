package ofc.bot.commands.userinfo.custom.subcommands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.commands.userinfo.custom.CustomUserinfo;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.databases.DBManager;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ofc.bot.databases.entities.tables.CustomUserinfo.CUSTOM_USERINFO;

@DiscordCommand(name = "color", description = "Define/reseta a cor da barra lateral da embed apresentada no userinfo.")
public class SetColor extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetColor.class);

    @Option
    private static final OptionData COLOR = new OptionData(OptionType.STRING, "color", "A cor em HEX a reset definida. Ignore para remover.")
            .setRequiredLength(6, 6);

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        String input = ctx.getOption("color", null, OptionMapping::getAsString);
        User sender = ctx.getUser();
        long userId = sender.getIdLong();

        try {
            int color = input == null ? -1 : Bot.hexToRgb(input);

            setColor(userId, color);
            return Status.USERINFO_COLOR_SUCCESSFULLY_UPDATED;
            
        } catch (DataAccessException e) {

            LOGGER.error("Could not update userinfo color of user '{}'", userId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;

        } catch (NumberFormatException e) {
            return Status.INVALID_HEX_PROVIDED;
        }
    }

    private void setColor(long userId, int color) {

        DSLContext ctx = DBManager.getContext();
        long timestamp = Bot.unixNow();

        CustomUserinfo.ensureExists(userId);

        ctx.update(CUSTOM_USERINFO)
                .set(CUSTOM_USERINFO.COLOR, color)
                .set(CUSTOM_USERINFO.UPDATED_AT, timestamp)
                .execute();
    }
}