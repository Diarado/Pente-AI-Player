package a5.logic;

import java.util.Objects;

/**
 * A position on the board.
 */
public class Position {

    /**
     * Represent the board position ({@code rowNo}, {@code colNo}).
     */
    final private int rowNo, colNo;

    public Position(int rowNo, int colNo) {
        this.rowNo = rowNo;
        this.colNo = colNo;
    }

    public int row() {
        return rowNo;
    }

    public int col() {
        return colNo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Position boardMove = (Position) o;
        return rowNo == boardMove.rowNo && colNo == boardMove.colNo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowNo, colNo);
    }

    @Override
    public String toString() {
        return "(" + rowNo + ", " + colNo + ")";
    }
}
