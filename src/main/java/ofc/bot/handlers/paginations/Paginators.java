package ofc.bot.handlers.paginations;

import ofc.bot.domain.entity.MemberPunishment;
import ofc.bot.domain.sqlite.repository.MemberPunishmentRepository;
import ofc.bot.domain.sqlite.repository.RepositoryFactory;
import ofc.bot.domain.sqlite.repository.UserXPRepository;
import ofc.bot.domain.viewmodels.LevelView;
import ofc.bot.util.Bot;

import java.util.List;

public final class Paginators {
    private Paginators() {}

    public static PaginationItem<MemberPunishment> viewInfractions(long userId, long guildId, int pageSize, int pageIndex) {
        MemberPunishmentRepository pnshRepo = RepositoryFactory.getMemberPunishmentRepository();
        int offset = pageIndex * pageSize;
        int rowCount = pnshRepo.countByUserAndGuildId(userId, guildId);
        int maxPages = Bot.calcMaxPages(rowCount, pageSize);
        List<MemberPunishment> punishments = pnshRepo.findByUserAndGuildId(userId, guildId, pageSize, offset);

        return new PaginationItem<>(
                punishments,
                pageIndex,
                offset,
                maxPages,
                rowCount
        );
    }

    public static PaginationItem<LevelView> viewLevels(int pageSize, int pageIndex) {
        UserXPRepository xpRepo = RepositoryFactory.getUserXPRepository();
        int offset = pageIndex * pageSize;
        int rowCount = xpRepo.countAll();
        int maxPages = Bot.calcMaxPages(rowCount, pageSize);
        List<LevelView> levels = xpRepo.viewLevels(offset, pageSize);

        return new PaginationItem<>(
                levels,
                pageIndex,
                offset,
                maxPages,
                rowCount
        );
    }
}