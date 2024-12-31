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
        return pageIndex < getLastPageIndex();
    }
}