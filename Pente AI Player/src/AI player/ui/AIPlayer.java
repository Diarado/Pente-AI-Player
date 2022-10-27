package a5.ui;

import a5.ai.GameModel;
import a5.ai.Minimax;
import a5.logic.Position;
import cms.util.maybe.Maybe;

/**
 * A computer-driven player that uses minimax search to find good moves.
 * @param <GameState> the type of game this player plays
 */
public class AIPlayer<GameState> implements Player {

    /**
     * The running game this player is playing.
     */
    private GameState state;

    /**
     * The game model this player is deploying.
     */
    private GameModel<GameState, Position> gameModel;

    /**
     * If the AI algorithm uses Transposition Table.
     */
    private boolean useTranspositionTable;

    /**
     * If the AI algorithm uses Alpha-beta Pruning.
     */
    private boolean useAlphaBetaPruning;

    /**
     * Maximum time the search will take (ms)
     */
    int timeLimit;

    /**
     * The minimax searcher this player uses.
     */
    Minimax<GameState, Position> searcher;

    public AIPlayer(GameState state,
                    GameModel<GameState, Position> model,
                    boolean useAlphaBetaPruning,
                    boolean useTranspositionTable,
                    boolean enableShowInfo,
                    int timeLimit) {
        gameModel = model;
        this.state = state;
        this.useTranspositionTable = useTranspositionTable;
        this.useAlphaBetaPruning = useAlphaBetaPruning;
        this.timeLimit = timeLimit;
        searcher = new Minimax<>(gameModel);
        if (useTranspositionTable) searcher.activateTranspositionTable();
        searcher.setAlphaBetaPruning(useAlphaBetaPruning);
        searcher.setShowSearchInfo(enableShowInfo);
    }

    /**
     * Search the best next move, within time limit.
     * Returns: the best move if found one.
     */
    @Override
    public Maybe<Position> nextMove() {
        return searcher.findBestMove(state, timeLimit);
    }
}