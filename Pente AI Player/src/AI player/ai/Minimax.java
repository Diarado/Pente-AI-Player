package a5.ai;

import static a5.ai.GameModel.WIN;

import a5.ai.TranspositionTable.StateInfo;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.*;

/**
 * Implementation of minimax search with alpha-beta pruning.
 */
public class Minimax<GameState, Move> {

    /**
     * The model object for accessing game states.
     */
    private GameModel<GameState, Move> gameModel;
    /**
     * Table of memoized minimax values for previously encountered states.
     */
    private Maybe<TranspositionTable<GameState>> transpositionTable;

    /** Statistics to keep track of how useful the table is. */
    private int tableQueries = 0;
    private int tableHits = 0;
    /** When to flush the table and start over */
    public static final int MAX_TRANSPOSITION_TABLE_SIZE = 10000000;

    /** Whether to use alpha-beta pruning. */
    private boolean alphaBetaPruning = true;
    /** Whether to report statistics. */
    private boolean showSearchInfo = true;
    /** Maximum time to search (in ms) */
    private long endTime;
    /** Number of moves tried in the current search. */
    private int movesTried;

    /**
     * Create a new minimax AI search engine.
     *
     * @param model The model of the game
     */
    public Minimax(GameModel<GameState, Move> model) {
        gameModel = model;
        transpositionTable = Maybe.none();
    }

    /**
     * Effect: Turn on the use of a transposition table.
     */
    public void activateTranspositionTable() {
        transpositionTable = Maybe.some(new TranspositionTable<>());
    }

    /**
     * Effect: Set whether alpha-beta pruning is used. It is turned on by default.
     */
    public void setAlphaBetaPruning(boolean value) {
        alphaBetaPruning = value;
    }

    /**
     * Effect: Set whether search info is printed. It is turned on by default.
     */
    public void setShowSearchInfo(boolean value) {
        showSearchInfo = value;
    }

    /**
     * Returns: the best move from the given state that can be found
     * by searching for up to a given amount of time. Returns {@code
     * Maybe.none()} if there is no legal move.
     *
     * @param state      the state to search from
     * @param searchTime the maximum time to search, in milliseconds
     */
    public Maybe<Move> findBestMove(GameState state, int searchTime) {
        // This search is performed as _iterative deepening_, in which
        // the depth limit is progressively increased until time is exhausted.
        // The best move computed using a completed search is the one returned.
        forgetExcessValuations();
        Maybe<Move> bestMove = Maybe.none();
        int globalBest = -WIN;
        List<Move> possibleMoves = gameModel.legalMoves(state);
        if (possibleMoves.isEmpty()) {
            return Maybe.none();
        }
        for (Move m : possibleMoves) {
            bestMove = Maybe.some(m);
            break;
        }
        // We have a move if one exists. Now try to find a better one
        // by searching with iterative deepening.
        long start = new Date().getTime();
        startTimer(start + searchTime);
        boolean found_win = false;
        try {
            for (int depth = 0;
                    !found_win && (new Date().getTime()) - start < searchTime;
                    depth++) {
                // the best move found at this search depth
                Maybe<Move> localBestMove = bestMove;
                int best = -WIN;
                if (showSearchInfo) {
                    System.out.print("Searching to depth " + depth + "...");
                }
                for (Move move : possibleMoves) {
                    GameState nextState = gameModel.applyMove(state, move);
                    // System.out.println(nextState);
                    int newMax = alphaBetaPruning ? -best : WIN;
                    int value = -search(nextState, depth, -WIN, newMax);
                    if (value > best) {
                        best = value;
                        localBestMove = Maybe.some(move);
                    }
                    if (best == WIN) {
                        found_win = true;
                        localBestMove = Maybe.some(move);
                        break;
                    }
                }
                bestMove = localBestMove;
                globalBest = best;
                if (showSearchInfo) {
                    System.out.println("done.");
                }
            }
        } catch (OutOfTime exc) {
            if (showSearchInfo) {
                System.out.println("out of time.");
            }
            // take the best move we've seen
        }
        showSearchInfo(globalBest, searchTime);
        return bestMove;
    }

    /**
     * Effect: Reports information about the search algorithm.
     */
    private void showSearchInfo(int globalBest, long searchTime) {
        if (showSearchInfo) {
            System.out.println("Estimated game value: " + globalBest);
            System.out.println("Moves tried: " + movesTried + ", positions/sec searched: "
                    + 1000.0 * movesTried / searchTime);
            if (transpositionTable.isPresent()) {
                System.out.println("Table hit rate: " +
                        (double) tableHits / tableQueries);
                try {
                    System.out.println("Table clustering: " +
                            transpositionTable.get().estimateClustering());
                } catch (NoMaybeValue exc) {
                    // can't happen
                }
            }
        }
    }

    /**
     * Start the timer for this search. It expires when the time goes
     * past endTime.
     */
    private void startTimer(long endTime) {
        this.endTime = endTime;
        this.movesTried = 0;
    }

    /**
     * Effect: register that move has been tried, and check whether time
     * has expired if enough moves have been tried. Throws OutOfTime if
     * time has expired.
     */
    private void checkTimer() throws OutOfTime {
        movesTried++;
        if (movesTried % 8192 == 0) {
            if (new Date().getTime() > endTime) {
                throw new OutOfTime();
            }
        }
    }

    /**
     * Returns: the optimal minimax value of the current game state,
     * within the range from min to max.  The minimax value is computed
     * with a specified search depth, with game states reached when
     * depth is 0 evaluated heuristically. It may search deeper than the
     * specified depth.  Checks: min &lt; max Requires: depth &gt;= 0,
     * -WIN &lt;= min, max &lt;= WIN. Throws OutOfTime if the search has
     * run out of time.
     */
    public int search(GameState state,
            int depth, int min, int max) throws OutOfTime {
        assert -WIN <= min && min < max && max <= WIN;
        assert depth >= 0;

        // To avoid searching on a given game state repeatedly,
        // a transposition table is used to look up whether this
        // state has already been searched. If it has been searched
        // already to adequate depth, the result found in the table is
        // used immediately. Otherwise, the result is computed by
        // searching, and the transposition table is updated at the
        // end with the new information.

        try {
            return lookupState(state, depth);
        } catch (NoMaybeValue exc) {
            // no table or state is not in table; keep going on slow path
        }
        if (depth == 0 || gameModel.hasEnded(state)) {
            int value = gameModel.evaluate(state);
            value = Integer.min(Integer.max(value, min), max);
            updateTable(state, depth, value);
            return value;
        }

        // If the depth budget permits searching, try all possible
        // moves to find the best one, performing a search from the
        // opponent's viewpoint to evaluate the position that is reached.
        // Since the opponent's evaluation is the negative of this player's,
        // the result of the search is the negative of the call. Further,
        // the roles of min and max are swapped and negated. As
        // moves are found that are better than min, it becomes useless to
        // search opponent moves that do better than the negative of the current
        // best move found. Hence, the 'max' argument in the recursive call is
        // -best rather than -min. This implements alpha-beta pruning.

        List<Move> possibleMoves = gameModel.legalMoves(state);
        int best = min;
        for (Move move : possibleMoves) {
            checkTimer();
            GameState nextState = gameModel.applyMove(state, move);
            int newMax = alphaBetaPruning ? -best : -min;
            int value = -search(nextState, depth - 1, -max, newMax);
            best = Integer.max(best, value);
            if (best >= max) {
                updateTable(state, depth, max);
                return max;
            }
        }
        updateTable(state, depth, best);
        assert min <= best && best <= max;
        return best;
    }

    /**
     * Returns the value of state if the transposition table has an entry for it with at least the
     * specified depth. Throws NoMaybeValue otherwise.
     */
    int lookupState(GameState state, int depth) throws NoMaybeValue {
        tableQueries++;
        StateInfo info = transpositionTable
                .thenMaybe(t -> t.getInfo(state))
                .get();
        if (info.depth() >= depth) {
            tableHits++;
            return info.value();
        }
        throw NoMaybeValue.theException;
    }

    /**
     * Update the transposition table, if it exists, so that it contains an entry mapping the state
     * to the depth and value.
     */
    void updateTable(GameState state, int depth, int value) {
        try {
            transpositionTable.get().add(state, depth, value);
        } catch (NoMaybeValue exc) {
            // no transposition table; ignore
        }
    }

    /**
     * Effect: Forget all memoized valuations. Useful if the transposition table is getting too
     * large.
     */
    void forgetExcessValuations() {
        transpositionTable.thenDo(tt -> {
            if (tt.size() > MAX_TRANSPOSITION_TABLE_SIZE) {
                transpositionTable = Maybe.some(new TranspositionTable<>());
            }
        });
    }

    public static class OutOfTime extends Exception {
    }
}