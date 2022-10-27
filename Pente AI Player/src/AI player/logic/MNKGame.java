package a5.logic;

import a5.util.GameResult;
import a5.util.GameType;
import a5.util.PlayerRole;

import java.util.*;

/**
 * A running (m, n, k) game. An (m, n, k) game is an abstract board game in which two players take
 * turns in placing a stone of their color on an m-by-n board, the winner being the player who first
 * gets k stones of their own color in a row, horizontally, vertically, or diagonally.
 */
public abstract class MNKGame {

    /**
     * The result of this game. Only meaningful after the game ends.
     */
    private GameResult result;

    /**
     * The board state of the game.
     */
    private final Board board;

    /**
     * k of the game.
     */
    private final int countToWin;

    /**
     * Current turn number.
     */
    private int currentTurn;

    /**
     * Current player of the game.
     */
    private PlayerRole currentPlayer;

    protected MNKGame(int m, int n, int k) {
        board = new Board(m, n);
        countToWin = k;
        currentTurn = 1;
        currentPlayer = PlayerRole.FIRST_PLAYER;
    }

    /**
     * Create a game copy.
     */
    protected MNKGame(MNKGame game) {
        board = new Board(game.board);
        result = game.result;
        countToWin = game.countToWin;
        currentTurn = game.currentTurn;
        currentPlayer = game.currentPlayer;
    }

    /**
     * Effect: place a stone as the current player at p, and advance to the
     * next turn
     * Checks: {@code p} is a valid position to place a stone, that is, 0 &lt;= {@code p.rowNo()} &lt; {@code rowSize()}, 0 &lt;=
     * {@code p.colNo()} &lt; {@code colSize()}, and {@code p} is empty on board.
     * on board.
     *
     * @param p the position to place a stone
     * @return true if the action is valid, false otherwise
     */
    public abstract boolean makeMove(Position p);

    /**
     * Checks if there are enough consecutive stones in a row so that the game ends.
     * Returns: whether this game has ended.
     * Effect: if the game has ended, determines the result of the game.
     */
    public boolean hasEnded() {
        for (int i = 0; i < colSize(); ++i) {
            for (int j = 0; j < rowSize(); ++j) {
                Position p = new Position(i, j);
                if (board.get(p) != 0) {
                    int[][] steps = {{+1, +1}, {+1, 0}, {+1, -1}, {0, +1}};
                    for (int[] step : steps) {
                        boolean success = true;
                        Position newP = new Position(i, j);
                        for (int l = 2; l <= countToWin; ++l) {
                            newP = new Position(newP.row() + step[0], newP.col() + step[1]);
                            if (!board.onBoard(newP)
                                    || board.get(newP) != board.get(p)) {
                                success = false;
                                break;
                            }
                        }
                        if (success) {
                            result = board.get(p) == PlayerRole.FIRST_PLAYER.boardValue()
                                    ? GameResult.FIRST_PLAYER_WON : GameResult.SECOND_PLAYER_WON;
                            return true;
                        }
                    }
                }
            }
        }
        if (isFull()) {
            // draw
            result = GameResult.DRAW;
            return true;
        }
        return false;
    }

    /**
     * Returns: true if the board is empty.
     */
    private boolean isFull() {
        for (int i = 0; i < board.rowSize(); ++i) {
            for (int j = 0; j < board.colSize(); ++j) {
                if (board.get(new Position(i, j)) == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns: The result of the game.
     * Requires: the game has ended and the result has been determined.
     */
    public GameResult result() {
        return result;
    }

    /**
     * Set the result of the game. Requires: game has ended.
     */
    public void setResult(GameResult r) {
        result = r;
    }

    /**
     * Returns: the board state.
     */
    public Board board() {
        return board;
    }

    /**
     * Returns: the number of rows of the board.
     */
    public int rowSize() {
        return board.rowSize();
    }

    /**
     * Returns: the number of columns of the board.
     */
    public int colSize() {
        return board.colSize();
    }

    /**
     * Returns: the type of this game.
     */
    public abstract GameType gameType();

    /**
     * Returns: number of the current turn.
     */
    public int currentTurn() {
        return currentTurn;
    }

    /**
     * Returns: the current player.
     */
    public PlayerRole currentPlayer() {
        return currentPlayer;
    }

    /**
     * Effect: advance the game turn by one.
     */
    protected void advanceTurn() {
        currentTurn += 1;
    }

    /**
     * Effect: change the current player to be the next.
     */
    protected void changePlayer() {
        currentPlayer = currentPlayer.nextPlayer();
    }

    /**
     * Returns: a set of legal moves.
     */
    public List<Position> legalMoves() {
        ArrayList<Position> result = new ArrayList<>();
        for (int i = 0; i < rowSize(); ++i) {
            for (int j = 0; j < colSize(); ++j) {
                Position p = new Position(i, j);
                if (board.validPos(p)) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    /**
     * Returns: the number of consecutive stones (k) to win.
     */
    public int countToWin() {
        return countToWin;
    }

    @Override
    public String toString() {
        final String FIRST_PLAYER_STONE = "●";
        final String SECOND_PLAYER_STONE = "◯";
        final String NL = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("Current turn: " + currentTurn).append(NL);
        sb.append("Current board:").append(NL);

        String rowPrefix = "   ";

        sb.append(rowPrefix);
        for (int i = 0; i < colSize(); ++i) {
            sb.append("  ").append((char) ('a' + i)).append(' ');
        }
        sb.append(NL);

        String bar = rowPrefix + (new String(new char[colSize()]).replace("\0", "|---")) + "|";

        for (int i = 0; i < rowSize(); ++i) {
            sb.append(bar).append(NL);

            if (i + 1 < 10) {
                sb.append(' ');
            }
            sb.append(i + 1).append(' ');

            for (int j = 0; j < colSize(); ++j) {
                sb.append("| ");
                int value = board.get(new Position(i, j));
                if (value == PlayerRole.FIRST_PLAYER.boardValue()) {
                    sb.append(FIRST_PLAYER_STONE);
                } else if (value == PlayerRole.SECOND_PLAYER.boardValue()) {
                    sb.append(SECOND_PLAYER_STONE);
                } else {
                    sb.append(' ');
                }
                sb.append(' ');
            }
            sb.append("|").append(NL);
        }
        sb.append(bar).append(NL);
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        MNKGame g = (MNKGame) o;
        return this.stateEqual(g);
    }

    /**
     * Returns: true if the two games have equivalent state for
     * the purpose of deciding future moves.
     */
    protected boolean stateEqual(MNKGame g) {
        if (!board.equals(g.board)) return false;
        if (countToWin != g.countToWin) return false;
        return true;
    }

    public int hashCode() {
        return Arrays.hashCode(new int[]{
                board.hashCode(),
                countToWin,
        });
    }
}
