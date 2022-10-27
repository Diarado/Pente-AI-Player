package a5.ai;

import java.util.List;

/**
 * An abstract, immutable representation of a two-player game. This interface exists to support the
 * Concept design pattern, in which objects provide the operations of parameter types. A familiar
 * example of this pattern is the Comparator interface used by TreeSet.
 *
 * @param <GameState> a type describing the current state of the game.
 * @param <Move>      a type describing a legal move in the game.
 */
public interface GameModel<GameState, Move> {

    public int WIN = 10000;

    /**
     * Returns: the set of legal moves in the state s for the player whose move it is.
     */
    List<Move> legalMoves(GameState s);

    /**
     * Returns: the heuristic evaluation of a state of a two-player game, as a value between WIN and
     * -WIN. 0 means the game appears to be tied. A value of WIN means the current player appears
     * certain to WIN, and -WIN means the other player
     *
     * @param s The game state to evaluate
     */
    int evaluate(GameState s);

    /**
     * Returns: a new game state representing the state of the game after the current player takes a
     * move.
     */
    GameState applyMove(GameState state, Move move);

    /**
     * Returns: a boolean indicating whether the game has ended.
     */
    boolean hasEnded(GameState s);
}