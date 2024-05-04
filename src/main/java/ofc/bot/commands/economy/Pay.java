package ofc.bot.commands.economy;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.users.MembersDAO;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.EconomyUtil;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "pay", description = "Envie dinheiro para outro usuário.", cooldown = 30)
public class Pay extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pay.class);
    private static final double TAX_PER_EXECUTION = 1 - 0.05; // 5%

    @Option(required = true)
    private static final OptionData TARGET = new OptionData(OptionType.USER, "user", "O usuário para enviar o dinheiro.");

    @Option(required = true) // We use the String data type here to make the "all" shortcut possible
    private static final OptionData AMOUNT = new OptionData(OptionType.STRING, "amount", "A quantia a ser enviada (forneça \"all\" sem aspas para transferir tudo).");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User sender = ctx.getUser();
        User target = ctx.getSafeOption("user", OptionMapping::getAsUser);
        String amountInput = ctx.getSafeOption("amount", OptionMapping::getAsString);
        long senderId = sender.getIdLong();
        long senderBalance = EconomyUtil.fetchBalance(senderId);
        long amount = parseAmountToPay(amountInput, senderBalance);
        long targetId = target.getIdLong();
        long amountToSend = amount >= 10
                ? (long) (amount * TAX_PER_EXECUTION)
                : amount;

        if (amount > senderBalance)
            return Status.INSUFFICIENT_BALANCE.args(Bot.strfNumber(amount - senderBalance));

        if (amount <= 0)
            return Status.INVALID_VALUE_PROVIDED.args(amountInput);

        if (targetId == senderId)
            return Status.CANNOT_TRANSFER_TO_YOURSELF;

        if (target.isBot())
            return Status.CANNOT_TRANSFER_TO_BOTS;

        if (newBalanceOverflows(targetId, amount))
            return Status.USER_CANNOT_RECEIVE_GIVEN_AMOUNT;

        try {
            updateBalance(senderId, targetId, amountToSend, amount);
            MembersDAO.upsertUser(target);

            return Status.TRANSACTION_SUCCESSFUL.args(
                    Bot.strfNumber(amountToSend),
                    target.getAsMention()
            );

        } catch (DataAccessException e) {
            LOGGER.error("Could not complete transaction of '%{}' from '{}' to '{}'", amountInput, senderId, targetId, e);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        }

        return Status.PASSED;
    }

    private void updateBalance(long senderId, long receiverId, long send, long remove) {

        DSLContext ctx = DBManager.getContext();

        ctx.transaction((cfg) -> {

            DSLContext trsCtx = DBManager.getContext(cfg);

            EconomyUtil.updateBalance(trsCtx, senderId, remove * -1);
            EconomyUtil.updateBalance(trsCtx, receiverId, send);
        });
    }

    /**
     * Attempts to parse a raw or shortened value (integer) provided directly by the user
     * through the {@code amount} command argument.
     * <p>
     * If the amount cannot be resolved, {@code -1} is returned instead.
     * <p>
     * <strong>This method does not include taxes.</strong>
     *
     * @param input The input value provided by the issuer.
     * @param userBalance The current balance of the issuer.
     * @return The resolved amount intended to be sent.
     */
    private long parseAmountToPay(String input, long userBalance) {

        if (input.equalsIgnoreCase("all"))
            return userBalance;

        String inputlc = input.toLowerCase();

        // By using replaceFirst() we ensure that users will not make
        // mistakes such as sending an obscene amount of money,
        // by "unwantingly" providing "5mm" and sending, yknow... "5000000000000"
        String parse = inputlc
                .replaceFirst("k", "000")
                .replaceFirst("kk", "000000")
                .replaceFirst("m", "000000");

        try {
            return Long.parseLong(parse);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean newBalanceOverflows(long userId, long amountModify) {

        long balance = EconomyUtil.fetchBalance(userId);

        return EconomyUtil.willOverflow(balance, amountModify);
    }
}