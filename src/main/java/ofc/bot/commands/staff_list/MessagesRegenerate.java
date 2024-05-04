package ofc.bot.commands.staff_list;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.CommandPermission;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.Channels;

import java.util.ArrayList;
import java.util.List;

@DiscordCommand(name = "staff_regenerate", description = "Regenere as mensagens do chat staffs-oficina.", autoDefer = true, deferEphemeral = true)
@CommandPermission(Permission.ADMINISTRATOR)
public class MessagesRegenerate extends SlashCommand {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Guild guild = ctx.getGuild();
        InputData parsed = RefreshStaff.parse();
        TextChannel staffsChannel = guild.getTextChannelById(Channels.D.id());

        if (RefreshStaff.isUpdating()) {
            ctx.reply("Algumas mensagens de `staff-oficina` ainda estão sendo atualizadas! Por favor, aguarde.");
            return Status.PASSED;
        }

        if (staffsChannel == null)
            return Status.CHANNEL_NOT_FOUND;

        if (parsed == null)
            return Status.COULD_NOT_CONVERT_DATA_FROM_FILE.args(RefreshStaff.FILE.getName());

        String banner = parsed.banner();
        Message newBannerMessage = staffsChannel.sendMessage("`Banner Message`").complete();
        List<StaffMessageBody> staffs = new ArrayList<>();
        InputData newInputData = new InputData(banner, newBannerMessage.getId(), staffs);

        for (StaffMessageBody data : parsed.staffs()) {
            Role role = guild.getRoleById(data.role());

            if (role == null) {
                return Status.ROLE_NOT_FOUND_BY_ID.args(data.role());
            } else {
                Message sent = staffsChannel.sendMessage("`" + role.getName() + "`").complete();
                staffs.add(new StaffMessageBody(data.title(), data.role(), sent.getId(), data.footer()));
            }
        }

        String json = gson.toJson(newInputData);
        Bot.writeToFile(json, RefreshStaff.FILE);

        ctx.reply("Todas as mensagens foram regeneradas! Use `.staffs` para atualizar os dados.");

        return Status.PASSED;
    }
}