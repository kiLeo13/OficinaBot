package ofc.bot.commands.stafflist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.Channels;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.ArrayList;
import java.util.List;

@DiscordCommand(
        name = "staff-regenerate",
        description = "Regenere as mensagens do chat staffs-oficina.",
        permission = Permission.ADMINISTRATOR
)
public class StaffListMessagesRegenerateCommand extends SlashCommand {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        ctx.ack(true);

        Guild guild = ctx.getGuild();
        InputData parsed = RefreshStaffListMessageCommand.parse();
        TextChannel staffsChannel = guild.getTextChannelById(Channels.D.id());

        if (RefreshStaffListMessageCommand.isUpdating())
            return Status.STAFF_MESSAGES_UPDATE_STILL_IN_PROGRESS;

        if (staffsChannel == null)
            return Status.CHANNEL_NOT_FOUND;

        if (parsed == null)
            return Status.COULD_NOT_CONVERT_DATA_FROM_FILE.args(RefreshStaffListMessageCommand.FILE.getName());

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
        Bot.writeToFile(json, RefreshStaffListMessageCommand.FILE);

        return ctx.reply("Todas as mensagens foram regeneradas! Use `.staffs` para atualizar os dados.");
    }
}