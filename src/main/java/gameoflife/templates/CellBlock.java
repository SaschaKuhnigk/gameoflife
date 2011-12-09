package gameoflife.templates;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class CellBlock {
    private final int _readXOffset;
    private final int _readYOffset;
    private final List<Point> _livingCells = new ArrayList<Point>();

    public CellBlock(int readXOffset, int readYOffset) {
        _readXOffset = readXOffset;
        _readYOffset = readYOffset;
    }

    public void addCellAlive(int x, int y) {
        _livingCells.add(new Point(x, y));
    }

    public int getXOffset() {
        return _readXOffset;
    }

    public int getYOffset() {
        return _readYOffset;
    }

    public boolean isAlife(int x, int y) {
        return _livingCells.contains(new Point(x, y));
    }

    public List<Point> getLivingCells() {
        return _livingCells;
    }
}
