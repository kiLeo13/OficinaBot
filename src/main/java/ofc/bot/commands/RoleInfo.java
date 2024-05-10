package ofc.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;

import java.util.List;

@DiscordCommand(name = "roleinfo", description = "Informações gerais sobre um cargo.", autoDefer = true)
public class RoleInfo extends SlashCommand {

    @Option(required = true)
    private static final OptionData ROLE = new OptionData(OptionType.ROLE, "role", "O cargo a saber as informações.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {
        
        Guild guild = ctx.getGuild();
        Role role = ctx.getOption("role", OptionMapping::getAsRole);

        if (role == null)
            return Status.ROLE_NOT_FOUND;

        guild.findMembersWithRoles(role).onSuccess((members) -> {

            MessageEmbed embed = embed(members, role);

            ctx.replyEmbeds(embed);

        }).onError((e) -> {

            MessageEmbed embed = embed(List.of(), role);

            ctx.reply()
                    .setContent("Não foi possível encontrar membros para o cargo, tente novamente.")
                    .setEmbeds(embed)
                    .send();
        });

        return Status.PASSED;
    }

    private MessageEmbed embed(List<Member> members, Role role) {

        List<Member> onlineMembers = members.stream().filter((m) -> m.getOnlineStatus() != OnlineStatus.OFFLINE).toList();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        Guild guild = role.getGuild();
        String memberCount = Bot.strfNumber(members.size());
        String onlineCount = Bot.strfNumber(onlineMembers.size());
        String color = role.getColor() == null ?
                "`Nenhuma`"
                : "`#" + Integer.toHexString(role.getColor().getRGB()).substring(2).toUpperCase() + "`";

        int colorRed = role.getColor() == null ? 0 : role.getColor().getRed();
        int colorGreen = role.getColor() == null ? 0 : role.getColor().getGreen();
        int colorBlue = role.getColor() == null ? 0 : role.getColor().getBlue();
        long creation = role.getTimeCreated().toEpochSecond();

        embedBuilder
                .setTitle(role.getName())
                .setDescription("Informações do cargo <@&" + role.getIdLong() + ">.")
                .setColor(role.getColor())
                .addField("📅 Criação", "<t:" + creation + ">\n<t:" + creation + ":R>", true)
                .addField("💻 Role ID", "`" + role.getIdLong() + "`", true)
                .addField("🤖 Integração", role.isManaged() ? "`Sim`" : "`Não`", true)
                .addField((role.isMentionable() ? "🔔" : "🔕") + " Mencionável", role.isMentionable() ? "`Sim`" : "`Não`", true)
                .addField("📃 Mostrar Separadamente", role.isHoisted() ? "`Sim`" : "`Não`", true)
                .addField("🎨 Cor", String.format("HEX: `%s`\nRGB: `%s, %s, %s`",
                        color,
                        colorRed < 10 ? "0" + colorRed : String.valueOf(colorRed),
                        colorGreen < 10 ? "0" + colorGreen : String.valueOf(colorGreen),
                        colorBlue < 10 ? "0" + colorBlue : String.valueOf(colorBlue)
                ), true)
                .addField("👥 Membros", "Total: `" + memberCount + "`\nOnline: `" + onlineCount + "`", true)
                .addField("🔒 Permissões", permissions(role), role.getPermissions().isEmpty())
                .setFooter(guild.getName(), guild.getIconUrl());

        RoleIcon icon = role.getIcon();

        if (icon != null)
            embedBuilder.setThumbnail(icon.getIconUrl());
        return embedBuilder.build();
    }

    private String permissions(Role role) {
        StringBuilder builder = new StringBuilder().append("```\n");
        List<Permission> permissions = role.getPermissions().stream().toList();

        if (permissions.isEmpty())
            return "`Nenhuma`";

        for (int i = 0; i < permissions.size(); i++) {
            if (i != 0) builder.append(", ");

            builder.append(permissions.get(i).getName());
        }

        builder.append(".\n```");
        return builder.toString();
    }
}