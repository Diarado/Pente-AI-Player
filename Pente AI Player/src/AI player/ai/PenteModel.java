package a5.ai;

import a5.logic.Board;
import a5.logic.Pente;
import a5.logic.Position;
import a5.util.GameResult;
import a5.util.PlayerRole;

import java.util.List;

/**
 * A model for Pente and Position satisfying the constraints of GameModel.
 */
public class PenteModel implements GameModel<Pente, Position> {

    @Override
    public List<Position> legalMoves(Pente s) {
        return s.legalMoves();
    }

    static final int[][] consecutive_weights = {
            {0, +1, +20, +30, +40},
            {0, -1, -20, -30, -40}
    };

    static final int captured_weight = 40;

    /**
     * This heuristic evaluation look for number of consecutive unblocked stones of both players,
     * and calculate a weighted sum of these numbers and number of captured pairs of both players.
     * {@code consecutive_weights[i][k]} represents the weight of k consecutive unblocked stones
     * of current player (i == 0) or the other player (i == 1).
     * {@code captured_weight[i]} represents the weight of captured pairs of
     * current player (i == 0) or the other player (i == 1).
     */
    @Override
    public int evaluate(Pente s) {

        if (s.hasEnded()) {
            if (s.result() == GameResult.DRAW) {
                return 0;
            } else {
                PlayerRole winner = s.result() == GameResult.FIRST_PLAYER_WON ? PlayerRole.FIRST_PLAYER
                        : PlayerRole.SECOND_PLAYER;
                return winner == s.currentPlayer() ? WIN : -WIN;
            }
        } else {
            int score = captured_weight *
                    (s.capturedPairsNo(s.currentPlayer())
                            - s.capturedPairsNo(s.currentPlayer().nextPlayer()));
            for (int i = 0; i < s.rowSize(); ++i) {
                for (int j = 0; j < s.colSize(); ++j) {
                    Position p = new Position(i, j);
                    Board board = s.board();
                    int current = board.get(p);
                    if (current != 0) {
                        int[][] steps = {
                                {+1, +1}, {+1, 0}, {+1, -1}, {0, +1}
                        };
                        for (int[] step : steps) {
                            boolean success = true, consecutive = true;
                            int len = 1, emptyCells = 0;
                            Position next = p;
                            int[] reverseStep = {-step[0], -step[1]};

                            Position prev = new Position(p.row() + reverseStep[0], p.col() + reverseStep[1]);

                            if (board.onBoard(prev)) {
                                if (board.get(prev) == current) {
                                    success = false;
                                } else {
                                    while (board.onBoard(prev) && board.get(prev) == 0) {
                                        ++emptyCells;
                                        prev = new Position(prev.row() + reverseStep[0], prev.col() + reverseStep[1]);
                                    }
                                }
                            }

                            for (int k = 1; success && k < s.countToWin(); ++k) {
                                next = new Position(next.row() + step[0], next.col() + step[1]);
                                if (!board.onBoard(next)) {
                                    break;
                                }
                                if (consecutive) {
                                    if (board.get(next) == board.get(p)) {
                                        ++len;
                                    } else {
                                        consecutive = false;
                                    }
                                }

                                if (!consecutive) {
                                    if (board.get(next) != 0) {
                                        break;
                                    } else {
                                        ++emptyCells;
                                    }
                                }
                            }

                            if (success && len + emptyCells >= s.countToWin()) {
                                score += consecutive_weights[
                                        board.get(p) == s.currentPlayer().boardValue() ? 0
                                                : 1][len];
                            }
                        }
                    }
                }
            }
            return score;
        }
    }

    public Pente applyMove(Pente pente, Position position) {
        return pente.applyMove(position);
    }

    @Override
    public boolean hasEnded(Pente game) {
        return game.hasEnded();
    }
}