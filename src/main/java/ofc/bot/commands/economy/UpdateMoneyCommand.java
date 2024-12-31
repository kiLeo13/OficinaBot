package ofc.bot.commands.economy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.UserEconomy;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
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

@DiscordCommand(
        name = "update-money",
        description = "ATUALIZA o saldo do usuário fornecido com a quantia fornecida, valores negativos removerão dinheiro.",
        permission = Permission.MANAGE_SERVER
)
public class UpdateMoneyCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMoneyCommand.class);
    private final UserEconomyRepository ecoRepo;

    public UpdateMoneyCommand(UserEconomyRepository ecoRepo) {
        this.ecoRepo = ecoRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        User issuer = ctx.getUser();
        User target = ctx.getOption("target", issuer, OptionMapping::getAsUser);
        long amount = ctx.getSafeOption("amount", OptionMapping::getAsLong);
        long issuerId = ctx.getUserId();
        long targetId = target.getIdLong();

        if (amount == 0)
            return Status.NOTHING_CHANGED_WITH_REASON.args("`$0` foi fornecido");

        try {
            UserEconomy eco = ecoRepo.findByUserId(targetId, UserEconomy.fromUserId(targetId));

            if (Bot.overflows(eco.getBalance(), amount))
                return Status.USER_CANNOT_RECEIVE_GIVEN_AMOUNT;

            eco.modifyBalance(amount).tickUpdate();
            ecoRepo.upsert(eco);
            dispatchBalanceUpdateEvent(issuerId, targetId, amount);

            return Status.ECONOMY_SUCCESSFULLY_UPDATED_BALANCE.args(target.getAsMention(), Bot.fmtNum(amount));
        } catch (DataAccessException e) {
            LOGGER.error("Could not access balance of '{}'", targetId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "amount", "A quantia a ser atualizada.", true)
                        .setRequiredRange((long) OptionData.MIN_NEGATIVE_NUMBER, (long) OptionData.MAX_POSITIVE_NUMBER),

                new OptionData(OptionType.USER, "target", "O alvo a atualizar o saldo.")
        );
    }

    private void dispatchBalanceUpdateEvent(long userId, long targetId, long amount) {
        BankTransaction tr = new BankTransaction(userId, targetId, amount, CurrencyType.OFICINA, TransactionType.BALANCE_UPDATED);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}