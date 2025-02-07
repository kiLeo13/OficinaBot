package ofc.bot.commands.levels;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.data.DataObject;
import ofc.bot.domain.entity.LevelRole;
import ofc.bot.domain.entity.UserXP;
import ofc.bot.domain.sqlite.repository.LevelRoleRepository;
import ofc.bot.domain.sqlite.repository.UserXPRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.handlers.requests.RequestMapper;
import ofc.bot.handlers.requests.Route;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.Base64;
import java.util.List;

@DiscordCommand(name = "rank", description = "Mostra o rank (global) de um usuário.", cooldown = 10)
public class RankCommand extends SlashCommand {
    private final UserXPRepository xpRepo;
    private final LevelRoleRepository lvlRoleRepo;

    public RankCommand(UserXPRepository xpRepo, LevelRoleRepository lvlRoleRepo) {
        this.xpRepo = xpRepo;
        this.lvlRoleRepo = lvlRoleRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Member issuer = ctx.getIssuer();
        Member target = ctx.getOption("user", issuer, OptionMapping::getAsMember);

        if (target == null)
            return Status.USER_NOT_FOUND;

        long targetId = target.getIdLong();
        User userTarget = target.getUser();
        UserXP userXp = xpRepo.findByUserId(targetId);

        if (userXp == null)
            return Status.USER_DOES_NOT_HAVE_RANK;

        ctx.ack();
        String avatarUrl = userTarget.getAvatarUrl();
        String username = userTarget.getName();
        OnlineStatus online = target.getOnlineStatus();
        int level = userXp.getLevel();
        int userRank = userXp.fetchRank(xpRepo);
        int nextXp = UserXP.calcNextXp(level);
        int currentXp = Math.min(userXp.getXp(), nextXp); // We can never let "currentXp" be greater than "nextXp"
        LevelRole levelRole = lvlRoleRepo.findLastByLevel(level);
        RankData rankData = RankData.create(username, userRank, level, currentXp, nextXp, levelRole, avatarUrl, online);
        byte[] cardImage = getRankCard(rankData);

        if (cardImage.length == 0)
            return Status.COULD_NOT_EXECUTE_SUCH_OPERATION;

        return ctx.replyFile(cardImage, "card.png");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "O membro que você deseja saber o rank global.")
        );
    }

    private static byte[] getRankCard(RankData data) {
        RequestMapper result = Route.Images.CREATE_RANK_CARD.create()
                .setContentType("application/json")
                .setBody(data)
                .send();

        if (result.getStatusCode() != 200)
            return new byte[0];

        DataObject json = result.asDataObject();
        String cardImage = json.getString("image");
        return Base64.getDecoder().decode(cardImage);
    }

    private record RankData(
            String username,
            int rank,
            int level,
            int xp,
            int xp_next,
            int theme_color,
            String avatar_url,
            String online_status
    ) {
        static RankData create(
                String username, int rank, int level, int xp, int xpNext,
                LevelRole lr, String avatarUrl, OnlineStatus status
        ) {
            int color = lr == null ? 0 : lr.getColor();
            int themeColor = color == 0 ? 0xFFFFFF : color;
            return new RankData(username, rank, level, xp, xpNext, themeColor, avatarUrl, getStatus(status));
        }
    }

    private static String getStatus(OnlineStatus status) {
        return switch (status) {
            case ONLINE, IDLE, OFFLINE -> status.name();
            case DO_NOT_DISTURB -> "DND";
            default -> "OFFLINE";
        };
    }
}