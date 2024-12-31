package ofc.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import ofc.bot.domain.sqlite.DB;
import ofc.bot.domain.sqlite.repository.DiscordMessageRepository;
import ofc.bot.domain.sqlite.repository.RepositoryFactory;
import ofc.bot.handlers.requests.Route;
import ofc.bot.internal.data.BotData;
import ofc.bot.internal.data.BotFiles;
import ofc.bot.util.Bot;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Testing {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = LoggerFactory.getLogger(Testing.class);

    public static void main(String[] args) {
        try {
            BotFiles.loadFiles();
            DB.init();

            JDA api = JDABuilder.createDefault(BotData.get("app.testing"),
                            GatewayIntent.GUILD_MEMBERS
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .build()
                    .awaitReady();

            Guild guild = api.getGuildById("582430782577049600");

            if (guild != null && guild.isLoaded())
                LOGGER.info("Guild \"{}\" is loaded", guild.getName());

            EXECUTOR.execute(() -> {
                LOGGER.info("Fetching leaderboard...");
                Map<Player, String> players = fetchTopUsers(guild);
                LOGGER.info("Result: {} users should be awarded with the \"\uD83C\uDFC6 TOP 100 5ª TEMPORADA 2024\" role", players.size());

                if (players.size() > 100)
                    LOGGER.warn("We have exceeded the limit of 100 members in the leaderboard, having {} more users than the expected", players.size() - 100);

                LOGGER.info("");
                LOGGER.info("==========================================");
                LOGGER.info("Listing the Selected Ones...");
                LOGGER.info("==========================================");
                LOGGER.info("");

                List<String> rows = getOrderedRows(players);

                rows.forEach(LOGGER::info);
            });

        } catch (DataAccessException e) {
            LOGGER.error("Could not perform database operation", e);
        } catch (IOException e) {
            LOGGER.error("Could not load bot files", e);
        } catch (InterruptedException e) {
            LOGGER.error("Failed to login", e);
        }
    }

    private static Map<Player, String> fetchTopUsers(Guild guild) {
        List<Player> players = new ArrayList<>();

        populate(guild, players);

        if (players.size() > 100) {
            LOGGER.warn("We have exceeded the limit (100) players, currently: {}, limiting to 100...", players.size());
            players = new ArrayList<>(players.stream().limit(100).toList());
        }

        Map<Player, String> topUsers = new HashMap<>();
        players.forEach(p -> topUsers.put(p, "Fetched from leaderboard (level " + Bot.fmtNum(p.level) + ")"));

        Map<Player, String> additionals = fetchAdditionals(guild);
        additionals.forEach(topUsers::putIfAbsent);

        return topUsers;
    }

    private static void populate(Guild guild, List<Player> playerList) {
        Mee6Client mee6 = new Mee6Client();
        String guildId = guild.getId();
        int page = 0;

        while (playerList.size() < 100) {
            List<Player> foundPlayers = mee6.fetchLeaderboard(guildId, page++);
            LOGGER.info("Successfully fetched {} players from leaderboard, page {}", foundPlayers.size(), page - 1);
            List<Player> ignoredPlayers = new ArrayList<>();

            for (Player player : foundPlayers) {
                if (!isMember(guild, player)) {
                    ignoredPlayers.add(player);
                    LOGGER.warn("Ignoring user \"{}\", they are no longer in the server", player.getUsername());
                }
            }

            foundPlayers.removeAll(ignoredPlayers);
            playerList.addAll(foundPlayers);

            if (playerList.size() < 100)
                LOGGER.info("We don't have enough (100) users! Currently: {}, fetching additional ones...", playerList.size());
        }
    }

    private static Map<Player, String> fetchAdditionals(Guild guild) {
        LOGGER.info("Fetching users who sent 1 or more messages in the last 30 days...");
        List<Long> activeUsers = fetchRecentlyActiveUsersIds();
        Map<Player, String> additionals = new HashMap<>();
        LOGGER.info("Found {} users", activeUsers.size());

        for (long userId : activeUsers) {
            Member member = guild.getMemberById(userId);

            if (member != null && member.getRoles().stream().anyMatch(r -> r.getIdLong() == 693973471989858714L)) {
                additionals.put(new Player(member.getId(), member.getUser().getName(), Integer.MIN_VALUE), "Member of Salada");
            }
        }

        LOGGER.info("Only {} users had the \"\uD83C\uDF52ㅤㅤ⠀\uD835\uDC46\uD835\uDC4E\uD835\uDC59\uD835\uDC4E\uD835\uDC51\uD835\uDC4E \uD835\uDC51\uD835\uDC52 \uD835\uDC39\uD835\uDC5F\uD835\uDC62\uD835\uDC61\uD835\uDC4Eㅤㅤ⠀\uD83C\uDF52\" role", additionals.size());
        return additionals;
    }

    private static List<String> getOrderedRows(Map<Player, String> players) {
        List<String> rows = new ArrayList<>();
        List<Player> orederedPlayers = players.keySet()
                .stream()
                .sorted(Comparator.comparing((Player p) -> p.level).reversed())
                .toList();

        for (Player player : orederedPlayers) {
            String reason = players.get(player);
            rows.add(String.format("%s is eligible for reason: %s", player.getUsername(), reason));
        }

        return rows;
    }

    private static List<Long> fetchRecentlyActiveUsersIds() {
        DiscordMessageRepository msgRepo = RepositoryFactory.getDiscordMessageRepository();
        return msgRepo.fetchRecentlyActiveUserlist(30, TimeUnit.DAYS);
    }

    private static boolean isMember(Guild guild, Player player) {
        return guild.isMember(UserSnowflake.fromId(player.getId()));
    }

    private static class Mee6Client {
        List<Player> fetchLeaderboard(String guildId, int page) {
            Leaderboard leaderboard = Route.MEE6.GET_LEADERBOARD
                    .create(guildId)
                    .addQueryParam("page", page)
                    .addHeader("Referer", "https://mee6.xyz/en/oficinacarr")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .addHeader("Accept", "application/json")
                    .addHeader("Accept-Language", "en-US,en;q=0.8")
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Pragma", "no-cache")
                    .send()
                    .json(Leaderboard.class);

            return new ArrayList<>(leaderboard.players);
        }
    }

    private record Leaderboard(
            List<Player> players
    ) {}

    private record Player(
            String id,
            String username,
            int level
    ) {
        String getId() {
            return id;
        }

        String getUsername() {
            return username;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Player pl) {
                return pl.id.equals(id);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}