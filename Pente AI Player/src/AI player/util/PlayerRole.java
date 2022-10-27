package a5.util;

/**
 * A class representing player roles.
 * In an (m, n, k) game, there are two player roles:
 * the first player and the second player.
 */
public class PlayerRole {

    public static final PlayerRole FIRST_PLAYER = new PlayerRole((byte) 1);
    public static final PlayerRole SECOND_PLAYER = new PlayerRole((byte) 2);

    /**
     * The int representation of the player role's stone.
     */
    private final byte boardValue;

    PlayerRole(byte i) {
        this.boardValue = i;
    }

    /**
     * Returns: the role of the next player when {@code this} is the current player.
     */
    public PlayerRole nextPlayer() {
        return this == FIRST_PLAYER ? SECOND_PLAYER : FIRST_PLAYER;
    }

    public int boardValue() {
        return boardValue;
    }

}
