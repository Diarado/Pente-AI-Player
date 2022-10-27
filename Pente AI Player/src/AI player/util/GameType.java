package a5.util;

import cms.util.maybe.Maybe;

/**
 * An enum representing the type of game.
 * Each type is associated with a string {@code name}
 * that represents the game's name.
 */
public enum GameType {
    TIC_TAC_TOE("Tic-Tac-Toe"),
    PENTE("Pente");

    private final String name;

    GameType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Returns: the type of game whose name matches.
     * It is case-insensitive.
     * @param name name of the game
     */
    public static Maybe<GameType> fromString(String name) {
        for (GameType type : GameType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return Maybe.some(type);
            }
        }
        return Maybe.none();
    }

    /**
     * Returns: a string representing a list of all valid game types.
     * The format is: &lt;gametype1|gametype2...&gt;
     */
    public static String options() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        boolean first = true;
        for (GameType type : GameType.values()) {
            if (!first) {
                sb.append("|");
            } else {
                first = false;
            }
            sb.append(type.name);
        }
        sb.append(">");
        return sb.toString();
    }
}
