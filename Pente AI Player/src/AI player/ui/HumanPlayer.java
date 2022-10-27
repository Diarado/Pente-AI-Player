package a5.ui;

import a5.ai.GameModel;
import a5.logic.Position;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A human player
 * @param <GameState> the type of game this player plays
 */
public class HumanPlayer<GameState> implements Player {

    /**
     * The scanner where the player inputs moves.
     */
    final Scanner input;
    public static String ASK_MOVE = "Please input next move:";
    public static String WRONG_MOVE_FORMAT = "Input is in the wrong format, please input a move as column symbol concatenated with row number. Such as \"a1\"";
    public static String INVALID_MOVE_POSITION = "The input position is not valid nor empty";

    /**
     * The running game this player is playing.
     */
    final GameState state;

    /**
     * The game model this player is deploying.
     */
    final GameModel<GameState, Position> gameModel;

    public HumanPlayer(GameState state,
            GameModel<GameState, Position> gameModel,
            InputStream input) {
        this.input = new Scanner(input);
        this.state = state;
        this.gameModel = gameModel;
    }

    /**
     * Ask the user to input the next move.
     * Returns: next move of this player
     */
    @Override
    public Maybe<Position> nextMove() {
        Maybe<Position> p = Maybe.none();
        List<Position> legalMoves = gameModel.legalMoves(state);
        if (legalMoves.isEmpty()) {
            return Maybe.none();
        }
        while (!p.isPresent()) {
            System.out.println(ASK_MOVE);
            String cmd = input.nextLine();
            p = parsePosition(cmd);
            boolean legalMove = false;
            try {
                for (Position move : legalMoves) {
                    if (move.equals(p.get())) {
                        legalMove = true;
                    }
                }
                if (!legalMove) {
                    System.out.println(INVALID_MOVE_POSITION);
                    p = Maybe.none();
                }
            } catch (NoMaybeValue exc) {
                System.out.println(WRONG_MOVE_FORMAT);
            }
        }
        return p;
    }

    /**
     * Returns: the position parsed from the String s, if any, in a Maybe. Returns Maybe.none() if s
     * does not describe a position. A valid position starts with a lowercase letter (a-z)
     * specifying the column and is followed by digits specifying the row (offset by 1). Examples:
     * a1 means position (0, 0) z20 means position (19, 25)
     */
    private Maybe<Position> parsePosition(String s) {
        String regex = "([a-z])(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);

        if (matcher.matches()) {
            int colNo = matcher.group(1).charAt(0) - 'a';
            int rowNo = Integer.parseInt(matcher.group(2)) - 1;
            return Maybe.some(new Position(rowNo, colNo));
        } else {
            return Maybe.none();
        }
    }
}
