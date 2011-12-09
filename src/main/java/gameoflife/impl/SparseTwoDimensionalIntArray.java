package gameoflife.impl;

/**
 * Based on {@code HashMap}.
 */
public final class SparseTwoDimensionalIntArray {

    public interface Visitor {
        public void visit(Element e);
    }

    private static final float LOAD_FACTOR = 0.75f;
    /** MUST be a power of two. */
    private static final int INITIAL_CAPACITY = 4096;
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    int threshold;
    transient Element[] table;
    transient int size;

    public SparseTwoDimensionalIntArray() {
        threshold = (int)(INITIAL_CAPACITY * LOAD_FACTOR);
        table = new Element[INITIAL_CAPACITY];
    }

    public int get(int x, int y) {
        final int bucketIndex = bucketIndexFor(keyFor(x, y));
        for (Element e = table[bucketIndex]; e != null; e = e.next) {
            if (e.x == x && e.y == y) {
                return e.value;
            }
        }
        return 0;
    }

    public void set(int x, int y, int value) {
        int bucketIndex = bucketIndexFor(keyFor(x, y));
        for (Element e = table[bucketIndex]; e != null; e = e.next) {
            if (e.x == x && e.y == y) {
                e.value = value;
                return;
            }
        }
        addEntry(bucketIndex, x, y, value);
    }

    public void add(int x, int y, int summand) {
        int bucketIndex = bucketIndexFor(keyFor(x, y));
        for (Element e = table[bucketIndex]; e != null; e = e.next) {
            if (e.x == x && e.y == y) {
                e.value += summand;
                return;
            }
        }
        addEntry(bucketIndex, x, y, summand);
    }

    private static int keyFor(int x, int y) {
        return (x ^ y);
    }

    private static int bucketIndexFor(int key, int capacity) {
        return key & (capacity - 1);
    }

    private int bucketIndexFor(int key) {
        return key & (table.length - 1);
    }

    private void addEntry(int bucketIndex, int x, int y, int value) {
        Element e = table[bucketIndex];
        table[bucketIndex] = new Element(x, y, value, e);
        if (size++ >= threshold) {
            resize(2 * table.length);
        }
    }

    private void resize(int newCapacity) {
        int oldCapacity = table.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
        Element[] newTable = new Element[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int)(newCapacity * LOAD_FACTOR);
    }

    private void transfer(Element[] newTable) {
        Element[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Element e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Element next = e.next;
                    int bucketIndex = bucketIndexFor(keyFor(e.x, e.y), newCapacity);
                    e.next = newTable[bucketIndex];
                    newTable[bucketIndex] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    public static final class Element {
        public final int x;
        public final int y;
        public int value;
        private Element next;

        Element(int x, int y, int v, Element n) {
            this.x = x;
            this.y = y;
            value = v;
            next = n;
        }
    }

    public void visitNonZeroValues(Visitor visitor) {
        for (Element head : table) {
            Element e = head;
            while (e != null) {
                int v = e.value;
                if (v == 0) {
                    // TODO: we can safely remove this element
                } else {
                    visitor.visit(e);
                    e = e.next;
                }
            }
        }
    }
}
