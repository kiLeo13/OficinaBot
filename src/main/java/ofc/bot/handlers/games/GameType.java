package ofc.bot.handlers.games;

public enum GameType {
    TIC_TAC_TOE(false, true,  true),
    ROULETTE(   true,  false, true);

    private final boolean isMultiWiner;
    private final boolean isRandomizable;
    private final boolean isBet;

    GameType(boolean isMultiWiner, boolean isRandomizable, boolean isBet) {
        this.isMultiWiner = isMultiWiner;
        this.isRandomizable = isRandomizable;
        this.isBet = isBet;
    }

    public boolean isMultiWiner() {
        return this.isMultiWiner;
    }

    public boolean isRandomizable() {
        return this.isRandomizable;
    }

    public boolean isBet() {
        return this.isBet;
    }
}