package ofc.bot.commands.economy.balance;

import ofc.bot.util.Bot;

public record BalanceData(
        int rank,
        long balance,
        long createdAt,
        long updatedAt,
        long workedAt,
        long lastDailyAt,
        boolean found
) {
    public String prettyRank() {
        return rank <= 0 ? "*Sem rank*" : "#" + rank;
    }

    public String prettyBalance() {
        return Bot.strfNumber(balance);
    }

    public String prettyCreation() {
        return createdAt == 0
                ? "Nunca"
                : formatTimestamp(createdAt);
    }

    public String prettyUpdated() {
        return updatedAt == 0 || updatedAt == createdAt
                ? "Nunca"
                : formatTimestamp(updatedAt);
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