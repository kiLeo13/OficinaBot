package ofc.bot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.util.content.annotations.commands.Option;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.CommandPermission;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@DiscordCommand(name = "disconnect_all", description = "Desconecta todos os usuários do canal de voz fornecido.")
@CommandPermission(Permission.VOICE_MOVE_OTHERS)
public class DisconnectAll extends SlashCommand {

    @Option
    private static final OptionData ONLY = new OptionData(OptionType.ROLE, "only", "Apenas os membros com o cargo selecionado serão desconectados.");

    @Option
    private static final OptionData EXCEPT = new OptionData(OptionType.ROLE, "except", "Os membros com o cargo selecionado NÃO serão desconectados.");

    @Option(required = true)
    private static final OptionData CHANNEL = new OptionData(OptionType.CHANNEL, "channel", "De qual canal os membros devem ser desconectados.")
            .setChannelTypes(ChannelType.VOICE);

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Guild guild = ctx.getGuild();
        Member issuer = ctx.getIssuer();
        Role include = ctx.getOption("only", OptionMapping::getAsRole);
        Role exclude = ctx.getOption("except", OptionMapping::getAsRole);
        VoiceChannel channel = ctx.getOption("channel", getFallback(issuer), (opt) -> opt.getAsChannel().asVoiceChannel());
        List<Member> connected = channel.getMembers();
        int connectedCount = connected.size();

        if (connected.isEmpty())
            return Status.VOICE_CHANNEL_IS_EMPTY.args(channel.getName());

        List<Member> disconnected = disconnect(connected, guild, (m) -> {

            List<Role> roles = m.getRoles();
            boolean isIncluded = include != null && roles.contains(include);
            boolean isExcluded = exclude != null && roles.contains(exclude);

            return isIncluded && !isExcluded || !isIncluded && !isExcluded;
        });

        return disconnected.isEmpty()
                ? Status.NO_USERS_DISCONNECTED
                : Status.SUCCESSFULLY_DISCONNECTING_USERS.args(connectedCount, channel.getName());
    }

    private List<Member> disconnect(List<Member> members, Guild guild, Predicate<Member> condition) {
        List<Member> disconnected = new ArrayList<>();

        for (Member m : members) {
            if (condition == null || condition.test(m)) {
                guild.kickVoiceMember(m).queue();
                disconnected.add(m);
            }
        }

        return disconnected;
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