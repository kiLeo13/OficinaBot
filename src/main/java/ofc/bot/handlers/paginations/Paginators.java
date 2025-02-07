package ofc.bot.handlers.paginations;

import ofc.bot.commands.levels.LevelsCommand;
import ofc.bot.domain.entity.MemberPunishment;
import ofc.bot.domain.sqlite.repository.MemberPunishmentRepository;
import ofc.bot.domain.sqlite.repository.RepositoryFactory;
import ofc.bot.domain.sqlite.repository.UserXPRepository;
import ofc.bot.domain.viewmodels.LevelView;
import ofc.bot.util.Bot;

import java.util.List;

public final class Paginators {
    public static final int PAGE_SIZE = 10;
    public static final int INFRACTIONS_PAGE_SIZE = 1;
    private Paginators() {}

    public static PaginationItem<MemberPunishment> viewInfractions(long userId, long guildId, int pageIndex) {
        MemberPunishmentRepository pnshRepo = RepositoryFactory.getMemberPunishmentRepository();
        int offset = pageIndex * INFRACTIONS_PAGE_SIZE;
        int rowCount = pnshRepo.countByUserAndGuildId(userId, guildId);
        int maxPages = Bot.calcMaxPages(rowCount, INFRACTIONS_PAGE_SIZE);
        List<MemberPunishment> punishments = pnshRepo.findByUserAndGuildId(userId, guildId, INFRACTIONS_PAGE_SIZE, offset);

        return new PaginationItem<>(
                punishments,
                pageIndex,
                offset,
                maxPages,
                rowCount
        );
    }

    public static PaginationItem<LevelView> viewLevels(int pageIndex) {
        UserXPRepository xpRepo = RepositoryFactory.getUserXPRepository();
        int limit = LevelsCommand.MAX_USERS_PER_PAGE;
        int offset = pageIndex * limit;
        int rowCount = xpRepo.countAll();
        int maxPages = Bot.calcMaxPages(rowCount, limit);
        List<LevelView> levels = xpRepo.viewLevels(offset, limit);

        return new PaginationItem<>(
                levels,
                pageIndex,
                offset,
                maxPages,
                rowCount
        );
    }
}