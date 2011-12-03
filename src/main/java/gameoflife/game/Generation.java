package gameoflife.game;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static gameoflife.util.Validate.isTrue;

public class Generation {

    private static int CELL_IS_ALIFE = 1;
    private static int CELL_IS_DEAD = 0;

    public static Generation createForWidthAndHeight(int width, int height) {
        return new Generation(width, height);
    }

    private final Set<Point> _livingCells = new HashSet<Point>();
    private final int _width;
    private final int _height;

    public Generation(int width, int height) {
        _width = width;
        _height = height;
    }

    public Generation next() {
        final Generation nextGeneration = new Generation(_width, _height);
        for (Point eachLivingCell : _livingCells) {
            final int numberOfLivingNeighbours = countLivingNeighbours(eachLivingCell);
            if (numberOfLivingNeighbours == 2 || numberOfLivingNeighbours == 3) {
                nextGeneration.setAlive(eachLivingCell.x, eachLivingCell.y);
            }
        }
        return nextGeneration;
    }

    private int countLivingNeighbours(Point point) {
        int result = 0;
        final Point neighBour = new Point();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                if (x != 0 || y != 0) {
                    neighBour.setLocation(point.x + x, point.y + y);
                    if (_livingCells.contains(neighBour)) {
                        result++;
                    }
                }
            }
        }
        return result;
    }


    public void addMatrix(int... matrix) {
        isTrue(matrix.length % (_width * _height) == 0);
        int currentX = 0;
        int currentY = 0;
        for (int each : matrix) {
            if (each == CELL_IS_ALIFE) {
                setAlive(currentX, currentY);
            }
            if (currentX > 0 && (currentX + 1) % _width == 0) {
                currentX = 0;
                currentY++;
            } else {
                currentX++;
            }
        }

    }


    public void setAlive(int x, int y) {
        _livingCells.add(new Point(x, y));
    }

    public boolean isAliveAt(int x, int y) {
        return _livingCells.contains(new Point(x, y));
    }
}
