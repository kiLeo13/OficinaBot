package ofc.bot.commands.userinfo.custom;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.domain.entity.CustomUserinfo;
import ofc.bot.domain.sqlite.repository.CustomUserinfoRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "customize description")
public class SetDescriptionCommand extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetDescriptionCommand.class);
    private final CustomUserinfoRepository csinfoRepo;

    public SetDescriptionCommand(CustomUserinfoRepository csinfoRepo) {
        this.csinfoRepo = csinfoRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        User sender = ctx.getUser();
        String input = ctx.getOption("description", "remove", OptionMapping::getAsString);
        boolean isRemotion = input.equalsIgnoreCase("remove");
        long userId = sender.getIdLong();

        try {
            String desc = isRemotion ? null : input;
            CustomUserinfo csInfo = csinfoRepo.findByUserId(userId, CustomUserinfo.fromUserId(userId))
                    .setDescription(desc)
                    .tickUpdate();

            csinfoRepo.upsert(csInfo);
            return Status.USERINFO_DESCRIPTION_SUCCESSFULLY_UPDATED;
        } catch (DataAccessException e) {
            LOGGER.error("Could not update userinfo description of user '{}'", userId, e);
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;
        }
    }

    @Override
    protected void init() {
        setDesc("Define/reseta a descrição da embed apresentada no userinfo.");

        addOpt(OptionType.STRING, "description", "A descrição da embed do userinfo. Ignore ou forneça \"remove\" para remover.", 1, 200);
    }
}