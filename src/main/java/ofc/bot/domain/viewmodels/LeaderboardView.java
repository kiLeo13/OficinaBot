package ofc.bot.domain.viewmodels;

import java.util.List;

public record LeaderboardView(
        List<LeaderboardUser> usersData,
        int pageIndex,
        int maxPages
) {
    public boolean isEmpty() {
        return this.usersData.isEmpty();
    }

    public int getLastPageIndex() {
        return this.maxPages - 1;
    }

    public boolean hasMorePages() {
        System.out.printf("Max pages %d, Last page index: %d, page index: %d\n", maxPages, getLastPageIndex(), pageIndex);
        return pageIndex < getLastPageIndex();
    }
}