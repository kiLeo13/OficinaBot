package ofc.bot.handlers.games.betting;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public interface Bet<T> {

    /**
     * Starts the current bet game.
     *
     * @param args the arguments to be passed to the start command.
     */
    void start(GameArgs args);

    /**
     * Stops this current bet game.
     * <p>
     * This method should (usually) never throw exceptions,
     * but instead, finish the game gracefully (even though it was a "forced" operation).
     *
     * @param args the arguments to be passed to the end command.
     */
    void end(GameArgs args);

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
     * Adds the specified users to the participants list.
     * <p>
     * This method serves as a shortcut for {@link #join(long, Object)},
     * but without requiring a specific bet/guess.
     * <p>
     * <b>Note:</b> Depending on the {@link BetType}, calling this method may fail immediately
     * if the game requires a bet to be placed (e.g., non-randomizable bet types like roulette).
     * <p>
     * <i>You can check if a bet type is randomizable with {@link BetType#isRandomizable()}.</i>
     *
     * @param userIds The IDs of the users who wish to participate in the game.
     * @throws UnsupportedOperationException If the game type requires a bet and does
     *         not support joining without one.
     *         <br>In this case, you should always use {@link #join(long, Object)} instead.
     * @see #join(long, Object)
     */
    void join(long... userIds);

    /**
     * Allows a user to join the game by providing their bet or guess for the outcome.
     * <p>
     * This method ensures that the user is added to the game with a specific bet/role,
     * which is required for games that involve user predictions or wagers.
     * <p>
     * For instance, if this is an instance of a {@link BetType#TIC_TAC_TOE} game,
     * you can use this method as follows:
     * <pre>
     *   {@code
     * Bet<Character> game = new TicTacToeGame(ecoRepo, betRepo, betUsersRepo, api, 200);
     * game.join(123, 'X');
     * game.join(456, 'O');
     *
     * game.start(msg);
     *   }
     * </pre>
     * <p>
     * <b>Note:</b> This method will likely not enforce a limit on the number of users who can join.
     * This is only checked when calling {@link #start(GameArgs)}.
     * It is the developer's responsability to check the number of participants joining the game
     * over the {@link #getMaxUsers()}
     *
     * @param userId The ID of the user joining the game.
     * @param bet The user's bet, role or guess for the outcome of the game.
     * @throws IllegalArgumentException If the bet is {@code null} or invalid for the game type.
     * @throws IllegalStateException If the game is not accepting new participants.
     */
    void join(long userId, @NotNull T bet);

    /**
     * Just a shortcut for {@link #join(long, Object)}.
     *
     * @param users A {@link Map Map&lt;Long, T&gt;} containing the user IDs and bets.
     */
    default void join(@NotNull Map<Long, T> users) {
        users.forEach(this::join);
    }

    BetType getType();

    BetStatus getStatus();

    long getTimeStarted();

    /**
     * Gets all the participants of this game.
     *
     * @return A {@link Set Set&lt;Long&gt;} containing the ID of the users.
     */
    Set<Long> getParticipants();

    Set<Long> getWinners();

    /**
     * Gets the max amount of users that can join this game.
     *
     * @return The max amount of users allowed to join this game, or {@code 0} if there is no limit.
     */
    int getMaxUsers();

    default boolean isParticipating(long userId) {
        return getParticipants().contains(userId);
    }

    default boolean hasWinner() {
        return !getWinners().isEmpty();
    }
}