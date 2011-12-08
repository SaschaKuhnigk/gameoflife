package gameoflife.game;

import java.util.*;

import static gameoflife.util.Validate.isTrue;

public class SaschaGenerationImpl1 implements Generation {

    private static final int CELL_IS_ALIFE = 1;
    private static final int NEIGHBOUR_COUNT = 8;

    private final Set<Point> _livingCells = new HashSet<Point>();
    private final Map<Point, Integer> _calculatedDeadCells = new HashMap<Point, Integer>();

    private final int _width;
    private final int _height;
    private final int _currentGeneration;

    public SaschaGenerationImpl1(int width, int height) {
        this(width, height, 0);
    }

    public SaschaGenerationImpl1(int width, int height, int currentGeneration) {
        _width = width;
        _height = height;
        _currentGeneration = currentGeneration;
    }

    @Override
    public SaschaGenerationImpl1 next() {
        final SaschaGenerationImpl1 nextGeneration = new SaschaGenerationImpl1(_width, _height, _currentGeneration + 1);
        for (Point eachLivingCell : _livingCells) {
            final List<Point> deadNeighbours = getDeadNeighBours(eachLivingCell);
            final int numberOfLivingNeighbours = NEIGHBOUR_COUNT - deadNeighbours.size();
            if (numberOfLivingNeighbours == 2 || numberOfLivingNeighbours == 3) {
                nextGeneration.setAlive(eachLivingCell.x, eachLivingCell.y);
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
                nextGeneration.setAlive(deadCell.x, deadCell.y);
            }
        }
        return nextGeneration;
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

    @Override
    public void setAlive(int x, int y) {
        _livingCells.add(new Point(x, y));
    }

    @Override
    public boolean isAliveAt(int x, int y) {
        return _livingCells.contains(new Point(x, y));
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
    public Set<Point> getLivingCells() {
        return _livingCells;
    }

    public String generationNumber() {
        return "" + _currentGeneration;
    }
}
