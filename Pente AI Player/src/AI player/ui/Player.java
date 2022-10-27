package a5.ui;

import a5.logic.Position;
import cms.util.maybe.Maybe;

/**
 * A game player.
 */
public interface Player {

    /**
     * Returns: the next move of this player, if there is one.
     */
    Maybe<Position> nextMove();
}
