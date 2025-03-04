package ofc.bot.handlers.games.betting;

public enum BetType {
    TIC_TAC_TOE(false),
    ROULETTE(   true);

    private final boolean isMultiWiner;

    BetType(boolean isMultiWiner) {
        this.isMultiWiner = isMultiWiner;
    }

    public boolean isMultiWiner() {
        return this.isMultiWiner;
    }
}