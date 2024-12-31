package ofc.bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;
import java.util.function.Predicate;

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
        Role include = ctx.getOption("only", OptionMapping::getAsRole);
        Role exclude = ctx.getOption("except", OptionMapping::getAsRole);
        VoiceChannel channel = ctx.getOption("channel", getFallback(issuer), (opt) -> opt.getAsChannel().asVoiceChannel());
        List<Member> connected = channel.getMembers();
        int connectedCount = connected.size();

        if (connected.isEmpty())
            return Status.VOICE_CHANNEL_IS_EMPTY.args(channel.getName());

        int disconnected = disconnect(connected, guild, (m) -> {
            List<Role> roles = m.getRoles();
            boolean isIncluded = include != null && roles.contains(include);
            boolean isExcluded = exclude != null && roles.contains(exclude);

            return isIncluded && !isExcluded || !isIncluded && !isExcluded;
        });

        return disconnected == 0
                ? Status.NO_USERS_DISCONNECTED
                : Status.SUCCESSFULLY_DISCONNECTING_USERS.args(connectedCount, channel.getName());
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.CHANNEL, "channel", "De qual canal os membros devem ser desconectados.")
                        .setChannelTypes(ChannelType.VOICE),

                new OptionData(OptionType.ROLE, "only", "Apenas os membros com o cargo selecionado serão desconectados."),

                new OptionData(OptionType.ROLE, "except", "Os membros com o cargo selecionado NÃO serão desconectados.")
        );
    }

    private int disconnect(List<Member> members, Guild guild, Predicate<Member> condition) {
        int disconnected = 0;

        for (Member m : members) {
            if (condition == null || condition.test(m)) {
                guild.kickVoiceMember(m).queue();
                disconnected++;
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