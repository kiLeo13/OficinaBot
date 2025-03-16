package ofc.bot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.domain.entity.enums.PunishmentType;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "kick", permission = Permission.KICK_MEMBERS)
public class KickCommand extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(KickCommand.class);

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Member target = ctx.getOption("member", OptionMapping::getAsMember);
        String reason = ctx.getSafeOption("reason", OptionMapping::getAsString);
        Guild guild = ctx.getGuild();
        Member self = guild.getSelfMember();
        long guildId = guild.getIdLong();

        if (target == null)
            return Status.MEMBER_NOT_FOUND;

        if (!self.canInteract(target))
            return Status.BOT_CANNOT_KICK_PROVIDED_MEMBER;

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
    protected void init() {
        setDesc("Expulsa um membro do servidor.");

        addOpt(OptionType.USER, "member", "O membro a ser expulso", true);
        addOpt(OptionType.STRING, "reason", "O motivo da expulsão.", (it) -> it.setRequired(true)
                .setMaxLength(500));
    }
}