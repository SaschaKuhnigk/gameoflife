package gameoflife.game;

public interface Generation {

    Generation next();

    void setAlive(int x, int y);

    boolean isAliveAt(int x, int y);

    Iterable<Point> getLivingCells();
}
