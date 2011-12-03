package gameoflife.game;

public class Point extends java.awt.Point {

    public Point() {
    }

    public Point(int x, int y) {
        super(x, y);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point pt = (Point) obj;
            return (x == pt.x) && (y == pt.y);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
