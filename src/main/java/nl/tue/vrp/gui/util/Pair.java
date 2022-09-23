package nl.tue.vrp.gui.util;

public class Pair<T extends Comparable<T>, U extends Comparable<U>> implements Comparable<Pair<T, U>> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(Pair<T, U> o) {
        if (this.first.equals(o.first)) {
            return this.second.compareTo(o.second);
        }
        return this.first.compareTo(o.first);
    }

    @Override
    public String toString() {
        return String.format("{%s,%s}", first, second);
    }
}
