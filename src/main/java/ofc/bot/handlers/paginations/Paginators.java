package ofc.bot.handlers.paginations;

import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.MemberPunishment;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.*;
import ofc.bot.domain.viewmodels.LevelView;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.util.Bot;

import java.util.List;

public final class Paginators {
    private Paginators() {}

    public static PaginationItem<BankTransaction> viewTransactions(long userId, int pageIndex, int pageSize,
                                                                   List<CurrencyType> currencies,
                                                                   List<TransactionType> types) {
        BankTransactionRepository bankTrRepo = RepositoryFactory.getBankTransactionRepository();
        int offset = pageIndex * pageSize;
        int rowCount = bankTrRepo.countUserTransactions(userId, currencies, types);
        int maxPages = Bot.calcMaxPages(rowCount, pageSize);
        List<BankTransaction> transactions = bankTrRepo.findUserTransactions(userId, currencies, types, pageSize, offset);

        return new PaginationItem<>(
                transactions,
                pageIndex,
                offset,
                maxPages,
                rowCount
        );
    }

    public static PaginationItem<MemberPunishment> viewInfractions(long targetId, long guildId,
                                                                   int pageSize, int pageIndex, boolean showInactive) {
        MemberPunishmentRepository pnshRepo = RepositoryFactory.getMemberPunishmentRepository();
        int offset = pageIndex * pageSize;
        int rowCount = pnshRepo.countByUserAndGuildId(targetId, guildId);
        int maxPages = Bot.calcMaxPages(rowCount, pageSize);
        List<MemberPunishment> punishments = pnshRepo.findByUserAndGuildId(
                targetId, guildId, pageSize, offset, showInactive);

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