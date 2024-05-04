package ofc.bot.commands.userinfo;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.User.Profile;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.CustomUserinfoRecord;
import ofc.bot.databases.entities.records.MarriageRecord;
import ofc.bot.databases.entities.tables.Economy;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.EconomyUtil;
import ofc.bot.util.MarriageUtil;
import org.jooq.DSLContext;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;

import static ofc.bot.databases.entities.tables.CustomUserinfo.CUSTOM_USERINFO;
import static ofc.bot.databases.entities.tables.Marriages.MARRIAGES;
import static ofc.bot.databases.entities.tables.Users.USERS;

@DiscordCommand(name = "userinfo", description = "Comando usado para saber informaçõe gerais do membro no servidor e do usuário do Discord.")
public class UserinfoCommand extends SlashCommand {
    public static final int MAX_MARRIAGE_DISPLAY = 20;

    @Option
    private static final OptionData MEMBER = new OptionData(OptionType.USER, "member", "O usuário a verificar as informações.");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        boolean hasMember = ctx.hasOption("member");
        Member issuer = ctx.getIssuer();
        Member member = ctx.getOption("member", OptionMapping::getAsMember);
        Member target = member == null ? issuer : member;

        // Received a User instead of a Member, which means
        // the provided user does not exist in the current guild
        if (hasMember && member == null)
            return Status.MEMBER_NOT_IN_GUILD;

        target.getUser().retrieveProfile().queue((profile -> {

            long userId = target.getIdLong();
            CustomUserinfoRecord userinfo = retrieveUserinfo(userId);
            MessageEmbed embed = embed(userinfo, target, profile);

            ctx.replyEmbeds(embed);
        }));

        return Status.PASSED;
    }

    private MessageEmbed embed(CustomUserinfoRecord userinfo, Member target, Profile profile) {

        EmbedBuilder builder = new EmbedBuilder();

        OffsetDateTime timeBoosted = target.getTimeBoosted();
        long boosterSince = timeBoosted == null
                ? 0
                : timeBoosted.toEpochSecond();
        long userId = target.getIdLong();
        long creation = target.getUser().getTimeCreated().toEpochSecond();
        long joined = target.getTimeJoined().toEpochSecond();
        long balance = EconomyUtil.fetchBalance(userId);
        Guild guild = target.getGuild();
        User user = target.getUser();
        Color color = userinfo.getEffectiveColor(target);
        String title = userinfo.getEffectiveTitle(user);
        String description = userinfo.getEffectiveDescription(target);
        String banner = profile.getBannerUrl();
        String footer = userinfo.getEffectiveFooter(guild);
        String resizedBanner = banner == null
                ? null
                : banner + "?size=2048";

        builder
                .setTitle(title)
                .setDescription(description)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setColor(color)
                .addField("📅 Criação da Conta", String.format("<t:%d>\n<t:%1$d:R>", creation), true)
                .addField("🌐 User ID", "`" + target.getIdLong() + "`", true)
                .addField("🌟 Entrou no Servidor", String.format("<t:%d>", joined), true)
                .addField(Economy.SYMBOL + " Saldo", "$" + Bot.strfNumber(balance), true)
                .setFooter(footer, guild.getIconUrl());

        if (banner != null)
            builder.setImage(resizedBanner);

        if (boosterSince != 0)
            builder.addField("<:discordbooster:1094816233234378762> Booster Desde", "<t:" + boosterSince + ">", true);

        includeMarriagesIfPresent(builder, userId);

        return builder.build();
    }

    private void includeMarriagesIfPresent(EmbedBuilder builder, long userId) {

        boolean isMarried = MarriageUtil.isPartnered(userId);

        if (!isMarried)
            return;

        int marriageCount = MarriageUtil.getMarriageCount(userId);
        String strfCount = Bot.strfNumber(marriageCount);
        List<MarriageRecord> marriages = retrieveMarriages(userId);
        String formattedMarriages = MarriageUtil.format(marriages);

        builder.addField("💍 Casamentos (" + strfCount + ")", formattedMarriages, false);
    }

    private List<MarriageRecord> retrieveMarriages(long userId) {

        DSLContext ctx = DBManager.getContext();

        return ctx.select(MARRIAGES.REQUESTER_ID, MARRIAGES.TARGET_ID, MARRIAGES.CREATED_AT, USERS.NAME, USERS.GLOBAL_NAME)
                .from(MARRIAGES)
                .join(USERS)
                .on(MARRIAGES.REQUESTER_ID.eq(USERS.ID).or(MARRIAGES.TARGET_ID.eq(USERS.ID)))
                .where(MARRIAGES.REQUESTER_ID.eq(userId).or(MARRIAGES.TARGET_ID.eq(userId))
                        .and(USERS.ID.ne(userId)))
                .groupBy(USERS.ID)
                .orderBy(MARRIAGES.CREATED_AT)
                .limit(MAX_MARRIAGE_DISPLAY)
                .fetchInto(MARRIAGES);
    }

    private CustomUserinfoRecord retrieveUserinfo(long userId) {

        DSLContext ctx = DBManager.getContext();

        CustomUserinfoRecord customUserinfo = ctx.selectFrom(CUSTOM_USERINFO)
                .where(CUSTOM_USERINFO.USER_ID.eq(userId))
                .fetchOne();

        return customUserinfo == null
                ? new CustomUserinfoRecord(userId)
                : customUserinfo;
    }
}