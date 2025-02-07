package ofc.bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(
        name = "disconnect-all",
        description = "Desconecta todos os usuários do canal de voz fornecido.",
        permission = Permission.VOICE_MOVE_OTHERS
)
public class DisconnectAllCommand extends SlashCommand {

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Guild guild = ctx.getGuild();
        Member issuer = ctx.getIssuer();
        VoiceChannel channel = ctx.getOption("channel", getFallback(issuer), (opt) -> opt.getAsChannel().asVoiceChannel());
        List<Member> connected = channel.getMembers();

        if (connected.isEmpty())
            return Status.VOICE_CHANNEL_IS_EMPTY.args(channel.getName());

        for (Member m : connected) {
            guild.kickVoiceMember(m).queue();
        }

        return connected.isEmpty()
                ? Status.NO_USERS_DISCONNECTED
                : Status.SUCCESSFULLY_DISCONNECTING_USERS.args(connected.size(), channel.getName());
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.CHANNEL, "channel", "De qual canal os membros devem ser desconectados.")
                        .setChannelTypes(ChannelType.VOICE)
        );
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