package a5.util;

import cms.util.maybe.Maybe;

/**
 * An enum representing the type of player.
 * Each type is associated with a string {@code name}
 * that represents the player's type.
 */
public enum PlayerType {

    AI("ai"),
    HUMAN("human"),
    AI2("ai2"); // alternate AI player, for comparing two AIs

    private final String name;

    PlayerType(String name) {
        this.name = name;
    }

    /**
     * Returns: the type of player whose name matches.
     * It is case-insensitive.
     * @param name type of the player
     */
    public static Maybe<PlayerType> fromString(String name) {
        for (PlayerType type : PlayerType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return Maybe.some(type);
            }
        }
        return Maybe.none();
    }

    public String toString() {
        return name;
    }

    /**
     * Returns: a string representing a list of all valid player types.
     * The format is: &lt;playertype1|playertype2...&gt;
     */
    public static String options() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        boolean first = true;
        for (PlayerType type : PlayerType.values()) {
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
