package ofc.bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(name = "move-all", permission = Permission.VOICE_MOVE_OTHERS)
public class MoveAllCommand extends SlashCommand {

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Member issuer = ctx.getIssuer();
        Guild guild = ctx.getGuild();
        VoiceChannel oldChannel = ctx.getOption("origin", getFallback(issuer), (opt) -> opt.getAsChannel().asVoiceChannel());
        VoiceChannel newChannel = ctx.getSafeOption("destination", OptionMapping::getAsChannel).asVoiceChannel();

        // "getOption()" methods with fallbacks are usually not-null, but in this case
        // we cannot assure the user is actually connected to a voice channel
        if (oldChannel == null)
            return Status.ISSUER_NOT_IN_VOICE_CHANNEL;

        if (oldChannel.getIdLong() == newChannel.getIdLong())
            return Status.SAME_CHANNEL_FOR_MULTIPLE_ARGUMENTS;

        List<Member> connected = oldChannel.getMembers();
        for (Member m : connected) {
            guild.moveVoiceMember(m, newChannel).queue(null, new ErrorHandler().ignore(ErrorResponse.USER_NOT_CONNECTED));
        }

        return connected.isEmpty()
                ? Status.NO_USERS_MOVED
                : Status.SUCCESSFULLY_MOVING_USERS.args(connected.size(), newChannel.getName());
    }

    @Override
    protected void init() {
        setDesc("Move todos os usuários de um canal de voz para outro.");

        addOpt(OptionType.CHANNEL, "destination", "Para qual canal de voz os membros devem ser movidos.", (it) -> it.setRequired(true)
                .setChannelTypes(ChannelType.VOICE));
        addOpt(OptionType.CHANNEL, "origin", "Partindo de qual canal de voz os membros devem ser movidos.", (it) -> it.setChannelTypes(ChannelType.VOICE));
    }

    // We can safely ignore the null warning at this point,
    // since we have CacheFlag.VOICE_STATE enabled
    @SuppressWarnings("DataFlowIssue")
    private VoiceChannel getFallback(Member member) {
        GuildVoiceState state = member.getVoiceState();
        AudioChannelUnion channel = state.getChannel();

        return channel == null || channel.getType() != ChannelType.VOICE
                ? null
                : channel.asVoiceChannel();
    }
}