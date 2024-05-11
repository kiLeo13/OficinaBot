package ofc.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageProxy;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@DiscordCommand(name = "avatar", description = "Mostra o avatar de um usuário.")
public class Avatar extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Avatar.class);

    @Option
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "O usuário a verificar o avatar.");

    @Option
    private static final OptionData IS_GUILD = new OptionData(OptionType.BOOLEAN, "local", "Se o avatar mostrado deve ser o específico do servidor atual (Padrão: False).");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        MessageEmbed embed;
        FileUpload upload;
        Member issuer = ctx.getIssuer();
        User user = ctx.getUser();
        Guild guild = ctx.getGuild();
        boolean fromGuild = ctx.getOption("local", false, OptionMapping::getAsBoolean);
        boolean targetProvided = ctx.hasOption("user");

        if (fromGuild) {
            Member targetInput = ctx.getOption("user", OptionMapping::getAsMember);
            Member target = targetProvided ? targetInput : issuer;

            if (target == null)
                return Status.MEMBER_NOT_IN_GUILD;

            ImageProxy avatar = target.getAvatar();

            if (avatar == null)
                return Status.NO_GUILD_AVATAR_PRESENT;

            upload = FileUpload.fromData(downloadAvatar(avatar), target.getId() + ".png");

            embed = embed(upload, guild, target.getUser());
        } else {
            User target = ctx.getOption("user", user, OptionMapping::getAsUser);
            ImageProxy avatar = target.getEffectiveAvatar();
            upload = FileUpload.fromData(downloadAvatar(avatar), target.getId() + ".png");

            embed = embed(upload, guild, target);
        }

        // We do not use a try with resources because JDA's Requester closes
        // the stream automatically when it's sent.
        ctx.reply()
                .setEmbeds(embed)
                .setFiles(upload)
                .send();

        return Status.PASSED;
    }

    private InputStream downloadAvatar(ImageProxy img) {

        try {
            return img.download(2048).get();
        } catch (InterruptedException | ExecutionException e) {

            LOGGER.warn("Could not download avatar at \"{}\"", img.getUrl(), e);

            // I mean... what else can we do about it?
            return InputStream.nullInputStream();
        }
    }

    private MessageEmbed embed(FileUpload file, Guild guild, User target) {

        EmbedBuilder builder = new EmbedBuilder();
        String name = target.getName();
        String title = "🖼 " + name;
        Color color = Bot.Colors.DISCORD;

        return builder
                .setTitle(title, target.getEffectiveAvatarUrl())
                .setDescription(String.format("Avatar de `%s`", target.getEffectiveName()))
                .setColor(color)
                .setImage("attachment://" + file.getName())
                .setFooter(guild.getName(), guild.getIconUrl())
                .build();
    }
}