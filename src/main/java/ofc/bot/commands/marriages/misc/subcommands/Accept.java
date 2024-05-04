package ofc.bot.commands.marriages.misc.subcommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.commands.marriages.Marry;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.MarriageRequestRecord;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import ofc.bot.util.Bot;
import ofc.bot.util.EconomyUtil;
import ofc.bot.util.MarriageUtil;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ofc.bot.databases.entities.tables.MarriageRequests.MARRIAGE_REQUESTS;

@DiscordCommand(name = "accept", description = "Aceite uma proposta de casamento.")
public class Accept extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Accept.class);

    @Option(required = true)
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "A pessoa na qual você quer aceitar o pedido de casamento.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Member sender = ctx.getIssuer();
        Member target = ctx.getSafeOption("user", OptionMapping::getAsMember);
        MessageChannel channel = ctx.getChannel();
        long senderId = sender.getIdLong();
        long targetId = target.getIdLong();
        boolean isMarriageAffordable = MarriageUtil.hasEnoughBalance(senderId) && MarriageUtil.hasEnoughBalance(targetId);
        boolean privilegedMarriage = Marry.isPrivilegedMarriage(sender, target);
        MarriageRequestRecord proposal = retrieveProposal(senderId, targetId);

        if (proposal == null)
            return Status.NO_INCOME_PROPOSAL_FROM_USER.args(target.getUser().getEffectiveName());

        if (proposal.getRequesterId() == senderId)
            return Status.CANNOT_ACCEPT_SELF_MARRIAGE_PROPOSAL;

        if (!isMarriageAffordable && !privilegedMarriage)
            return Status.MARRIAGE_INSUFFICIENT_BALANCE.args(Bot.strfNumber(Marry.INITIAL_MARRIAGE_COST));

        try {
            proposal.approve();
            sendCelebrationMessage(channel, senderId, targetId);

            if (!privilegedMarriage)
                chargeMarriage(senderId, targetId);

            return Status.MARRIAGE_PROPOSAL_ACCEPTED_SUCCESSFULLY.setEphm(true);

        } catch (DataAccessException e) {
            LOGGER.error("Could not marry '{}' and '{}'", senderId, targetId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private void chargeMarriage(long senderId, long targetId) {

        DSLContext ctx = DBManager.getContext();
        int cost = Marry.INITIAL_MARRIAGE_COST * -1;

        ctx.transaction((cfg) -> {

            DSLContext transactCtx = DBManager.getContext(cfg);

            EconomyUtil.updateBalance(transactCtx, senderId, cost);
            EconomyUtil.updateBalance(transactCtx, targetId, cost);
        });
    }

    private void sendCelebrationMessage(MessageChannel channel, long spouse, long anotherSpouse) {
        channel.sendMessageFormat("Parabéns pelo casamento entre vocês <@%d> e <@%d>! Muitas felicidades 🥳🥳🥳", spouse, anotherSpouse).queue();
    }

    private MarriageRequestRecord retrieveProposal(long user, long anotherUser) {

        DSLContext ctx = DBManager.getContext();

        return ctx.selectFrom(MARRIAGE_REQUESTS)
                .where(MARRIAGE_REQUESTS.REQUESTER_ID.eq(user).and(MARRIAGE_REQUESTS.TARGET_ID.eq(anotherUser)))
                .or(MARRIAGE_REQUESTS.REQUESTER_ID.eq(anotherUser).and(MARRIAGE_REQUESTS.TARGET_ID.eq(user)))
                .fetchOne();
    }
}