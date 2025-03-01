package ofc.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.domain.entity.enums.PunishmentType;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@DiscordCommand(name = "kick", description = "Expulsa um membro do servidor.", permission = Permission.KICK_MEMBERS)
public class KickCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(KickCommand.class);

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Member target = ctx.getOption("member", OptionMapping::getAsMember);
        String reason = ctx.getSafeOption("reason", OptionMapping::getAsString);
        long guildId = ctx.getGuildId();

        if (target == null)
            return Status.MEMBER_NOT_FOUND;

        target.kick().reason(reason).queue(v -> {
            MessageEmbed embed = EmbedFactory.embedPunishment(target.getUser(), PunishmentType.KICK, reason, 0);

            ctx.replyEmbeds(embed);
        }, (err) -> {
            LOGGER.error("Could not kick member {} from guild {}", target.getId(), guildId, err);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });
        return Status.OK;
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "member", "O membro a ser expulso", true),
                new OptionData(OptionType.STRING, "reason", "O motivo da expulsão.", true)
                        .setMaxLength(500)
        );
    }
}