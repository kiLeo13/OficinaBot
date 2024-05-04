package ofc.bot.commands.marriages.misc.subcommands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.databases.DBManager;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import ofc.bot.util.MarriageUtil;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ofc.bot.databases.entities.tables.MarriageRequests.MARRIAGE_REQUESTS;

@DiscordCommand(name = "reject", description = "Recuse uma proposta de casamento.")
public class Reject extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reject.class);

    @Option(required = true)
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "A pessoa na qual você quer recusar o pedido de casamento.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User sender = ctx.getUser();
        User target = ctx.getSafeOption("user", OptionMapping::getAsUser);
        long senderId = sender.getIdLong();
        long targetId = target.getIdLong();
        boolean isPending = MarriageUtil.isPending(senderId, targetId);

        if (!isPending)
            return Status.NO_INCOME_PROPOSAL_FROM_USER.args(target.getAsMention());

        try {
            removePending(senderId, targetId);

            return Status.MARRIAGE_PROPOSAL_REJECTED_SUCCESSFULLY;

        } catch (DataAccessException e) {
            LOGGER.error("Could not reject proposal of '{}' and '{}'", senderId, targetId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private void removePending(long spouse, long anotherSpouse) {

        DSLContext ctx = DBManager.getContext();

        ctx.deleteFrom(MARRIAGE_REQUESTS)
                .where(MARRIAGE_REQUESTS.REQUESTER_ID.eq(spouse).and(MARRIAGE_REQUESTS.TARGET_ID.eq(anotherSpouse)))
                .or(MARRIAGE_REQUESTS.REQUESTER_ID.eq(anotherSpouse).and(MARRIAGE_REQUESTS.TARGET_ID.eq(spouse)))
                .execute();
    }
}