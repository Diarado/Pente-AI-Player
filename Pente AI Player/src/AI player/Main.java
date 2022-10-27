package a5;

import a5.ai.GameModel;
import a5.ai.PenteModel;
import a5.ai.TicTacToeModel;
import a5.logic.MNKGame;
import a5.logic.Pente;
import a5.logic.Position;
import a5.logic.TicTacToe;
import a5.ui.AIPlayer;
import a5.ui.HumanPlayer;
import a5.ui.Player;
import a5.util.GameResult;
import a5.util.GameType;
import a5.util.PlayerRole;
import a5.util.PlayerType;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The main program for Assignment 5.
 */
public class Main {

    /**
     * The message the program shows to welcome the player.
     */
    public static String WELCOME_MSG = "Welcome to the (m,n,k) game engine!";

    /**
     * Maximum time to search for moves by AI players
     */
    static int timeLimit = 3000;

    /**
     * The main method to run a game.
     *
     * @param args args must contain at least these three arguments: game type, type of the first player (AI or
     *             human), type of the second players (AI or human).
     *             Three options can be specified before them:
     *             "--showinfo" enable printing out info for minimax search)
     *             "--timelimit n" set time limit for AI players to n ms
     *             "--help" show usage help
     */
    public static void main(String[] args) {
        boolean valid = true;
        boolean enableShowInfo = false;
        GameType gameType;
        PlayerType firstPlayer, secondPlayer;

        int argi = 0;
        while (valid && argi < args.length) {
            if (args[argi].charAt(0) != '-') break;
            switch (args[argi].toLowerCase(Locale.ROOT)) {
                case "--showinfo":
                    enableShowInfo = true;
                    break;
                case "--timelimit":
                    argi++;
                    try {
                        timeLimit = Integer.parseInt(args[argi]);
                    } catch (NumberFormatException
                            |ArrayIndexOutOfBoundsException exc) {
                        valid = false;
                    }
                    break;
                case "--help":
                    valid = false;
                    break;
            }
            argi++;
        }
        if (valid && args.length - argi == 3) {
            // parse the options
            try {
                gameType = GameType.fromString(args[argi]).get();
                firstPlayer = PlayerType.fromString(args[argi + 1]).get();
                secondPlayer = PlayerType.fromString(args[argi + 2]).get();
                run(gameType, firstPlayer, secondPlayer, enableShowInfo);
            } catch (NoMaybeValue exc) {
                valid = false;
            }
        } else {
            valid = false;
        }

        if (!valid) showUsage();
    }

    private static void parseOptions() {
    }

    /**
     * Run a new game.
     * @param gameType the type of this game
     * @param firstPlayer the type of the first player
     * @param secondPlayer the type of the second player
     * @param enableShowInfo if turn on info printing in minimax search
     */
    private static void run(GameType gameType, PlayerType firstPlayer,
            PlayerType secondPlayer, boolean enableShowInfo) {

        welcome(gameType, firstPlayer, secondPlayer);
        Map<PlayerRole, Player> players = new HashMap<>();
        MNKGame game = createPlayers(gameType, firstPlayer, secondPlayer, players, enableShowInfo);

        while (!game.hasEnded()) {
            displayLine(game.toString());
            displayLine(currentPlayerToString(game.currentPlayer(), players));
            Player p = players.get(game.currentPlayer());
            Maybe<Position> nextPosition = p.nextMove();
            try {
                game.makeMove(nextPosition.get());
            } catch (NoMaybeValue exc) {
                // player failed to move
                System.out.println("The current player failed to move.");
                break;
            }
        }

        System.out.println(game);
        displayLine(game.toString());
        displayLine(endMessage(game.result()));
    }

    private static void welcome(GameType gameType, PlayerType firstPlayer, PlayerType secondPlayer) {
        String SPLIT_LINE = "-----------------------------------";
        displayLine(SPLIT_LINE);
        displayLine(WELCOME_MSG);
        displayLine(gameSettingToStrings(gameType, firstPlayer, secondPlayer));
        displayLine(SPLIT_LINE);
    }

    /** Set up the game and players */
    private static MNKGame createPlayers(GameType gameType, PlayerType firstPlayer, PlayerType secondPlayer,
            Map<PlayerRole, Player> players, boolean enableShowInfo) {
        MNKGame game;
        switch (gameType) {
            case PENTE:
                Pente pente = new Pente();
                game = pente;
                players.put(PlayerRole.FIRST_PLAYER,
                        createPlayer(pente, new PenteModel(), firstPlayer, enableShowInfo));
                players.put(PlayerRole.SECOND_PLAYER,
                        createPlayer(pente, new PenteModel(), secondPlayer, enableShowInfo));
                break;
            case TIC_TAC_TOE:
                TicTacToe ticTacToe = new TicTacToe();
                game = ticTacToe;
                players.put(PlayerRole.FIRST_PLAYER,
                        createPlayer(ticTacToe, new TicTacToeModel(), firstPlayer, enableShowInfo));
                players.put(PlayerRole.SECOND_PLAYER,
                        createPlayer(ticTacToe, new TicTacToeModel(), secondPlayer, enableShowInfo));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + gameType);
        }
        return game;
    }

    /**
     * Returns: a new player.
     * @param game the game the player is playing
     * @param gameModel the game model of the game
     * @param type the type of this player
     * @param enableShowInfo if turn on info printing in minimax search
     * @param <GameState> the type of game
     */
    private static <GameState> Player createPlayer(GameState game,
            GameModel<GameState, Position> gameModel,
            PlayerType type, boolean enableShowInfo) {
        switch (type) {
            case AI:
                return new AIPlayer<>(game, gameModel, true, false, enableShowInfo, timeLimit);
            case AI2:
                return new AIPlayer<>(game, gameModel, true, true, enableShowInfo, timeLimit);
            case HUMAN:
            default:
                return new HumanPlayer<>(game, gameModel, System.in);
        }
    }

    /**
     * Print out usage help of this program.
     */
    private static void showUsage() {
        String sb = "usage: a5.Main [--help] [--showinfo] [--timelimit <ms>]" +
                GameType.options() +
                " " + PlayerType.options() +
                " " + PlayerType.options();
        System.out.println(sb);
    }

    /**
     * Print out one message in one line.
     * @param msg one message
     */
    public static void displayLine(String msg) {
        System.out.println(msg);
    }

    /**
     * Returns: a string presenting the game setting.
     * @param gameType type of the game
     * @param p1 type of the first player
     * @param p2 type of the second player
     */
    public static String gameSettingToStrings(GameType gameType, PlayerType p1, PlayerType p2) {
        String nl = System.lineSeparator();
        return "Game type: " + gameType + nl +
               "First player: " + p1 + nl +
               "Second player: " + p2 + nl;
    }

    /**
     * Returns: a string presenting the current player.
     * @param currentPlayer role of the current player
     * @param players players of the game
     */
    public static String currentPlayerToString(PlayerRole currentPlayer,
            Map<PlayerRole, Player> players) {
        return "Current player: " +
                (currentPlayer == PlayerRole.FIRST_PLAYER ?
                        "first" + (players.get(currentPlayer) instanceof AIPlayer ? "(AI)" : "") :
                        "second" + (players.get(currentPlayer) instanceof AIPlayer ? "(AI)" : ""));
    }

    /**
     * Returns: a string presenting the game result.
     * @param result result of the game
     */
    public static String endMessage(GameResult result) {
        String rtn = "The game ended.";
        if (result == GameResult.DRAW) {
            rtn += " It is a draw.";
        } else {
            rtn += " The " + (result == GameResult.FIRST_PLAYER_WON ? "first" : "second")
                    + " player won.";
        }
        return rtn;
    }
}
