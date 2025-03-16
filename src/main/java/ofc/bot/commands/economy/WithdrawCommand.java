package ofc.bot.commands.economy;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.domain.entity.UserEconomy;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.handlers.games.betting.BetManager;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "withdraw")
public class WithdrawCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawCommand.class);
    private static final BetManager betManager = BetManager.getManager();
    private final UserEconomyRepository ecoRepo;

    public WithdrawCommand(UserEconomyRepository ecoRepo) {
        this.ecoRepo = ecoRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        User user = ctx.getUser();
        long userId = user.getIdLong();
        UserEconomy userEco = ecoRepo.findByUserId(userId, UserEconomy.fromUserId(userId));
        String amountInput = ctx.getSafeOption("amount", OptionMapping::getAsString);
        int bank = userEco.getBank();

        if (betManager.isBetting(userId))
            return Status.YOU_CANNOT_DO_THIS_WHILE_BETTING;

        int amount = Bot.parseAmount(amountInput, bank);
        if (amount <= 0)
            return Status.INVALID_VALUE_PROVIDED.args(amountInput);

        if (bank <= 0 || amount > bank) {
            MessageEmbed embed = embedInsufficient(user);
            return ctx.replyEmbeds(Status.YOU_GOT_NO_MONEY_TO_WITHDRAW, embed);
        }

        try {
            userEco.modifyBalance(amount, -amount).tickUpdate();
            ecoRepo.upsert(userEco);

            MessageEmbed embed = embedSuccess(user, amount);
            return ctx.replyEmbeds(embed);
        } catch (DataAccessException e) {
            LOGGER.error("Could not withdraw {} from user {}", amount, userId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @Override
    protected void init() {
        setDesc("Saque dinheiro da sua conta bancária.");

        addOpt(OptionType.STRING, "amount", "A quantia a ser sacada (forneça \"all\" sem aspas para sacar tudo).", true);
    }

    private MessageEmbed embedInsufficient(User user) {
        return EmbedFactory.embedBankAction(user, EmbedFactory.DANGER_RED, "❌ Você não tem dinheiro para sacar.");
    }

    private MessageEmbed embedSuccess(User user, int amount) {
        return EmbedFactory.embedBankAction(user, EmbedFactory.OK_GREEN, "✅ Sacou $%s do seu banco!", Bot.fmtNum(amount));
    }
}