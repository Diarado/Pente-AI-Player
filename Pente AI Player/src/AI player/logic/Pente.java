package a5.logic;

import a5.util.PlayerRole;
import a5.util.GameType;
import a5.util.GameResult;
import java.util.Arrays;


/**
 * A Pente game, where players take turns to place stones on board.
 * When consecutive two stones are surrounded by the opponent's stones on two ends,
 * these two stones are removed (captured).
 * A player wins by placing 5 consecutive stones or capturing stones 5 times.
 */
public class Pente extends MNKGame {

    private int captureVal1;
    private int captureVal2;
    private int captureToWin;



    /**
     * Create an 8-by-8 Pente game.
     */
    public Pente() {
        super(8, 8, 5);
        captureVal1 = 0;
        captureVal2 = 0;
        captureToWin = 5;

    }

    /**
     * Creates: a copy of the game state.
     */
    public Pente(Pente game) {
        super(game);
        captureVal1 = game.captureVal1;
        captureVal2 = game.captureVal2;

    }

    @Override
    public boolean makeMove(Position p) {
        if (!board().validPos(p)) {
            return false;
        }
        board().place(p, currentPlayer());

        int currCap = 0;
        // Specify cases to capture stones & erase them from the board
        int[][] steps = {{+1, +1}, {+1, 0}, {+1, -1}, {0, +1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, +1}};

        for(int i = 0; i < steps.length; i++){
            int[] step = steps[i];
            Position newP1 = new Position(p.row()+step[0], p.col()+step[1]);
            Position newP2 = new Position(newP1.row()+step[0], newP1.col()+step[1]);
            Position newP3 = new Position(newP2.row()+step[0], newP2.col()+step[1]);
            if (board().onBoard(newP1) && board().onBoard(newP2) && board().onBoard(newP3)){
                if(board().get(newP1) != board().get(p) && board().get(newP1) != 0 && board().get(newP2) != board().get(p) && board().get(newP2) != 0 && board().get(newP3) == board().get(p)){
                    currCap ++;
                    board().erase(newP1);
                    board().erase(newP2);
                }
            }
        }
        if(currentPlayer().boardValue() == 1){
            captureVal1 += currCap;
        }
        else{
            captureVal2 += currCap;
        }
        changePlayer();
        advanceTurn();
        return true;
    }

    /**
     * Returns: a new game state representing the state of the game after the current player takes a
     * move {@code p}.
     */
    public Pente applyMove(Position p) {
        Pente newGame = new Pente(this);
        newGame.makeMove(p);
        return newGame;
    }

    /**
     * Returns: the number of captured pairs by {@code playerRole}.
     */
    public int capturedPairsNo(PlayerRole playerRole) {
        if(playerRole.boardValue() == (byte) 1){
            return captureVal1;
        }
        else{
            return captureVal2;
        }
    }

    @Override
    public boolean hasEnded() {
        if(super.hasEnded()){
            return true;
        }
        else if(captureVal1 >= 5){
            setResult(GameResult.FIRST_PLAYER_WON);
            return true;
        }
        else if (captureVal2 >= 5){
            setResult(GameResult.SECOND_PLAYER_WON);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public GameType gameType() {
        return GameType.PENTE;
    }


    @Override
    public String toString() {
        String board = super.toString();
        return board + System.lineSeparator() + "Captured pairs: " +
                "first: " + capturedPairsNo(PlayerRole.FIRST_PLAYER) + ", " +
                "second: " + capturedPairsNo(PlayerRole.SECOND_PLAYER);
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        Pente p = (Pente) o;
        return stateEqual(p);
    }

    /**
     * Returns: true if the two games have the same state.
     */
    protected boolean stateEqual(Pente p) {
        if (super.stateEqual(p)){
            return(captureVal1 == p.captureVal1 && captureVal2 == p.captureVal2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int temp = Math.abs(Arrays.hashCode(new int[]{
                Math.abs(super.hashCode()),
                captureVal1, captureVal2
        }));
        return Math.abs(temp * 8360921);
    }
}
