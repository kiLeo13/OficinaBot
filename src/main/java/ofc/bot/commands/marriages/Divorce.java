package ofc.bot.commands.marriages;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.Main;
import ofc.bot.databases.DBManager;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.MarriageUtil;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ofc.bot.databases.entities.tables.Marriages.MARRIAGES;

@DiscordCommand(name = "divorce", description = "Divorcie-se de algum usuário. (ZERO REEMBOLSO).")
public class Divorce extends SlashCommand {
    private static final long JANJO = 742729586659295283L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Divorce.class);

    @Option(required = true)
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "O usuário a se divorciar.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User sender = ctx.getUser();
        User target = ctx.getSafeOption("user", OptionMapping::getAsUser);
        String requesterName = sender.getEffectiveName();
        long requesterId = sender.getIdLong();
        long targetId = target.getIdLong();

        if (targetId == requesterId)
            return Status.CANNOT_DIVORCE_YOURSELF;

        boolean isMarriedToUser = MarriageUtil.areMarried(requesterId, targetId);

        if (!isMarriedToUser)
            return Status.USER_IS_NOT_MARRIED_TO_TARGET.args(target.getAsMention());

        try {
            deleteMarriage(requesterId, targetId);

            if (targetId != JANJO)
                informTarget(requesterName, targetId);

            return Status.DIVORCED_SUCCESSFULLY.setEphm(true);

        } catch (DataAccessException e) {
            LOGGER.error("Could not divorce '{}' and '{}'", requesterId, targetId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }
    
    private void informTarget(String requesterName, long targetId) {
        Main.getApi().openPrivateChannelById(targetId)
                .flatMap((dm) -> dm.sendMessageFormat("O seu casamento com %s infelizmente chegou ao fim. 😔", requesterName))
                .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
    }
    
    private void deleteMarriage(long spouse, long anotherSpouse) {
        
        DSLContext ctx = DBManager.getContext();
        
        ctx.deleteFrom(MARRIAGES)
                .where(MARRIAGES.REQUESTER_ID.eq(spouse).and(MARRIAGES.TARGET_ID.eq(anotherSpouse)))
                .or(MARRIAGES.REQUESTER_ID.eq(anotherSpouse).and(MARRIAGES.TARGET_ID.eq(spouse)))
                .execute();
    }
}