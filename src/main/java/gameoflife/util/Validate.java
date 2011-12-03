package gameoflife.util;

public class Validate {

    private Validate() {
        throw new AssertionError("Instantiation is not allowed.");
    }


    public static void notNull(Object o) {
        if (o == null) {
            throw new RuntimeException("Object may not be null.");
        }
    }

    public static void isTrue(boolean expression) {
        if (!expression) {
            throw new RuntimeException("Expression is not true.");
        }
    }
}
