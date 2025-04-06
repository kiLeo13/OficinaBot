package ofc.bot.handlers.games.uno;

import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 */
public class Card {
    public static final int MIN_VALUE = 1;
    public static final int MAX_VALUE = 9;

    private final Type type;
    private final Color color;
    private final int number;

    public Card(@NotNull Type type, @NotNull Color color, int number) {
        Checks.notNull(type, "CardType");
        Checks.notNull(color, "Color");

        if (type == Type.NUMBER && (number < MIN_VALUE || number > MAX_VALUE))
            throw new IllegalArgumentException("Number must be between 0 and 9, provided: " + number);

        if (type != Type.NUMBER && number >= MIN_VALUE)
            throw new IllegalArgumentException("Only number cards can have a number value, card type provided: " + type);

        if (color == Color.WILD && !type.isWild())
            throw new IllegalArgumentException(
                    "The provided color is Color.WILD but the CardType is " + type + " (which is not of a wild type)");

        this.type = type;
        this.color = color;
        this.number = number;
    }

    public Card(@NotNull Type type, @NotNull Color color) {
        this(type, color, -1);
    }

    public static List<Card> generate(int count, @NotNull Type type, @NotNull Color color, int number) {
        List<Card> cards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cards.add(new Card(type, color, number));
        }
        return cards;
    }

    public static List<Card> generate(int count, Type type, @NotNull Color color) {
        return generate(count, type, color, -1);
    }

    public Type getType() {
        return this.type;
    }

    public Color getColor() {
        return this.color;
    }

    /**
     * Checks if the specified {@code next} card can be played immediately after {@code this} card,
     * following <a href="https://www.unorules.com/">UNO's rules</a>.
     * <p>
     * A card can be played after another if they share the same color or number.
     * <p>
     * <b>Note:</b> This method will always return {@code true} if either:
     * <ul>
     *   <li>The {@code next} card's {@link Type} is a wild card.</li>
     *   <li>The {@code next} is the same instance as {@code this}.</li>
     * </ul>
     *
     * @param next The {@link Card} that would be played after this one.
     * @return {@code true} if the {@code next} card can be played after this card,
     *         {@code false} otherwise.
     * @throws IllegalArgumentException If {@code next} is {@code null}.
     */
    public boolean canProceed(@NotNull Card next) {
        Checks.notNull(next, "Card Next");

        if (next.isWild() || equals(next)) return true;

        return this.color == next.color && this.number == next.number;
    }

    /**
     * Gets the number of this Uno Card, ranging from {@value #MIN_VALUE} to {@value #MAX_VALUE}.
     * <p>
     * This method will return a negative value (usually {@code -1})
     * if the {@link Type} is not of type {@link Type#NUMBER}.
     *
     * @return The number on this card.
     */
    public int getNumber() {
        return this.number;
    }

    /**
     * Checks if the card supports a number on it.
     * <p>
     * Shortcut for:
     * <pre>
     *   {@code
     * // Though our deck does not currently support "0",
     * // we can account on it for a future.
     * boolean isNumeric = card.getNumber() >= 0;
     *   }
     * </pre>
     *
     * @return {@code true} if this card supports numbers on it, {@code false} otherwise.
     */
    public boolean isNumeric() {
        return this.getNumber() >= 0;
    }

    /**
     * A shortcut for {@link Type#isWild() Card.getType().isWild()}.
     *
     * @return {@code true} if this is a wild card, {@code false} otherwise.
     */
    public boolean isWild() {
        return this.type.isWild();
    }

    /**
     * Just a shortcut for this {@link #toString()} method call.
     *
     * @return This card as a {@code String}.
     */
    @NotNull
    public String asText() {
        return this.toString();
    }

    @Override
    public String toString() {
        // Examples:
        // RED_1
        // GREEN_5
        // RED_SKIP
        // YELLOW_REVERSE
        // BLUE_DRAW_TWO
        // WILD
        // WILD_DRAW_FOUR
        return switch (this.type) {
            case NUMBER -> String.format("%s_%d", this.color.name(), this.number);
            case SKIP,
                 DRAW_TWO,
                 REVERSE -> String.format("%s_%s", this.color.name(), this.type.name());

            default -> this.type.name();
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        return (obj instanceof Card other) &&
                this.color == other.color &&
                this.type == other.type &&
                this.number == other.number;
    }

    public enum Color {
        RED(212 << 16),
        BLUE((42 << 16) | (127 << 8) | 255),
        GREEN((44 << 16) | (160 << 8) | 90),
        YELLOW((255 << 16) | (204 << 8)),
        WILD;

        private final int rgb;

        Color(int rgb) {
            Checks.notNegative(rgb, "RGB");
            this.rgb = rgb;
        }

        Color() {
            this(0);
        }

        /**
         * Gets the RGB value of this card color.
         * <p>
         * You can also use this method to get a Java {@link java.awt.Color Color} instance:
         * <pre>
         *   {@code
         * Card card = new Card(Card.Type.SKIP, Card.Color.BLUE);
         * Color cardColor = new Color(card.getRGB()); // Creates a Java Color instance from this RGB value
         *   }
         * </pre>
         * <p>
         * <b>Note:</b> Alpha values are not supported and thus not returned by this method.
         *
         * @return The RGB color of this constant, or {@code 0} if {@code this == Card.Color.WILD}.
         * @see Type#supportsColor()
         */
        public int getRGB() {
            return this.rgb;
        }

        public static List<Color> nonWild() {
            return Stream.of(values())
                    .filter(e -> e != WILD)
                    .toList();
        }
    }

    public enum Type {
        NUMBER,
        SKIP,
        REVERSE,
        DRAW_TWO,
        WILD,
        WILD_DRAW_FOUR;

        public boolean isWild() {
            return this == WILD || this == WILD_DRAW_FOUR;
        }

        public boolean supportsColor() {
            return !isWild();
        }
    }
}