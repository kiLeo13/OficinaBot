package ofc.bot.handlers.games.betting;

public enum BetType {
    TIC_TAC_TOE(false, true),
    ROULETTE(   true,  false);

    private final boolean isMultiWiner;
    private final boolean isRandomizable;

    BetType(boolean isMultiWiner, boolean isRandomizable) {
        this.isMultiWiner = isMultiWiner;
        this.isRandomizable = isRandomizable;
    }

    public boolean isMultiWiner() {
        return this.isMultiWiner;
    }

    public boolean isRandomizable() {
        return this.isRandomizable;
    }
}