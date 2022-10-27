package a5.logic;

import a5.util.GameType;

public class TicTacToe extends MNKGame {

    public TicTacToe() {
        super(3, 3, 3);
    }

    /**
     * Creates: a copy of the game state.
     */
    private TicTacToe(TicTacToe game) {
        super(game);
    }

    @Override
    public boolean makeMove(Position p) {
        if (!board().validPos(p)) {
            return false;
        }
        board().place(p, currentPlayer());
        changePlayer();
        advanceTurn();
        return true;
    }

    /**
     * Returns: a new game state representing the state of the game after the current player takes a
     * move {@code p}.
     */
    public TicTacToe applyMove(Position p) {
        TicTacToe newGame = new TicTacToe(this);
        newGame.makeMove(p);
        return newGame;
    }

    @Override
    public GameType gameType() {
        return GameType.TIC_TAC_TOE;
    }

}
