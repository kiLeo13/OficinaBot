package ofc.bot.commands.staff_list;

import com.google.gson.Gson;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import ofc.bot.Main;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.CommandPermission;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.handlers.requests.RequesterManager;
import ofc.bot.internal.data.BotFiles;
import ofc.bot.util.Bot;
import ofc.bot.util.content.Channels;
import ofc.bot.util.exclusions.ExclusionType;
import ofc.bot.util.exclusions.ExclusionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DiscordCommand(name = "refresh_staff", description = "Atualize a lista de membros da staff.", autoDefer = true)
@CommandPermission(Permission.ADMINISTRATOR)
public class RefreshStaff extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshStaff.class);
    protected static final File FILE = new File(BotFiles.DIR_CONTENT, "staffconfig.json");
    private static final Gson GSON = new Gson();
    private static final int COOLDOWN = 120000;
    private static boolean isUpdating = false;
    private static long lastUsed = 0L;

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Member member = ctx.getIssuer();
        Guild guild = ctx.getGuild();
        TextChannel staffsChannel = Main.getApi().getTextChannelById(Channels.D.id());
        InputData parsed = parse();
        long now = System.currentTimeMillis();
        long wait = COOLDOWN - (now - lastUsed);

        if (wait > 0)
            return Status.PLEASE_WAIT_COOLDOWN.args(Bot.parsePeriod(wait / 1000));

        if (staffsChannel == null)
            return Status.CHANNEL_NOT_FOUND;

        if (parsed == null)
            return Status.COULD_NOT_CONVERT_DATA_FROM_FILE.args(FILE.getAbsolutePath());

        if (isUpdating)
            return Status.COMMAND_IS_ALREADY_RUNNING;

        isUpdating = true;
        LOGGER.info("Looking for Staff Banner...");

        if (!parsed.banner().isBlank() && !parsed.bannerMessageId().isBlank()) {
            InputStream banner = RequesterManager.get(parsed.banner(), ByteArrayInputStream::new);

            if (banner != null) {
                staffsChannel.editMessageAttachmentsById(parsed.bannerMessageId(), FileUpload.fromData(banner, "banner.gif"))
                        .setReplace(true)
                        .queue();
                LOGGER.info("Banner was found and successfully updated!");
            } else
                LOGGER.warn("Could not download banner at {}", parsed.banner());
        }

        for (StaffMessageBody data : parsed.staffs()) {
            LOGGER.info("Preparing for role lookup...");

            Role role = guild.getRoleById(data.role());

            if (role == null)
                return Status.ROLE_NOT_FOUND_BY_ID.args(data.role());

            if (data.message() == null) {
                ctx.reply(true)
                        .setContentFormat("O id da mensagem para `%s` não foi encontrado.", role.getName())
                        .send();
                return Status.PASSED;
            }

            push(guild, staffsChannel, role, data);

            try { Thread.sleep(6000); }
            catch (InterruptedException ignored) {}
        }

        ctx.reply()
                .setContent(member.getAsMention() + " todas as mensagens encontradas foram editadas.")
                .send();

        LOGGER.info("All messages found were successfully updated!");
        isUpdating = false;

        return Status.PASSED;
    }

    private void push(Guild guild, TextChannel output, Role role, StaffMessageBody messageData) {

        MessageEditBuilder builder = new MessageEditBuilder();
        String title = messageData.title() == null ? "" : messageData.title();
        String footer = messageData.footer() == null ? "" : messageData.footer();

        LOGGER.info("Looking for members with role {} ({})...", role.getName(), role.getId());
        List<Long> excluded = ExclusionUtil.fetchExcluded(ExclusionType.APPEAR_ON_STAFF_LIST);
        List<Member> members = guild.findMembersWithRoles(role)
                .setTimeout(30, TimeUnit.SECONDS)
                .get()
                .stream()
                .filter(m -> !excluded.contains(m.getIdLong()))
                .sorted(Comparator.comparing(member -> member.getUser().getEffectiveName()))
                .toList();
        String amount = members.size() < 10 ? "0" + members.size() : String.valueOf(members.size());
        LOGGER.info("Found {} members with role {}!", members.size(), role.getName());

        String yeah = String.format("""
                %s
                
                ## %s
                
                %s
                
                %s
                """,
                title,
                members.isEmpty() ? role.getAsMention() + " (*Sem membros*)" : String.format("%s (%s)", role.getAsMention(), amount),
                format(members),
                footer
        );

        builder.setContent(yeah);

        LOGGER.info("Updating message for {} ({})", role.getName(), role.getId());
        output.editMessageById(messageData.message(), builder.build()).setAllowedMentions(List.of()).queue(null, e -> {
            LOGGER.error("Could not edit staffs message for role {}", role.getId(), e);
        });
        LOGGER.info("========================================\n");

        lastUsed = System.currentTimeMillis();
    }

    private String format(final List<Member> members) {
        StringBuilder builder = new StringBuilder();

        for (Member m : members) {
            String text = String.format("- %s\n", m.getAsMention());
            builder.append(text);
        }

        return builder.toString().strip();
    }

    protected static boolean isUpdating() {
        return isUpdating;
    }

    static InputData parse() {
        try {
            String json = String.join("", Files.readAllLines(Path.of(FILE.getAbsolutePath())));

            return GSON.fromJson(json, InputData.class);
        } catch (IOException e) {
            return null;
        }
    }
}