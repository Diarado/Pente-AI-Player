package a5.ai;

import a5.logic.Board;
import a5.logic.Position;
import a5.logic.TicTacToe;
import a5.util.GameResult;
import a5.util.PlayerRole;

import java.util.List;

public class TicTacToeModel implements GameModel<TicTacToe, Position> {

    @Override
    public List<Position> legalMoves(TicTacToe s) {
        return s.legalMoves();
    }

    @Override
    public int evaluate(TicTacToe s) {
        Board board = s.board();
        if (s.hasEnded()) {
            if (s.result() == GameResult.DRAW) {
                return 0;
            } else {
                PlayerRole winner =
                        s.result() == GameResult.FIRST_PLAYER_WON ? PlayerRole.FIRST_PLAYER
                                : PlayerRole.SECOND_PLAYER;
                return winner == s.currentPlayer() ? WIN : -WIN;
            }
        } else {
            int score = 0;
            for (int i = 0; i < s.rowSize(); ++i) {
                for (int j = 0; j < s.colSize(); ++j) {
                    Position p = new Position(i, j);
                    int current = board.get(p);
                    if (current != 0) {
                        int[][] steps = {
                                {+1, +1}, {+1, 0}, {+1, -1}, {0, +1},
                                {-1, -1}, {-1, 0}, {-1, +1}, {0, -1}
                        };
                        for (int[] step : steps) {
                            if (consecutive(board, p, 2, s.countToWin(), step)) {
                                score += current == s.currentPlayer().boardValue() ? +1 : -1;
                            }
                        }
                    }
                }
            }
            return score;
        }
    }

    /**
     * Check that, starting from {@code p}, among the next {@code countToWin} cells in direction
     * {@code step}, the first {@code length} cells are the same as {@code p}, and the rest are
     * empty cells.
     * <p>
     * Requires: p to be on the board
     */
    private boolean consecutive(Board board, Position p, int length, int countToWin, int[] step) {
        Position here = p;
        for (int i = 1; i < countToWin; ++i) {
            here = new Position(here.row() + step[0], here.col() + step[1]);
            if (!board.onBoard(here)) {
                return false;
            }
            if (i < length) {
                if (board.get(here) != board.get(p)) {
                    return false;
                }
            } else {
                if (board.get(here) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public TicTacToe applyMove(TicTacToe game, Position position) {
        return game.applyMove(position);
    }

    @Override
    public boolean hasEnded(TicTacToe game) {
        return game.hasEnded();
    }
}