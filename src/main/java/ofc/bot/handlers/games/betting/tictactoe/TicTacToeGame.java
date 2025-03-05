package ofc.bot.handlers.games.betting.tictactoe;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.domain.entity.*;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.BetGameRepository;
import ofc.bot.domain.sqlite.repository.GameParticipantRepository;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.events.impl.BankTransactionEvent;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.handlers.games.betting.*;
import ofc.bot.handlers.games.betting.exceptions.BetGameCreationException;
import ofc.bot.handlers.interactions.*;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Bot;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.InteractionHandler;
import ofc.bot.util.embeds.EmbedFactory;
import ofc.bot.util.time.ElasticScheduler;
import org.jetbrains.annotations.Contract;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class TicTacToeGame implements Bet {
    public static final int MIN_AMOUNT = 100;
    public static final int MAX_AMOUNT = 10_000;
    public static final int DEFAULT_GRID_SIZE = 3;
    public static final int TIMEOUT = 60 * 1000;
    public static final float TIMEOUT_PENALTY_RATE = 0.70f;
    private static final Logger LOGGER = LoggerFactory.getLogger(TicTacToeGame.class);
    private static final int ALLOWED_PLAYERS = 2;
    private static final InteractionMemoryManager memoryManager = InteractionMemoryManager.getManager();
    private static final Random RANDOM = new Random();
    private static final BetManager betManager = BetManager.getManager();
    private final int prizeValue;
    private final long id;
    private final UserEconomyRepository ecoRepo;
    private final BetGameRepository betRepo;
    private final GameParticipantRepository betUsersRepo;
    private final JDA api;
    private final GameGrid grid;
    private final Map<Long, Character> players;
    private final ElasticScheduler scheduler; // Handle timeout system
    private long winnerId;
    private long currentPlayerId;
    private long startedAt;
    private Message message;
    private GameHandler gameHandler;
    private BetStatus status;

    public TicTacToeGame(UserEconomyRepository ecoRepo, BetGameRepository betRepo,
                         GameParticipantRepository betUsersRepo, JDA api, int prizeValue, int gridSize) {
        Checks.notNull(ecoRepo, "Economy Repository");
        Checks.notNull(betRepo, "Bet Repository");
        Checks.notNull(betUsersRepo, "Bet Users Repository");
        Checks.notNull(api, "JDA");

        if (prizeValue < MIN_AMOUNT || prizeValue > MAX_AMOUNT)
            failRange(prizeValue);

        this.prizeValue = prizeValue;
        this.id = System.nanoTime();
        this.ecoRepo = ecoRepo;
        this.betRepo = betRepo;
        this.betUsersRepo = betUsersRepo;
        this.api = api;
        this.grid = new GameGrid(gridSize);
        this.players = new HashMap<>(2);
        this.status = BetStatus.WAITING;
        this.scheduler = new ElasticScheduler(this::timeout, TIMEOUT);
        this.winnerId = 0;
    }

    public TicTacToeGame(UserEconomyRepository ecoRepo, BetGameRepository betRepo,
                         GameParticipantRepository betUsersRepo, JDA api, int prizeValue) {
        this(ecoRepo, betRepo, betUsersRepo, api, prizeValue, DEFAULT_GRID_SIZE);
    }

    @Override
    public void start(GameArgs args) {
        if (this.status == BetStatus.RUNNING) return;

        if (players.size() != ALLOWED_PLAYERS)
            failPlayers();

        this.message = args.get(0);
        this.startedAt = Bot.unixNow();
        this.status = BetStatus.RUNNING;
        this.gameHandler = new GameHandler();
        this.currentPlayerId = pickRandomPlayer();

        betManager.addBets(this, players.keySet());
        memoryManager.registerListeners(gameHandler);
        api.retrieveUserById(currentPlayerId).queue(user -> {
            MessageEmbed embed = EmbedFactory.embedTicTacToeGame(user);
            List<ActionRow> rows = Stream.of(EntityContextFactory.createTicTacToeTable(id, currentPlayerId, grid))
                    .map(ActionRow::of)
                    .toList();

            message.editMessageEmbeds(embed)
                    .setComponents(rows)
                    .setReplace(true)
                    .queue((v) -> this.scheduler.start());
        }, (err) -> LOGGER.error("Could not find user for ID {}", currentPlayerId, err));
    }

    @Override
    public void end(GameArgs args) {
        if (this.status != BetStatus.RUNNING) return;

        boolean isDraw = args.get(0);
        this.status = isDraw ? BetStatus.DRAW : BetStatus.COMPLETE;
        this.scheduler.shutdown();

        long timeEnded = Bot.unixNow();
        BetStatus exitStatus = resolveExitStatus(isDraw);

        // Remove bets and listeners regardless of the result
        betManager.removeBets(players.keySet());
        memoryManager.removeListener(gameHandler);

        finalizeGame(exitStatus, timeEnded);
    }

    @Override
    public void timeout() {
        if (this.status != BetStatus.RUNNING) return;

        this.status = BetStatus.TIMED_OUT;
        long timeEnded = Bot.unixNow();
        long targetId = this.currentPlayerId;
        betManager.removeBets(players.keySet());
        memoryManager.removeListener(gameHandler);

        int penaltyAmount = Math.round(this.prizeValue * TIMEOUT_PENALTY_RATE);
        try {
            User target = api.retrieveUserById(targetId).complete();
            UserEconomy userEco = ecoRepo.findByUserId(targetId, UserEconomy.fromUserId(targetId))
                    .modifyBalance(-penaltyAmount)
                    .tickUpdate();

            ecoRepo.upsert(userEco);

            // Inform the users about the timeout action taken
            MessageEmbed embed = EmbedFactory.embedTicTacToeTimeout(penaltyAmount, target);
            message.editMessageEmbeds(embed)
                    .setReplace(true)
                    .queue();

            persist(this.status, timeEnded);
            dispatchTimeoutEvent(penaltyAmount, targetId);
        } catch (DataAccessException e) {
            LOGGER.error("Could not handle tictactoe timeout database operations", e);
        } catch (ErrorResponseException e) {
            LOGGER.error("Could not fetch penalized user of ID {}", targetId, e);
        } finally {
            this.scheduler.shutdown();
        }
    }

    @Override
    public int getTimeout() {
        return TIMEOUT;
    }

    @Override
    public void join(long... userIds) {
        for (long userId : userIds) {
            if (players.isEmpty()) {
                players.put(userId, 'X');
            } else {
                players.put(userId, 'O');
            }
        }
    }

    @Override
    public BetType getType() {
        return BetType.TIC_TAC_TOE;
    }

    @Override
    public BetStatus getStatus() {
        return this.status;
    }

    @Override
    public long getTimeStarted() {
        return this.startedAt;
    }

    @Override
    public Set<Long> getParticipants() {
        return this.players.keySet();
    }

    @Override
    public Set<Long> getWinners() {
        return winnerId == 0 ? Set.of() : Set.of(winnerId);
    }

    private long findLoserId() {
        return players.keySet()
                .stream()
                .filter(id -> id != winnerId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Somehow we did not find the loser ID, what???"));
    }

    private BetStatus resolveExitStatus(boolean isDraw) {
        if (!isDraw && this.winnerId == 0) {
            return BetStatus.INTERRUPTED;
        }
        return this.status;
    }

    private void handlePayment(User winner, User loser) {
        long loserId = loser.getIdLong();

        ecoRepo.transfer(loserId, winnerId, prizeValue);
        dispatchBetWinEvent(winner, loser);
    }

    private void dispatchBetWinEvent(User winner, User loser) {
        long loserId = loser.getIdLong();
        String winnerComment = String.format("Venceu de %s no Jogo da velha.", loser.getName());
        String loserComment = String.format("Perdeu para %s no Jogo da velha.", winner.getName());

        BankTransaction trWin = new BankTransaction(winnerId, prizeValue, winnerComment, CurrencyType.OFICINA, TransactionType.BET_RESULT);
        BankTransaction trLose = new BankTransaction(loserId, -prizeValue, loserComment, CurrencyType.OFICINA, TransactionType.BET_RESULT);

        EventBus.dispatchEvent(new BankTransactionEvent(trWin));
        EventBus.dispatchEvent(new BankTransactionEvent(trLose));
    }

    private void dispatchTimeoutEvent(int amount, long idleId) {
        int percent = Math.round(TIMEOUT_PENALTY_RATE * 100);
        String comment = String.format("Timeout no Jogo da velha, perdeu %d%% de %s.", percent, Bot.fmtNum(this.prizeValue));

        BankTransaction tr = new BankTransaction(idleId, -amount, comment, CurrencyType.OFICINA, TransactionType.BET_PENALTY);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }

    private void finalizeGame(BetStatus exitStatus, long timeEnded) {
        long loserId = findLoserId();
        try {
            User winner = winnerId == 0 ? null : api.retrieveUserById(winnerId).complete();
            User loser = api.retrieveUserById(loserId).complete();

            MessageEmbed embed = EmbedFactory.embedTicTacToeEnd(winner);
            message.editMessageEmbeds(embed)
                    .setReplace(true)
                    .queue();

            persist(exitStatus, timeEnded);

            // Only handle payment if a winner exists and the game was not interrupted
            if (winner != null && exitStatus != BetStatus.INTERRUPTED) {
                handlePayment(winner, loser);
            }
        } catch (DataAccessException e) {
            LOGGER.error("Could not make transaction from {} to {} of {}", loserId, winnerId, prizeValue, e);
        } catch (ErrorResponseException e) {
            LOGGER.error("Could not fetch users {} or {}", winnerId, loserId, e);
        }
    }

    private void persist(BetStatus status, long endTime) {
        BetGame bet = new BetGame(id, status, grid.toString(), getType(), startedAt, endTime);
        List<GameParticipant> participants = players.keySet()
                .stream()
                .map(pId -> new GameParticipant(id, pId, pId == winnerId))
                .toList();

        try {
            betRepo.save(bet);
            betUsersRepo.bulkSave(participants);
        } catch (DataAccessException e) {
            LOGGER.error("Could not save bet game or participants to the database", e);
        }
    }

    private void swapPlayers() {
        for (long userId : players.keySet()) {
            if (currentPlayerId != userId) {
                currentPlayerId = userId;
                return;
            }
        }
    }

    private long pickRandomPlayer() {
        int rand = RANDOM.nextInt(players.size());
        Long[] ids = players.keySet().toArray(new Long[0]);

        return ids[rand];
    }

    @Contract("-> fail")
    private void failPlayers() {
        throw new BetGameCreationException(String.format("TicTacToe game must have exactly %d players, provided: %d",
                ALLOWED_PLAYERS, players.size()));
    }

    @Contract("_ -> fail")
    private void failRange(int value) {
        throw new IllegalArgumentException(String.format("Prize value should be in range of %d - %d, provided: %d",
                MIN_AMOUNT, MAX_AMOUNT, value));
    }

    @InteractionHandler(scope = Scopes.Bets.TICTACTOE_GAME, autoResponseType = AutoResponseType.DEFER_EDIT)
    private class GameHandler implements InteractionListener<ButtonClickContext> {

        @Override
        public InteractionResult onExecute(ButtonClickContext ctx) {
            // Safety purposes, do not proceed if the status is no longer set as RUNNING
            if (status != BetStatus.RUNNING) return Status.OK;

            int row = ctx.get("row");
            int col = ctx.get("col");
            char symbol = players.get(currentPlayerId);
            grid.set(row, col, symbol);

            // Checking for winners
            char winnerSymbol = grid.getWinner();
            winnerId = resolveWinner(winnerSymbol);
            if (winnerId != '\0') {
                end(new GameArgs(false));
                return Status.OK;
            }

            // If we don't have any winners and the table/grid
            // is already full, then we can end the game, its a tie.
            if (!grid.hasSlotAvailable()) {
                end(new GameArgs(true));
                return Status.OK;
            }

            // Proceed if its not tied and we have no winners
            swapPlayers();
            User curr = api.retrieveUserById(currentPlayerId).complete();
            MessageEmbed embed = EmbedFactory.embedTicTacToeGame(curr);
            List<ActionRow> rows = Stream.of(EntityContextFactory.createTicTacToeTable(id, currentPlayerId, grid))
                    .map(ActionRow::of)
                    .toList();

            scheduler.reset();
            return ctx.create()
                    .setEmbeds(embed)
                    .setComponents(rows)
                    .edit();
        }

        @Override
        public boolean validate(ButtonClickContext ctx) {
            return ((long) ctx.get("bet_id")) == id;
        }

        private long resolveWinner(char value) {
            for (long userId : players.keySet()) {
                if (players.get(userId).equals(value)) {
                    return userId;
                }
            }
            return '\0';
        }
    }
}