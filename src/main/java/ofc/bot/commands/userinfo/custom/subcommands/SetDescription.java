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

@DiscordCommand(name = "description", description = "Define/reseta a descrição da embed apresentada no userinfo.")
public class SetDescription extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetDescription.class);

    @Option
    private static final OptionData DESCRIPTION = new OptionData(OptionType.STRING, "description", "A descrição da embed do userinfo. Ignore ou forneça \"remove\" para remover.")
            .setRequiredLength(1, 200);

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User sender = ctx.getUser();
        String input = ctx.getOption("description", "remove", OptionMapping::getAsString);
        boolean isRemotion = input.equalsIgnoreCase("remove");
        long userId = sender.getIdLong();

        try {
            String text = isRemotion
                    ? null
                    : input;

            setDescription(userId, text);
            return Status.USERINFO_DESCRIPTION_SUCCESSFULLY_UPDATED;
            
        } catch (DataAccessException e) {
            LOGGER.error("Could not update userinfo description of user '{}'", userId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private void setDescription(long userId, String text) {

        DSLContext ctx = DBManager.getContext();
        long timestamp = Bot.unixNow();

        ctx.transaction((cfg) -> {

            CustomUserinfo.ensureExists(userId);

            ctx.update(CUSTOM_USERINFO)
                    .set(CUSTOM_USERINFO.DESCRIPTION, text)
                    .set(CUSTOM_USERINFO.UPDATED_AT, timestamp)
                    .execute();
        });
    }
}