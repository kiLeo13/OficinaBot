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
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ofc.bot.databases.entities.tables.MarriageRequests.MARRIAGE_REQUESTS;

@DiscordCommand(name = "cancel", description = "Cancele uma proposta de casamento enviada à alguém.")
public class CancelProposal extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelProposal.class);

    @Option(required = true)
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "A pessoa na qual você quer remover a proposta de casamento.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User sender = ctx.getUser();
        User target = ctx.getSafeOption("user", OptionMapping::getAsUser);
        long senderId = sender.getIdLong();
        long targetId = target.getIdLong();
        boolean isPending = hasSentProposal(senderId, targetId);

        if (!isPending)
            return Status.NO_ACTIVE_PROPOSAL_SENT_TO_USER.args(target.getEffectiveName());

        try {
            cancelProposal(senderId, targetId);

            return Status.PROPOSAL_REMOVED_SUCCESSFULLY.args(target.getEffectiveName());

        } catch (DataAccessException e) {
            LOGGER.error("Could not remove proposal from '{}' to '{}'", senderId, targetId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private boolean hasSentProposal(long sender, long receiver) {

        DSLContext ctx = DBManager.getContext();

        return ctx.fetchValue(DSL.exists(
                ctx.selectOne().from(MARRIAGE_REQUESTS)
                        .where(MARRIAGE_REQUESTS.REQUESTER_ID.eq(sender).and(MARRIAGE_REQUESTS.TARGET_ID.eq(receiver)))
                )
        );
    }

    private void cancelProposal(long sender, long target) {

        DSLContext ctx = DBManager.getContext();

        ctx.deleteFrom(MARRIAGE_REQUESTS)
                .where(MARRIAGE_REQUESTS.REQUESTER_ID.eq(sender).and(MARRIAGE_REQUESTS.TARGET_ID.eq(target)))
                .execute();
    }
}