package gameoflife.impl;

import gameoflife.GameOfLife;
import gameoflife.impl.SparseTwoDimensionalIntArray.Element;
import gameoflife.impl.SparseTwoDimensionalIntArray.Visitor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MichasGameOfLife3 implements GameOfLife {

    private SparseTwoDimensionalIntArray cells = new SparseTwoDimensionalIntArray();

    @Override
    public void setCellAlive(int x, int y) {
        cells.set(x, y, 3);
    }

    @Override
    public void calculateNextGeneration() {
        final SparseTwoDimensionalIntArray nextGeneration = new SparseTwoDimensionalIntArray();
        cells.visitNonZeroValues(new Visitor() {
            @Override
            public void visit(Element e) {
                int v;
                if ((v = e.value) == 3 || v == 11 || v == 12) {
                    int x = e.x;
                    int y = e.y;
                    nextGeneration.add(x, y, 9);
                    nextGeneration.add(x, --y, 1);
                    nextGeneration.add(++x, y, 1);
                    nextGeneration.add(x, ++y, 1);
                    nextGeneration.add(x, ++y, 1);
                    nextGeneration.add(--x, y, 1);
                    nextGeneration.add(--x, y, 1);
                    nextGeneration.add(x, --y, 1);
                    nextGeneration.add(x, --y, 1);
                }
            }
        });
        cells = nextGeneration;
    }

    @Override
    public Iterable<Point> getCoordinatesOfAliveCells() {
        final List<Point> result = new ArrayList<Point>();
        cells.visitNonZeroValues(new Visitor() {
            @Override
            public void visit(Element e) {
                int v;
                if ((v = e.value) == 3 || v == 11 || v == 12) {
                    result.add(new Point(e.x, e.y));
                }
            }
        });
        return result;
    }
}
    