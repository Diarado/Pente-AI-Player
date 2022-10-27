package a5.ai;

import cms.util.maybe.Maybe;

/**
 * A transposition table for an arbitrary game. It maps a game state
 * to a search depth and a heuristic evaluation of that state to the
 * recorded depth. Unlike a conventional map abstraction, a state is
 * associated with a depth, so that clients can look for states whose
 * entry has at least the desired depth.
 *
 * @param <GameState> A type representing the state of a game.
 */
public class TranspositionTable<GameState> {

    /**
     * Information about a game state, for use by clients.
     */
    public interface StateInfo {

        /**
         * The heuristic value of this game state.
         */
        int value();

        /**
         * The depth to which the game tree was searched to determine the value.
         */
        int depth();
    }

    /**
     * A Node is a node in a linked list of nodes for a chaining-based implementation of a hash
     * table.
     *
     * @param <GameState>
     */
    static private class Node<GameState> implements StateInfo {
        /**
         * The state
         */
        GameState state;
        /**
         * The depth of this entry. >= 0
         */
        int depth;
        /**
         * The value of this entry.
         */
        int value;
        /**
         * The next node in the list. May be null.
         */
        Node<GameState> next;

        Node(GameState state, int depth, int value, Node<GameState> next) {
            this.state = state;
            this.depth = depth;
            this.value = value;
            this.next = next;
        }

        public int value() {
            return value;
        }

        public int depth() {
            return depth;
        }
    }

    /**
     * The number of entries in the transposition table.
     */
    private int size;

    /**
     * The buckets array may contain null elements.
     * Class invariant:
     * All transposition table entries are found in the linked list of the
     * bucket to which they hash, and the load factor is no more than 1.
     */
    private Node<GameState>[] buckets;

    boolean classInv() {
        for (int i = 0; i < buckets.length; i++){
            Node<GameState> node = buckets[i];
            while(node != null){
                if(i != Math.abs(node.state.hashCode()) % buckets.length){
                    return false;
                }
                node = node.next;
            }
        }
        return size/buckets.length <= 1;
    }

    @SuppressWarnings("unchecked")
    /** Creates: a new, empty transposition table. */
    TranspositionTable() {
        size = 0;
        buckets = (Node<GameState>[]) new Node[5];
    }

    /** The number of entries in the transposition table. */
    public int size() {
        return size;
    }

    public int length(){return buckets.length;}

    /**
     * Returns: the information in the transposition table for a given
     * game state, package in an Optional. If there is no information in
     * the table for this state, returns an empty Optional.
     */
    public Maybe<StateInfo> getInfo(GameState state) {
        if (size == 0){
            return Maybe.none();
        }
        int index = Math.abs(state.hashCode()) % buckets.length;
        Node<GameState> node = buckets[index];
        while(node != null){
            if(node.state.equals(state)){
                return Maybe.some(node);
            }
            node = node.next;
        }
        return Maybe.none();
    }

    /**
     * Effect: Add a new entry in the transposition table for a given
     * state and depth, or overwrite the existing entry for this state
     * with the new state and depth. Requires: if overwriting an
     * existing entry, the new depth must be greater than the old one.
     */
    public void add(GameState state, int depth, int value) {
        int index = Math.abs(state.hashCode()) % buckets.length;
        Node node = buckets[index];
        Node p = null;
        while(node != null){
            if(node.state.equals(state)){
                node.depth = depth;
                node.value = value;
                return;
            }
            p = node;
            node = node.next;
        }

        size ++;

        if(!grow(size)){
            Node newNode = new Node<>(state, depth, value, null);
            if(p == null){
                buckets[index] = newNode;
            }
            else{
                p.next = newNode;
            }
        }
        else{
            index = Math.abs(state.hashCode()) % buckets.length;
            node = buckets[index];
            p = buckets[index];
            while(node != null){
                p = node;
                node = node.next;
            }
            if(p == null){
                buckets[index] = new Node<>(state, depth, value, null);
            }
            else{
                p.next = new Node<>(state, depth, value, null);
            }
        }

    }

    /**
     * Effect: Make sure the hash table has at least {@code target} buckets.
     * Returns true if the hash table actually resized.
     */
    private boolean grow(int target) {
        // TODO 5
        if (buckets.length >= target){
            return false;
        }
        else{
            Node<GameState>[] newBuckets = new Node[2*buckets.length];
            for(Node<GameState> node : buckets){
                while(node != null){
                    int index = Math.abs(node.state.hashCode()) % newBuckets.length;
                    Node<GameState> n = newBuckets[index];
                    Node<GameState> p = newBuckets[index];
                    Node<GameState> toAdd = new Node<>(node.state, node.depth, node.value, null);
                    while(n != null){
                        p = n;
                        n = n.next;
                    }
                    if(p == null){
                        newBuckets[index] = toAdd;
                    }
                    else{
                        p.next = toAdd;
                    }
                    node = node.next;
                }
            }
            buckets = newBuckets;
            return true;
        }
    }



    /**
     * Estimate clustering. With a good hash function, clustering
     * should be around 1.0. Higher values of clustering lead to worse
     * performance.
     */
    double estimateClustering() {
        final int N = 500;
        int m = buckets.length, n = size;
        double sum2 = 0;
        for (int i = 0; i < N; i++) {
            int j = Math.abs((i * 82728353) % buckets.length);
            int count = 0;
            Node<GameState> node = buckets[j];
            while (node != null) {
                count++;
                node = node.next;
            }
            sum2 += count*count;
        }
        double alpha = (double)n/m;
        return sum2/(N * alpha * (1 - 1.0/m + alpha));
    }
}

