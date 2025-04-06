package ofc.bot.handlers.games.uno;

import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.games.*;
import ofc.bot.util.Bot;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UnoGame implements Game {
    // General Cards
    public static final int DEFAULT_START_CARDS = 7; // The amount of cards given to each player initially
    public static final int NUMBER_CARDS = 2;        // 2 numbers for each card color

    // Action Cards
    public static final int SKIP_CARDS = 2;     // 2 skip cards per color
    public static final int DRAW_TWO_CARDS = 2; // 2 draw two cards per color
    public static final int REVERSE_CARDS = 2;  // 2 reverse cards per color

    // Wild Cards
    public static final int WILD_CARDS = 4;
    public static final int WILD_DRAW_FOUR_CARDS = 4;

    // Game-related
    public static final int MIN_USERS = 2;
    public static final int MAX_USERS = 10;
    public static final int TIMEOUT = 60 * 1000 * 2; // 2 minutes
    public static final int MIN_PRIZE = 800;
    public static final int MAX_PRIZE = 50_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(UnoGame.class);
    private final Map<Long, List<Card>> players;

    /**
     * Represents the draw pile in the Uno game, where players can "buy" or draw more cards
     * if they do not have the necessary card in their hand to play.
     * <p>
     * This pile is replenished when cards are drawn from it,
     * ensuring that players always have a way to continue playing if they are unable to do so from their hand.
     */
    private final CardPile drawPile;

    /**
     * Represents the discard pile in the Uno game, which stores the cards that players have
     * played during the course. The most recent card played can always be found
     * here, and it serves as a reference for game rules such as matching the top card for the next move.
     * <p>
     * The discard pile can also be refreshed and cleared back into the {@link #drawPile} using the
     * {@link #refreshDrawPile()} method, ensuring that the game continues smoothly even when the draw pile is exhausted.
     */
    private final CardPile discardPile;

    private final int prize;
    private GameStatus status;
    private long startedAt;

    public UnoGame(int prize) {
        if (prize != 0 && (prize < MIN_PRIZE || prize > MAX_PRIZE))
            failPrize(prize);

        this.players = new HashMap<>();
        this.drawPile = new CardPile();
        this.discardPile = new CardPile();
        this.status = GameStatus.WAITING;
        this.prize = prize;
    }

    @Override
    public void start(GameArgs args) {
        if (players.size() < MIN_USERS || players.size() > MAX_USERS)
            failUserCount();

        populateDrawPile();
        this.status = GameStatus.RUNNING;
        this.startedAt = Bot.unixNow();

        grantCards();
    }

    @Override
    public void end(GameArgs args) {

    }

    @Override
    public void timeout() {

    }

    @Override
    public int getTimeout() {
        return TIMEOUT;
    }

    @Override
    public void join(long... userIds) {
        for (long userId : userIds) {
            this.players.put(userId, new ArrayList<>());
        }
    }

    @Override
    public GameType getType() {
        return GameType.UNO;
    }

    @Override
    public GameStatus getStatus() {
        return this.status;
    }

    @Override
    public long getTimeStarted() {
        return this.startedAt;
    }

    @Override
    public Set<Long> getParticipants() {
        return UnmodifiableSet.unmodifiableSet(this.players.keySet());
    }

    @Override
    public Set<Long> getWinners() {
        return Set.of();
    }

    @Override
    public int getMaxUsers() {
        return MAX_USERS;
    }

    @Override
    public int getMinUsers() {
        return MIN_USERS;
    }

    public int getPrize() {
        return this.prize;
    }

    private void addCard(long userId, @NotNull Card card) {
        Checks.notNull(card, "Card");

        List<Card> playerCards = this.players.get(userId);
        if (playerCards == null)
            throw new UnsupportedOperationException("User " + userId + " is not in this game");

        // No need to update the map, as this change will already reflect on the Map
        playerCards.add(card);
    }

    private void takeCard(long userId, Card card) {
        List<Card> playerCards = this.players.get(userId);
        if (playerCards == null)
            throw new UnsupportedOperationException("User " + userId + " is not in this game");

        // No need to update the map, as this change will already reflect on the Map
        playerCards.remove(card);
    }

    /**
     * Gives each user {@value DEFAULT_START_CARDS} random initial cards.
     */
    private void grantCards() {
        players.forEach((id, cards) -> {
            List<Card> initialCards = this.drawPile.get(DEFAULT_START_CARDS);
            cards.addAll(initialCards);
        });
    }

    /**
     * Populates the {@code drawPile} field.
     * <p>
     * This is a no-op method if the Stack is not empty.
     */
    private void populateDrawPile() {
        if (!this.drawPile.isEmpty()) return;

        LOGGER.info("Populating Draw Pile...");
        for (Card.Color color : Card.Color.nonWild()) {
            // Generating number cards
            for (int i = Card.MIN_VALUE; i <= Card.MAX_VALUE; i++) {
                List<Card> numCards = Card.generate(NUMBER_CARDS, Card.Type.NUMBER, color, i);
                this.drawPile.addAll(numCards);
            }

            // Generating Action cards
            List<Card> skipCards = Card.generate(SKIP_CARDS, Card.Type.SKIP, color);

            // Generating Draw Two cards
            List<Card> drawTwoCards = Card.generate(DRAW_TWO_CARDS, Card.Type.DRAW_TWO, color);

            // Generating Reverse Cards
            List<Card> reverseCards = Card.generate(REVERSE_CARDS, Card.Type.REVERSE, color);

            // Generating Wild Cards
            List<Card> wildCards = Card.generate(WILD_CARDS, Card.Type.WILD, color);

            // Generating Wil Draw Four Cards
            List<Card> wildDrawFourCards = Card.generate(WILD_DRAW_FOUR_CARDS, Card.Type.WILD_DRAW_FOUR, color);

            // Adding to the pile
            this.drawPile.addAll(skipCards);
            this.drawPile.addAll(drawTwoCards);
            this.drawPile.addAll(reverseCards);
            this.drawPile.addAll(wildCards);
            this.drawPile.addAll(wildDrawFourCards);
        }
        LOGGER.info("Successfully populated Draw Pile with {} cards", this.drawPile.size());

        // Now we shuffle the whole deck :)
        this.drawPile.shuffle();
    }

    /**
     * Sends all the cards (but the last) from the {@code discardPile} to the {@code drawPile}.
     */
    private void refreshDrawPile() {
        if (!this.drawPile.isEmpty()) return;

        // At this point, if this ever throws an EmptyStackException, then its a good
        // way to prove afterlife actually exists
        Card lastDrawnCard = this.discardPile.pop();

        swapPiles(this.discardPile, this.drawPile);
        this.discardPile.addCard(lastDrawnCard);

        this.drawPile.shuffle();
    }

    private void swapPiles(CardPile a, CardPile b) {
        Checks.notNull(a, "CardPile A");
        Checks.notNull(b, "CardPile B");

        Stack<Card> tmp = a.getStack();

        a.set(b.getStack());
        b.set(tmp);
    }

    @Contract("_ -> fail")
    private void failPrize(int amount) {
        throw new IllegalArgumentException(String.format(
                "Uno prize can only be either 0 (for no bet), or range from %d - %d, but %d was provided",
                MIN_PRIZE, MAX_PRIZE, amount));
    }

    @Contract("-> fail")
    private void failUserCount() {
        throw new IllegalStateException(String.format(
                "The amount of users for Uno ranges from %d - %d, but %d was provided",
                MIN_USERS, MAX_USERS, this.players.size()));
    }
}