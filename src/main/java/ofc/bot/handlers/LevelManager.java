package ofc.bot.handlers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.domain.entity.LevelRole;
import ofc.bot.domain.entity.UserXP;
import ofc.bot.domain.sqlite.repository.LevelRoleRepository;
import ofc.bot.domain.sqlite.repository.RepositoryFactory;
import ofc.bot.domain.sqlite.repository.UserXPRepository;
import ofc.bot.util.content.Channels;
import org.jetbrains.annotations.NotNull;

public final class LevelManager {
    private static LevelManager instance;
    private final UserXPRepository xpRepo;
    private final LevelRoleRepository lvlRoleRepo;

    private LevelManager() {
        this.xpRepo = RepositoryFactory.getUserXPRepository();
        this.lvlRoleRepo = RepositoryFactory.getLevelRoleRepository();
    }

    public static LevelManager getManager() {
        if (instance == null) instance = new LevelManager();
        return instance;
    }

    public synchronized void addXp(@NotNull Member member, int xp) {
        Checks.notNull(member, "Member");
        Checks.positive(xp, "Xp");
        Guild guild = member.getGuild();
        long userId = member.getIdLong();
        UserXP userXp = xpRepo.findByUserId(userId, UserXP.fromUserId(userId));
        int xpMod = userXp.getXp() + xp;
        int oldLevel = userXp.getLevel();

        UserXP.compute(xpMod, oldLevel, (newXp, newLevel) -> userXp.setXp(newXp)
                .setLevel(newLevel)
                .tickUpdate());
        xpRepo.upsert(userXp);

        // If the levels after the patch are not the same (indicating a level up),
        // we check for roles to be given and notify the user in the respective channel.
        int currLevel = userXp.getLevel();
        if (currLevel <= oldLevel) return;

        TextChannel chan = Channels.LEVEL_UP.textChannel();
        if (chan != null) {
            chan.sendMessageFormat("%s avançou para o nível %d!", member.getAsMention(), currLevel).queue();
        }

        // Check if we should give the user a new role for their rank.
        // If the closest role to this given level is not found or does not match
        // the level the user is currently on, we ignore it.
        LevelRole levelRole = lvlRoleRepo.findLastByLevel(currLevel);
        if (levelRole == null || levelRole.getLevel() != currLevel) return;
        guild.addRoleToMember(member, levelRole.toRole()).queue();

        LevelRole oldLvlRole = lvlRoleRepo.findLastByLevel(oldLevel);
        if (oldLvlRole != null) {
            // We could check for all the other available level roles
            // and use "Guild#modifyMemberRoles()" to remove them all at once,
            // but its not necessary, removing the old role is enough.
            guild.removeRoleFromMember(member, oldLvlRole.toRole()).queue();
        }
    }
}