package ofc.bot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.util.content.annotations.commands.Option;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.CommandPermission;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;

import java.util.List;

@DiscordCommand(name = "move_all", description = "Move todos os usuários de um canal de voz para outro.")
@CommandPermission(Permission.VOICE_MOVE_OTHERS)
public class MoveAll extends SlashCommand {

    @Option
    private static final OptionData ONLY = new OptionData(OptionType.ROLE, "only", "Apenas os membros com o cargo selecionado serão movidos.");

    @Option
    private static final OptionData EXCEPT = new OptionData(OptionType.ROLE, "except", "Os membros com o cargo selecionado NÃO serão movidos.");

    @Option
    private static final OptionData OLD_CHANNEL = new OptionData(OptionType.CHANNEL, "old-channel", "Partindo de qual canal de voz os membros devem ser movidos.")
            .setChannelTypes(ChannelType.VOICE);

    @Option(required = true)
    private static final OptionData NEW_CHANNEL = new OptionData(OptionType.CHANNEL, "new-channel", "Para qual canal de voz os membros devem ser movidos.")
            .setChannelTypes(ChannelType.VOICE);

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Member issuer = ctx.getIssuer();
        Role include = ctx.getOption("only", OptionMapping::getAsRole);
        Role exclude = ctx.getOption("except", OptionMapping::getAsRole);
        VoiceChannel oldChannel = ctx.getOption("old-channel", getFallback(issuer), (opt) -> opt.getAsChannel().asVoiceChannel());
        VoiceChannel newChannel = ctx.getSafeOption("new-channel", OptionMapping::getAsChannel).asVoiceChannel();

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