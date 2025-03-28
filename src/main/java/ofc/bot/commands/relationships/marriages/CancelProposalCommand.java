package ofc.bot.commands.relationships.marriages;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.domain.entity.MarriageRequest;
import ofc.bot.domain.sqlite.repository.MarriageRequestRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "marriage cancel")
public class CancelProposalCommand extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelProposalCommand.class);
    private final MarriageRequestRepository mreqRepo;

    public CancelProposalCommand(MarriageRequestRepository mreqRepo) {
        this.mreqRepo = mreqRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        User issuer = ctx.getUser();
        User target = ctx.getSafeOption("user", OptionMapping::getAsUser);
        long issuerId = issuer.getIdLong();
        long targetId = target.getIdLong();
        MarriageRequest req = mreqRepo.findByStrictIds(issuerId, targetId);

        if (req == null)
            return Status.NO_ACTIVE_PROPOSAL_SENT_TO_USER.args(target.getEffectiveName());

        try {
            mreqRepo.delete(req);

            return Status.PROPOSAL_REMOVED_SUCCESSFULLY.args(target.getEffectiveName());
        } catch (DataAccessException e) {
            LOGGER.error("Could not remove proposal from '{}' to '{}'", issuerId, targetId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @Override
    protected void init() {
        setDesc("Cancele uma proposta de casamento enviada à alguém.");

        addOpt(OptionType.USER, "user", "A pessoa na qual você quer remover a proposta de casamento.", true);
    }
}