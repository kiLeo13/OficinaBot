package ofc.bot.commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import org.slf4j.Logger;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.LoggerFactory;

@DiscordCommand(name = "rolemembers", description = "Veja os membros de um cargo", autoDefer = true)
public class RoleMembers extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleMembers.class);

    @Option(required = true)
    private static final OptionData ROLE = new OptionData(OptionType.ROLE, "role", "O cargo a verificar os membros.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Role role = ctx.getSafeOption("role", OptionMapping::getAsRole);
        Guild guild = ctx.getGuild();

        guild.findMembersWithRoles(role).setTimeout(30, TimeUnit.SECONDS).onSuccess((members) -> {

            int count = members.size();

            if (count == 0) {
                ctx.reply(Status.ROLE_HAS_NO_MEMBERS);
                return;
            }

            String statistics = getPrettyStatistics(role, members);
            String formatMembers = format(members);

            handleResponse(formatMembers, statistics, ctx);

        }).onError((e) -> {

            LOGGER.error("Could not perform role members lookup", e);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });

        return Status.PASSED;
    }

    private void handleResponse(String formattedMembers, String statistics, CommandContext cmd) {

        String output = statistics + "```txt\n" + formattedMembers + "```";

        if (output.length() > Message.MAX_CONTENT_LENGTH) {
            handleAsFile(formattedMembers, statistics, cmd);
            return;
        }

        cmd.reply(output);
    }

    private void handleAsFile(String formattedMembers, String statistics, CommandContext cmd) {

        cmd.reply()
                .setContent(statistics)
                .setFiles(FileUpload.fromData(formattedMembers.getBytes(), "members.txt"))
                .send();
    }

    private String getPrettyStatistics(Role role, List<Member> members) {

        List<Member> online = members.stream().filter((m) -> m.getOnlineStatus() != OnlineStatus.OFFLINE).toList();
        List<Member> offline = members.stream().filter((m) -> m.getOnlineStatus() == OnlineStatus.OFFLINE).toList();

        return String.format("""
                Cargo `%s`
                Online: `%s`
                Offline: `%s`
                Total: `%s`
                """,
                role.getName(),
                Bot.strfNumber(online.size()),
                Bot.strfNumber(offline.size()),
                Bot.strfNumber(members.size())
        );
    }

    protected static String format(final List<Member> members) {
        int maxLength = 0;
        List<String> ids = members.stream().map(Member::getId).toList();
        StringBuilder formatted = new StringBuilder();

        for (String id : ids) {
            int length = id.length();

            if (length > maxLength)
                maxLength = length;
        }

        for (Member m : members) {
            String name = m.getUser().getName();
            String id = m.getId();
            int spaces = maxLength - id.length();

            String line = id + " ".repeat(Math.max(0, spaces)) +
                    "  ->  " + name;

            formatted.append(line);
            formatted.append("\n");
        }

        return formatted.toString().strip();
    }
}