package ofc.bot.commands.userinfo.custom.subcommands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.commands.userinfo.custom.CustomUserinfo;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.databases.DBManager;
import ofc.bot.util.content.annotations.commands.Option;
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

@DiscordCommand(name = "footer", description = "Define/reseta o rodapé da embed apresentada no userinfo.")
public class SetFooter extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetFooter.class);

    @Option
    private static final OptionData FOOTER = new OptionData(OptionType.STRING, "footer", "O rodapé da embed do userinfo. Ignore ou forneça \"remove\" para remover.")
            .setRequiredLength(1, 50);

    @Override
    public CommandResult onCommand(CommandContext ctx) {
        
        User sender = ctx.getUser();
        String input = ctx.getOption("footer", "remove", OptionMapping::getAsString);
        boolean isRemotion = input.equalsIgnoreCase("--remove");
        long userId = sender.getIdLong();

        try {
            String text = isRemotion
                    ? null
                    : input;

            setFooter(userId, text);
            return Status.USERINFO_FOOTER_SUCCESSFULLY_UPDATED;
            
        } catch (DataAccessException e) {
            LOGGER.error("Could not update userinfo footer of user '{}'", userId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private void setFooter(long userId, String text) {

        DSLContext ctx = DBManager.getContext();
        long timestamp = Bot.unixNow();

        ctx.transaction((cfg) -> {

            CustomUserinfo.ensureExists(userId);

            ctx.update(CUSTOM_USERINFO)
                    .set(CUSTOM_USERINFO.FOOTER, text)
                    .set(CUSTOM_USERINFO.UPDATED_AT, timestamp)
                    .execute();
        });
    }
}