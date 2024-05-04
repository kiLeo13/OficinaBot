package ofc.bot.commands.economy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.CommandPermission;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.EconomyUtil;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "update_money", description = "ATUALIZA o saldo do usuário fornecido com a quantia fornecida, valores negativos removerão dinheiro.")
@CommandPermission(Permission.MANAGE_SERVER)
public class UpdateMoney extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMoney.class);

    @Option(required = true)
    private static final OptionData AMOUNT = new OptionData(OptionType.INTEGER, "amount", "A quantia a ser atualizada.")
            .setRequiredRange((long) OptionData.MIN_NEGATIVE_NUMBER, (long) OptionData.MAX_POSITIVE_NUMBER);

    @Option
    private static final OptionData TARGET = new OptionData(OptionType.USER, "target", "O alvo a atualizar o saldo.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User issuer = ctx.getUser();
        User target = ctx.getOption("target", issuer, OptionMapping::getAsUser);
        long amount = ctx.getSafeOption("amount", OptionMapping::getAsLong);
        long targetId = target.getIdLong();

        if (amount == 0)
            return Status.NOTHING_CHANGED_WITH_REASON.args("`$0` foi fornecido");

        if (newBalanceOverflows(targetId, amount))
            return Status.USER_CANNOT_RECEIVE_GIVEN_AMOUNT;

        try {
            EconomyUtil.updateBalance(targetId, amount);

            return Status.ECONOMY_SUCCESSFULLY_UPDATED_BALANCE.args(target.getAsMention(), Bot.strfNumber(amount));
        } catch (DataAccessException e) {
            LOGGER.error("Could not access balance of '{}'", targetId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    private boolean newBalanceOverflows(long userId, long amountModify) {

        long balance = EconomyUtil.fetchBalance(userId);

        return EconomyUtil.willOverflow(balance, amountModify);
    }
}