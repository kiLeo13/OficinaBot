package ofc.bot.domain.viewmodels;

import ofc.bot.domain.entity.UserEconomy;
import ofc.bot.util.Bot;

public record BalanceView(
        int rank,
        long balance,
        long createdAt,
        long updatedAt,
        long workedAt,
        long lastDailyAt,
        boolean found
) {
    public static BalanceView of(UserEconomy user, int rank) {
        return new BalanceView(
                rank, user.getBalance(), user.getTimeCreated(), user.getLastUpdated(),
                user.getLastWorkAt(), user.getLastDailyAt(), true
        );
    }

    public String prettyRank() {
        return rank == 0 ? "*Sem rank*" : "#" + rank;
    }

    public String prettyBalance() {
        return Bot.fmtNum(balance);
    }

    public String prettyCreation() {
        return createdAt == 0
                ? "Nunca"
                : formatTimestamp(createdAt);
    }

    public String prettyLastWork() {
        return workedAt == 0
                ? "Nunca"
                : formatTimestamp(workedAt);
    }

    public String prettyLastDaily() {
        return lastDailyAt == 0
                ? "Nunca"
                : formatTimestamp(lastDailyAt);
    }

    private String formatTimestamp(long value) {
        return String.format("<t:%d>", value);
    }
}