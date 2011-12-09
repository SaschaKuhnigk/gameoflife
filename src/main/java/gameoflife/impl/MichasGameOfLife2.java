package gameoflife.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import gameoflife.GameOfLife;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MichasGameOfLife2 implements GameOfLife {

    private static final Integer ZERO = 0;
    private static final Integer ALIVE = 9;
    private static final Integer DEAD_WITH_THREE_ALIVE_NEIGHBOURS = 3;
    private static final Integer ALIVE_WITH_TWO_ALIVE_NEIGHBOURS = ALIVE + 2;
    private static final Integer ALIVE_WITH_THREE_ALIVE_NEIGHBOURS = ALIVE + 3;

    private static Integer keyFor(int x, int y) {
        return x << 16 | (y & 0xffff);
    }
    
    private static class IntegerToIntegerMap extends HashMap<Integer, Integer> {
        @Override
        public Integer get(Object key) {
            Integer cachedValue = super.get(key);
            return (cachedValue == null ? ZERO : cachedValue);
        }
    }

    private IntegerToIntegerMap cells = new IntegerToIntegerMap();

    private final Predicate<? super Map.Entry<Integer, Integer>> isAlive = new Predicate<Map.Entry<Integer, Integer>>() {
        @Override
        public boolean apply(Map.Entry<Integer, Integer> mapEntry) {
            int temp = mapEntry.getValue();
            return temp == DEAD_WITH_THREE_ALIVE_NEIGHBOURS || temp == ALIVE_WITH_TWO_ALIVE_NEIGHBOURS || temp == ALIVE_WITH_THREE_ALIVE_NEIGHBOURS;
        }
    };

    private final Function<Map.Entry<Integer, Integer>, Point> getPoint = new Function<Map.Entry<Integer, Integer>, Point>() {
        @Override
        public Point apply(Map.Entry<Integer, Integer> mapEntry) {
            int temp = mapEntry.getKey();
            return new Point(temp >> 16, temp & 0xffff);
        }
    };

    @Override
    public void setCellAlive(int x, int y) {
        cells.put(keyFor(x, y), DEAD_WITH_THREE_ALIVE_NEIGHBOURS);
    }

    @Override
    public boolean isCellAlive(int x, int y) {
        int temp = cells.get(keyFor(x, y));
        return temp == 3 || temp == 11 || temp == 12;
    }

    @Override
    public void calculateNextGeneration() {
        IntegerToIntegerMap nextGeneration = new IntegerToIntegerMap();
        for (Map.Entry<Integer, Integer> mapEntry : cells.entrySet()) {
            int temp = mapEntry.getValue();
            if (temp == 3 || temp == 11 || temp == 12) {
                temp = mapEntry.getKey();
                nextGeneration.put(temp, nextGeneration.get(temp) + 9);
                int x = temp >> 16;
                int y = temp & 0xffff;
                --y;
                Integer key = keyFor(x, y);
                nextGeneration.put(key, nextGeneration.get(key) + 1);
                key = keyFor(++x, y);
                nextGeneration.put(key, nextGeneration.get(key) + 1);
                key = keyFor(x, ++y);
                nextGeneration.put(key, nextGeneration.get(key) + 1);
                key = keyFor(x, ++y);
                nextGeneration.put(key, nextGeneration.get(key) + 1);
                key = keyFor(--x, y);
                nextGeneration.put(key, nextGeneration.get(key) + 1);
                key = keyFor(--x, y);
                nextGeneration.put(key, nextGeneration.get(key) + 1);
                key = keyFor(x, --y);
                nextGeneration.put(key, nextGeneration.get(key) + 1);
                key = keyFor(x, --y);
                nextGeneration.put(key, nextGeneration.get(key) + 1);
            }
        }
        cells = nextGeneration;
    }

    @Override
    public Iterable<Point> getCoordinatesOfAliveCells() {
        return Iterables.transform(Iterables.filter(cells.entrySet(), isAlive), getPoint);
    }
}
    