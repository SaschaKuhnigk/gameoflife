package gameoflife.impl;

import gameoflife.GameOfLife;

import java.util.*;

import static gameoflife.util.Validate.isTrue;
import java.awt.Point;

public class SaschasGameOfLife1 implements GameOfLife {

    private static final int CELL_IS_ALIVE = 1;
    private static final int NEIGHBOUR_COUNT = 8;

    private final int _width;
    private final int _height;

    private int _currentGeneration;
    private Set<Point> _livingCells = new HashSet<Point>();
    private Map<Point, Integer> _calculatedDeadCells = new HashMap<Point, Integer>();

    public SaschasGameOfLife1() {
        this(0,0);
    }

    public SaschasGameOfLife1(int width, int height) {
        _width = width;
        _height = height;
        _currentGeneration = 0;
    }

    @Override
    public void setCellAlive(int x, int y) {
        _livingCells.add(new Point(x, y));
    }

    public boolean isCellAlive(int x, int y) {
        return _livingCells.contains(new Point(x, y));
    }

    @Override
    public void calculateNextGeneration() {
        final SaschasGameOfLife1 nextGeneration = new SaschasGameOfLife1(_width, _height);
        for (Point eachLivingCell : _livingCells) {
            final List<Point> deadNeighbours = getDeadNeighBours(eachLivingCell);
            final int numberOfLivingNeighbours = NEIGHBOUR_COUNT - deadNeighbours.size();
            if (numberOfLivingNeighbours == 2 || numberOfLivingNeighbours == 3) {
                nextGeneration.setCellAlive(eachLivingCell.x, eachLivingCell.y);
            }
            for (Point deadNeighbour : deadNeighbours) {
                Integer numberOfAliveNeighbours = _calculatedDeadCells.get(deadNeighbour);
                if (numberOfAliveNeighbours == null) {
                    numberOfAliveNeighbours = 0;
                }
                numberOfAliveNeighbours++;
                _calculatedDeadCells.put(deadNeighbour, numberOfAliveNeighbours);
            }
        }
        for (Map.Entry<Point, Integer> pointIntegerEntry : _calculatedDeadCells.entrySet()) {
            final Point deadCell = pointIntegerEntry.getKey();
            final Integer numberOfNeighbours = pointIntegerEntry.getValue();
            if (numberOfNeighbours == 3) {
                nextGeneration.setCellAlive(deadCell.x, deadCell.y);
            }
        }
        // Quick fix (previously this method returned nextGeneration
        _livingCells = nextGeneration._livingCells;
        _calculatedDeadCells = nextGeneration._calculatedDeadCells;
        _currentGeneration++;
    }

    private List<Point> getDeadNeighBours(Point cell) {
        final List<Point> result = new ArrayList<Point>();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                if (x != 0 || y != 0) {
                    final Point neighbour = new Point();
                    neighbour.setLocation(cell.x + x, cell.y + y);
                    if (!_livingCells.contains(neighbour)) {
                        result.add(neighbour);
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
            if (each == CELL_IS_ALIVE) {
                setCellAlive(currentX, currentY);
            }
            if (currentX > 0 && (currentX + 1) % _width == 0) {
                currentX = 0;
                currentY++;
            } else {
                currentX++;
            }
        }
    }

    public int[] toMatrix() {
        final int[] result = new int[_width * _height];
        int pointer = 0;
        for (int y = 0; y < _width; ++y) {
            for (int x = 0; x < _height; ++x) {
                result[pointer] = _livingCells.contains(new Point(x, y)) ? 1 : 0;
                pointer++;
            }
        }

        return result;
    }

    @Override
    public Set<Point> getCoordinatesOfAliveCells() {
        return _livingCells;
    }

    public String generationNumber() {
        return Integer.toString(_currentGeneration);
    }
}
