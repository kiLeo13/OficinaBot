package ofc.bot.handlers.games.betting;

import java.util.Set;

public interface Bet<S, E> {

    /**
     * Starts the current bet game.
     *
     * @param arg the optional custom argument to be passed to the start command.
     */
    void start(S arg);

    /**
     * Stops this current bet game.
     * <p>
     * This method should (usually) never throw exceptions,
     * but instead, finish the game gracefully (even though it was a "forced" operation).
     *
     * @param arg the optional custom argument to be passed to the stop command.
     */
    void end(E arg);

    /**
     * <b>This method is intended to be called exclusively by the instance itself.</b>
     * <p>
     * It should be invoked when a specified period of user inactivity is detected,
     * as defined by the {@link #getTimeout()} value.
     * <p>
     * The implementor must invoke this method when the user(s) fail to interact with the game
     * for a duration that exceeds the specified timeout period.
     */
    void timeout();

    /**
     * Returns the maximum duration (in milliseconds) of user inactivity before the game times out.
     * <p>
     * Depending on the specific game implementation, this value may either:
     * <ul>
     *   <li>Return a positive integer representing the time limit after which the game should
     *       enforce timeout rules (e.g., end the match), or</li>
     *   <li>Return {@code -1} to indicate that there is no timeout limit, and the game
     *       should run indefinitely unless externally interrupted.</li>
     * </ul>
     * <p>
     * For certain game types, such as roulette or timed challenges, this value may represent
     * a deadline by which a player must take action, such as placing a bet or making a decision.
     *
     * @return The number of milliseconds before the game times out due to inactivity.
     *         If the game runs indefinitely, {@code -1} is returned.
     */
    int getTimeout();

    /**
     * Adds these users to the participants list.
     * <p>
     * Refer to the implementation-specific constraints for more information.
     *
     * @param userIds The ID of the users to participate the game.
     */
    void join(long... userIds);

    BetType getType();

    BetStatus getStatus();

    /**
     * Gets the ID uniquely identifying this bet.
     *
     * @return the ID of this bet.
     */
    long getId();

    long getTimeStarted();

    /**
     * Gets all the participants of this game.
     *
     * @return A {@link Set Set&lt;Long&gt;} containing the ID of the users.
     */
    Set<Long> getParticipants();

    Set<Long> getWinners();

    default boolean isParticipating(long userId) {
        return getParticipants().contains(userId);
    }

    default boolean hasWinner() {
        return !getWinners().isEmpty();
    }
}