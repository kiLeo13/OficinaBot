package ofc.bot.commands.economy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.CommandPermission;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.util.Bot;
import ofc.bot.util.EconomyUtil;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "set_money", description = "DEFINE um novo saldo para um usuário.")
@CommandPermission(Permission.MANAGE_SERVER)
public class SetMoney extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetMoney.class);

    @Option(required = true)
    private static final OptionData AMOUNT = new OptionData(OptionType.INTEGER, "amount", "A quantia a ser definida.")
            .setRequiredRange((long) OptionData.MIN_NEGATIVE_NUMBER, (long) OptionData.MAX_POSITIVE_NUMBER);

    @Option
    private static final OptionData TARGET = new OptionData(OptionType.USER, "target", "O alvo a definir o saldo.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User issuer = ctx.getUser();
        User target = ctx.getOption("target", issuer, OptionMapping::getAsUser);
        long amount = ctx.getSafeOption("amount", OptionMapping::getAsInt);
        long targetId = target.getIdLong();

        try {
            EconomyUtil.setBalance(amount, targetId);

            ctx.reply(Status.BALANCE_SET_SUCCESSFULLY.args(target.getAsMention(), Bot.strfNumber(amount)));
        } catch (DataAccessException e) {
            LOGGER.error("Could not access balance of '{}'", targetId, e);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        }

        return Status.PASSED;
    }
}