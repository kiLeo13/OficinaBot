package ofc.bot.handlers.games.uno;

import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class CardPile {
    private final Stack<Card> cards;

    public CardPile(@NotNull CardPile copy) {
        Checks.notNull(copy, "CardPile copy");

        this.cards = new Stack<>();
        this.cards.addAll(copy.cards);
    }

    public CardPile() {
        this.cards = new Stack<>();
    }

    public void forEach(Consumer<Card> action) {
        this.cards.forEach(action);
    }

    public void addCard(@NotNull Card card) {
        Checks.notNull(card, "Card");
        this.cards.push(card);
    }

    public void addAll(@NotNull List<Card> cards) {
        Checks.notNull(cards, "Cards");
        this.cards.addAll(cards);
    }

    public void set(@NotNull List<Card> cards) {
        Checks.notNull(cards, "Cards");
        this.cards.clear();
        this.cards.addAll(cards);
    }

    public void clear() {
        this.cards.clear();
    }

    /**
     * Similar to {@link Stack#pop()}, but instead of throwing
     * an exception if no elements are found, {@code null} is returned.
     *
     * @return The last {@link Card} added to the pile.
     */
    @Nullable
    public Card pop() {
        return isEmpty() ? null : this.cards.pop();
    }

    /**
     * Similar to {@link Stack#peek()}, but instead of throwing
     * an exception if no elements are found, {@code null} is returned.
     *
     * @return The last {@link Card} added to the pile.
     */
    @Nullable
    public Card peek() {
        return isEmpty() ? null : this.cards.peek();
    }

    @NotNull
    public List<Card> get(int amount) {
        if (amount > size())
            throw new IllegalArgumentException(
                    "The amount of requested cards is greater than the entire amount of cards in this pile, " +
                    amount + " was provided, but we have only " + size());

        List<Card> result = new ArrayList<>(amount);

        for (int i = 0; i < amount; i++) {
            result.add(this.cards.pop());
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns a <b><u>MUTABLE</u></b> {@link Stack}
     * underlying this {@code CardPile}.
     * <p>
     * <b>Note:</b> Be careful! Modifications on this stack will be reflected on this instance.
     *
     * @return The underlying {@link Stack Stack&lt;Card&gt;} this instance.
     */
    @NotNull
    public final Stack<Card> getStack() {
        return this.cards;
    }

    public int size() {
        return this.cards.size();
    }

    public boolean isEmpty() {
        return this.cards.isEmpty();
    }

    public void shuffle() {
        Collections.shuffle(this.cards);
    }
}