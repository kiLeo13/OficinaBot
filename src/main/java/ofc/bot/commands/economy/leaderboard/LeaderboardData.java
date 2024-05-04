package ofc.bot.commands.economy.leaderboard;

import ofc.bot.databases.entities.records.EconomyRecord;

import java.util.List;

public record LeaderboardData(
        List<EconomyRecord> usersData,
        int page,
        int maxPages
) {
    public boolean isEmpty() {
        return this.usersData.isEmpty();
    }
}