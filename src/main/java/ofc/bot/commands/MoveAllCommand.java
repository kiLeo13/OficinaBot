package ofc.bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(
        name = "move-all",
        description = "Move todos os usuários de um canal de voz para outro.",
        permission = Permission.VOICE_MOVE_OTHERS
)
public class MoveAllCommand extends SlashCommand {

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Member issuer = ctx.getIssuer();
        Role include = ctx.getOption("only", OptionMapping::getAsRole);
        Role exclude = ctx.getOption("except", OptionMapping::getAsRole);
        VoiceChannel oldChannel = ctx.getOption("origin", getFallback(issuer), (opt) -> opt.getAsChannel().asVoiceChannel());
        VoiceChannel newChannel = ctx.getSafeOption("destination", OptionMapping::getAsChannel).asVoiceChannel();

        // "getOption()" methods with fallbacks are usually not-null, but in this case
        // we cannot assure the user is actually connected to a voice channel
        if (oldChannel == null)
            return Status.ISSUER_NOT_IN_VOICE_CHANNEL;

        if (oldChannel.getIdLong() == newChannel.getIdLong())
            return Status.SAME_CHANNEL_FOR_MULTIPLE_ARGUMENTS;

        List<Member> connected = oldChannel.getMembers();
        List<Member> filtered = connected.stream().filter((m) -> {
            List<Role> roles = m.getRoles();
            boolean isIncluded = include != null && roles.contains(include);
            boolean isExcluded = exclude != null && roles.contains(exclude);

            return isIncluded && !isExcluded || !isIncluded && !isExcluded;
        }).toList();

        int movedCount = move(filtered, newChannel);

        return movedCount == 0
                ? Status.NO_USERS_MOVED
                : Status.SUCCESSFULLY_MOVING_USERS.args(movedCount, newChannel.getName());
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.CHANNEL, "destination", "Para qual canal de voz os membros devem ser movidos.", true)
                        .setChannelTypes(ChannelType.VOICE),

                new OptionData(OptionType.ROLE, "only", "Apenas os membros com o cargo selecionado serão movidos."),

                new OptionData(OptionType.ROLE, "except", "Os membros com o cargo selecionado NÃO serão movidos."),

                new OptionData(OptionType.CHANNEL, "origin", "Partindo de qual canal de voz os membros devem ser movidos.")
                        .setChannelTypes(ChannelType.VOICE)
        );
    }

    private int move(List<Member> members, VoiceChannel newChannel) {
        Guild guild = newChannel.getGuild();
        int moved = members.size();

        for (Member m : members)
            guild.moveVoiceMember(m, newChannel).queue(null, new ErrorHandler().ignore(ErrorResponse.USER_NOT_CONNECTED));

        return moved;
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