package ofc.bot.commands.marriages;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.databases.users.MembersDAO;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.MarriageUtil;

@DiscordCommand(name = "marry", description = "Se case com outra pessoa.")
public class Marry extends SlashCommand {
    public static final int INITIAL_MARRIAGE_COST = 50000;
    public static final int DAILY_COST = 150;

    public static final int MAX_GENERAL_MARRIAGES = 10;
    public static final int MAX_PRIVILEGED_MARRIAGES = 20;

    @Option(required = true)
    private static final OptionData MEMBER = new OptionData(OptionType.USER, "member", "O usuário com quem você quer se casar.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Member target = ctx.getOption("member", OptionMapping::getAsMember);
        Member sender = ctx.getIssuer();

        if (target == null)
            return Status.MEMBER_NOT_FOUND;

        MessageChannel channel = ctx.getChannel();
        long requesterId = sender.getIdLong();
        long targetId = target.getIdLong();
        boolean requesterHitLimit = MarriageUtil.hasHitLimit(sender);
        boolean targetHitLimit = MarriageUtil.hasHitLimit(target);
        boolean requesterHasBalance = MarriageUtil.hasEnoughBalance(requesterId);
        boolean targetHasBalance = MarriageUtil.hasEnoughBalance(targetId);
        boolean privilegedMarriage = isPrivilegedMarriage(sender, target);

        if (requesterId == targetId || target.getUser().isBot())
            return Status.CANNOT_MARRY_TO_USER;

        if (MarriageUtil.isPending(requesterId, targetId))
            return Status.PENDING_PROPOSAL;

        if (MarriageUtil.areMarried(requesterId, targetId))
            return Status.ALREADY_MARRIED_TO_USER.args(target.getAsMention());

        if (requesterHitLimit)
            return Status.ISSUER_HIT_MARRIAGE_LIMIT;

        if (targetHitLimit)
            return Status.TARGET_HIT_MARRIAGE_LIMIT;

        if ((!requesterHasBalance || !targetHasBalance) && !privilegedMarriage)
            return Status.MARRIAGE_INSUFFICIENT_BALANCE.args(Bot.strfNumber(INITIAL_MARRIAGE_COST));

        channel.sendMessageFormat("> ✨💍 %s você recebeu uma proposta de casamento maravilhosa de %s, safadeza hein?! 😏\nPara aceitar, use `/marriage accept`.",
                target.getAsMention(),
                sender.getAsMention()
        ).queue();

        MarriageUtil.createProposal(requesterId, targetId);

        MembersDAO.upsertUser(sender.getUser());
        MembersDAO.upsertUser(target.getUser());

        return Status.MARRIAGE_PROPOSAL_SENT_SUCCESSFULLY.setEphm(true);
    }

    public static boolean isPrivilegedMarriage(Member spouse, Member anotherSpouse) {
        return isPermissionPrivileged(spouse) || isPermissionPrivileged(anotherSpouse);
    }

    private static boolean isPermissionPrivileged(Member member) {
        return member != null && member.hasPermission(Permission.MANAGE_SERVER);
    }
}