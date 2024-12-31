package ofc.bot.commands.relationships.marriages;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.commands.relationships.MarryCommand;
import ofc.bot.domain.entity.*;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.DB;
import ofc.bot.domain.sqlite.repository.MarriageRepository;
import ofc.bot.domain.sqlite.repository.MarriageRequestRepository;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.events.entities.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@DiscordCommand(name = "marriage accept", description = "Aceite uma proposta de casamento.")
public class MarriageAcceptCommanad extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarriageAcceptCommanad.class);
    private final MarriageRequestRepository mreqRepo;
    private final MarriageRepository marrRepo;
    private final UserEconomyRepository ecoRepo;

    public MarriageAcceptCommanad(MarriageRequestRepository mreqRepo, MarriageRepository marrRepo, UserEconomyRepository ecoRepo) {
        this.mreqRepo = mreqRepo;
        this.marrRepo = marrRepo;
        this.ecoRepo = ecoRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Member issuer = ctx.getIssuer();
        Member target = ctx.getSafeOption("user", OptionMapping::getAsMember);
        MessageChannel channel = ctx.getChannel();
        long issuerId = issuer.getIdLong();
        long targetId = target.getIdLong();
        boolean privilegedMarriage = isPrivilegedMarriage(issuer, target);
        UserEconomy senderBal = ecoRepo.findByUserId(issuerId);
        UserEconomy targetBal = ecoRepo.findByUserId(targetId);
        MarriageRequest proposal = mreqRepo.findByStrictIds(targetId, issuerId);
        int cost = MarryCommand.INITIAL_MARRIAGE_COST;

        if (issuerId == targetId)
            return Status.CANNOT_ACCEPT_SELF_MARRIAGE_PROPOSAL;

        if (proposal == null)
            return Status.NO_INCOME_PROPOSAL_FROM_USER.args(target.getUser().getEffectiveName());

        if (proposal.getRequesterId() == issuerId)
            return Status.CANNOT_ACCEPT_SELF_MARRIAGE_PROPOSAL;

        if (!isAffordable(senderBal, targetBal) && !privilegedMarriage)
            return Status.MARRIAGE_INSUFFICIENT_BALANCE.args(Bot.fmtNum(cost));

        try {
            accept(proposal);
            sendCelebrationMessage(channel, issuerId, targetId);

            if (!privilegedMarriage)
                chargeMarriage(senderBal, targetBal, cost);

            return Status.MARRIAGE_PROPOSAL_ACCEPTED_SUCCESSFULLY.setEphm(true);
        } catch (DataAccessException e) {
            LOGGER.error("Could not marry '{}' and '{}'", issuerId, targetId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "A pessoa na qual você quer aceitar o pedido de casamento.", true)
        );
    }

    private boolean isAffordable(UserEconomy user1, UserEconomy user2) {
        return checkBalance(user1) && checkBalance(user2);
    }

    private boolean checkBalance(UserEconomy user) {
        return user != null && user.getBalance() >= MarryCommand.INITIAL_MARRIAGE_COST;
    }

    private void chargeMarriage(UserEconomy ecoSender, UserEconomy ecoTarget, int price) {
        DSLContext ctx = DB.getContext();

        ecoSender.modifyBalance(-price)
                .tickUpdate();

        ecoTarget.modifyBalance(-price)
                .tickUpdate();

        ctx.transaction((cfg) -> {
            DSLContext trs = cfg.dsl();

            ecoRepo.updateCtx(trs, ecoSender);
            ecoRepo.updateCtx(trs, ecoTarget);
        });

        dispatchMarriageCreateEvent(ecoSender.getUserId(), price);
        dispatchMarriageCreateEvent(ecoTarget.getUserId(), price);
    }

    private void accept(MarriageRequest req) {
        mreqRepo.delete(req);

        Marriage marr = Marriage.fromUsers(req.getRequesterId(), req.getTargetId());
        marrRepo.save(marr);
    }

    private void sendCelebrationMessage(MessageChannel channel, long spouse, long anotherSpouse) {
        channel.sendMessageFormat("Parabéns pelo casamento entre vocês <@%d> e <@%d>! Muitas felicidades 🥳🥳🥳", spouse, anotherSpouse).queue();
    }

    private boolean isPrivilegedMarriage(Member spouse, Member anotherSpouse) {
        return isPermissionPrivileged(spouse) || isPermissionPrivileged(anotherSpouse);
    }

    private boolean isPermissionPrivileged(Member member) {
        return member != null && member.hasPermission(Permission.MANAGE_SERVER);
    }

    private void dispatchMarriageCreateEvent(long userId, int amount) {
        BankTransaction tr = new BankTransaction(userId, -amount, CurrencyType.OFICINA, TransactionType.MARRIAGE_CREATED);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}