package ofc.bot.commands.economy;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.domain.entity.AppUser;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.domain.sqlite.repository.UserRepository;
import ofc.bot.events.entities.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@DiscordCommand(name = "pay", description = "Envie dinheiro para outro usuário.", cooldown = 30)
public class PayCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(PayCommand.class);
    private static final double TAX_PER_EXECUTION = 1 - 0.05; // 5%
    private final UserEconomyRepository ecoRepo;
    private final UserRepository userRepo;

    public PayCommand(UserEconomyRepository ecoRepo, UserRepository userRepo) {
        this.ecoRepo = ecoRepo;
        this.userRepo = userRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        User sender = ctx.getUser();
        User target = ctx.getSafeOption("user", OptionMapping::getAsUser);
        String amountInput = ctx.getSafeOption("amount", OptionMapping::getAsString);
        long senderId = sender.getIdLong();
        long senderBalance = ecoRepo.fetchBalanceByUserId(senderId);
        long amountTotal = parseAmountToPay(amountInput, senderBalance);
        long targetId = target.getIdLong();
        long amountToSend = amountTotal >= 10
                ? (long) (amountTotal * TAX_PER_EXECUTION)
                : amountTotal;

        if (amountTotal > senderBalance)
            return Status.INSUFFICIENT_BALANCE_VALUE.args(Bot.fmtNum(amountTotal - senderBalance));

        if (amountTotal <= 0)
            return Status.INVALID_VALUE_PROVIDED.args(amountInput);

        if (targetId == senderId)
            return Status.CANNOT_TRANSFER_TO_YOURSELF;

        if (target.isBot())
            return Status.CANNOT_TRANSFER_TO_BOTS;

        try {
            ecoRepo.transfer(senderId, targetId, amountToSend, amountTotal);
            userRepo.upsert(AppUser.fromUser(target));

            dispatchSendMoneyEvent(senderId, targetId, amountTotal);

            return Status.TRANSACTION_SUCCESSFUL.args(
                    Bot.fmtNum(amountToSend),
                    target.getAsMention()
            );
        } catch (DataAccessException e) {
            LOGGER.error("Could not complete transaction of '%{}' from '{}' to '{}'", amountInput, senderId, targetId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "O usuário para enviar o dinheiro.", true),

                // We use the String data type here to make the "all" shortcut possible
                new OptionData(OptionType.STRING, "amount", "A quantia a ser enviada (forneça \"all\" sem aspas para transferir tudo).", true)
        );
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

    private void dispatchSendMoneyEvent(long senderId, long targetId, long amount) {
        BankTransaction tr = new BankTransaction(senderId, targetId, amount, CurrencyType.OFICINA, TransactionType.MONEY_TRANSFERRED);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}