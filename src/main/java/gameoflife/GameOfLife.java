package gameoflife;

import java.awt.Point;

public interface GameOfLife {

    void setCellAlive(int x, int y);

    void calculateNextGeneration();

    Iterable<Point> getCoordinatesOfAliveCells();
}
