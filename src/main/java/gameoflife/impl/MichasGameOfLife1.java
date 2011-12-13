package gameoflife.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import gameoflife.GameOfLife;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MichasGameOfLife1 implements GameOfLife {

    private static final Integer ZERO = 0;
    private static final Integer ALIVE = 9;
    private static final Integer DEAD_WITH_THREE_ALIVE_NEIGHBOURS = 3;
    private static final Integer ALIVE_WITH_TWO_ALIVE_NEIGHBOURS = ALIVE + 2;
    private static final Integer ALIVE_WITH_THREE_ALIVE_NEIGHBOURS = ALIVE + 3;

    private static class PointToIntegerMap extends HashMap<Point, Integer> {
        @Override
        public Integer get(Object key) {
            Integer cachedValue = super.get(key);
            return (cachedValue == null ? ZERO : cachedValue);
        }
    } 
    
    private PointToIntegerMap cells = new PointToIntegerMap();
    
    private final Predicate<? super Map.Entry<Point, Integer>> isAlive = new Predicate<Map.Entry<Point, Integer>>() {
        @Override
        public boolean apply(Map.Entry<Point, Integer> mapEntry) {
            int temp = mapEntry.getValue();
            return temp == DEAD_WITH_THREE_ALIVE_NEIGHBOURS || temp == ALIVE_WITH_TWO_ALIVE_NEIGHBOURS || temp == ALIVE_WITH_THREE_ALIVE_NEIGHBOURS;
        }
    };

    private final Function<Map.Entry<Point, Integer>, Point> getKey = new Function<Map.Entry<Point, Integer>, Point>() {
        @Override
        public Point apply(Map.Entry<Point, Integer> mapEntry) {
            return mapEntry.getKey();
        }
    };

    @Override
    public void setCellAlive(int x, int y) {
        cells.put(new Point(x, y), DEAD_WITH_THREE_ALIVE_NEIGHBOURS);
    }

    @Override
    public void calculateNextGeneration() {
        PointToIntegerMap nextGeneration = new PointToIntegerMap();
        Point temp = new Point();
        for (Point p : getCoordinatesOfAliveCells()) {
            nextGeneration.put(p, nextGeneration.get(p) + 9);
            temp.x = p.x - 1;
            temp.y = p.y - 1;
            nextGeneration.put((Point) temp.clone(), nextGeneration.get(temp) + 1);
            temp.x++;
            nextGeneration.put((Point) temp.clone(), nextGeneration.get(temp) + 1);
            temp.x++;
            nextGeneration.put((Point) temp.clone(), nextGeneration.get(temp) + 1);
            temp.y++;
            nextGeneration.put((Point) temp.clone(), nextGeneration.get(temp) + 1);
            temp.y++;
            nextGeneration.put((Point) temp.clone(), nextGeneration.get(temp) + 1);
            temp.x--;
            nextGeneration.put((Point) temp.clone(), nextGeneration.get(temp) + 1);
            temp.x--;
            nextGeneration.put((Point) temp.clone(), nextGeneration.get(temp) + 1);
            temp.y--;
            nextGeneration.put((Point) temp.clone(), nextGeneration.get(temp) + 1);
        }
        cells = nextGeneration;
    }

    @Override
    public Iterable<Point> getCoordinatesOfAliveCells() {
        return Iterables.transform(Iterables.filter(cells.entrySet(), isAlive), getKey);
    }
}
    