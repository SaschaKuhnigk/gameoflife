package gameoflife;

import gameoflife.impl.*;
import gameoflife.templates.CellBlock;
import gameoflife.templates.Pattern;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class GameOfLifeUnitTest {

    @DataPoints
    public static GameOfLife[] getImplementationsToTest() {
        return new GameOfLife[] {
            new SaschasGameOfLife1(),
            new MichasGameOfLife1(),
            new MichasGameOfLife3(),
            new MichasGameOfLife4(),
            new MichasGameOfLife5()
        };
    }

    @Theory
    public void test_that_there_are_no_alive_cells_initally(GameOfLife gameOfLife) {
        assertThat(gameOfLife.getCoordinatesOfAliveCells().iterator().hasNext(), is(false));
    }

    @Theory
    public void test_setCellAlive(GameOfLife gameOfLife) {
        gameOfLife.setCellAlive(1, 2);

        HashSet<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());

        assertThat(coordinatesOfAliveCells, is(newHashSet(new Point(1, 2))));
    }

    @Theory
    public void test_setCellAlive_with_negative_x_coordinate(GameOfLife gameOfLife) {
        gameOfLife.setCellAlive(-3, 4);

        HashSet<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());

        assertThat(coordinatesOfAliveCells, is(newHashSet(new Point(-3, 4))));
    }

    @Theory
    public void test_setCellAlive_with_large_coordinates(GameOfLife gameOfLife) {
        gameOfLife.setCellAlive(-123456789, 987654321);

        HashSet<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());

        assertThat(coordinatesOfAliveCells, is(newHashSet(new Point(-123456789, 987654321))));
    }

    @Theory
    public void test_getCoordinatesOfAliveCells(GameOfLife gameOfLife) {
        gameOfLife.setCellAlive(1, 2);
        gameOfLife.setCellAlive(-3, 4);

        HashSet<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());

        assertThat(coordinatesOfAliveCells, is(newHashSet(new Point(1, 2), new Point(-3, 4))));
    }

    @Theory
    public void test_that_an_alive_cell_with_fewer_than_two_live_neighbours_dies(GameOfLife gameOfLife) {
        // two alive cells, each with one alive neighbour ...
        gameOfLife.setCellAlive(0, 0);
        gameOfLife.setCellAlive(1, 0);
        // one alive cells without any alive neighbours ...
        gameOfLife.setCellAlive(3, 3);

        gameOfLife.calculateNextGeneration();
        
        assertThat(gameOfLife.getCoordinatesOfAliveCells().iterator().hasNext(), is(false));
    }

    @Theory
    public void test_that_an_alive_cell_with_two_live_neighbours_stays_alive(GameOfLife gameOfLife) {
        gameOfLife.setCellAlive(0, 0);
        gameOfLife.setCellAlive(1, 0);
        gameOfLife.setCellAlive(0, 1);

        gameOfLife.calculateNextGeneration();

        Set<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());
        assertThat(coordinatesOfAliveCells, hasItems(new Point(0, 0), new Point(1, 0), new Point(0, 1)));
    }

    @Theory
    public void test_that_an_alive_cell_with_three_live_neighbours_stays_alive(GameOfLife gameOfLife) {
        gameOfLife.setCellAlive(0, 0);
        gameOfLife.setCellAlive(1, 0);
        gameOfLife.setCellAlive(0, 1);
        gameOfLife.setCellAlive(1, 1);

        gameOfLife.calculateNextGeneration();

        Set<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());
        assertThat(coordinatesOfAliveCells, hasItems(new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1)));
    }

    @Theory
    public void test_that_an_alive_cell_with_four_live_neighbours_dies(GameOfLife gameOfLife) {
        gameOfLife.setCellAlive(0, 0);
        gameOfLife.setCellAlive(1, 0);
        gameOfLife.setCellAlive(0, 1);
        gameOfLife.setCellAlive(1, 1);
        gameOfLife.setCellAlive(2, 2);

        gameOfLife.calculateNextGeneration();

        Set<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());
        assertThat(coordinatesOfAliveCells, not(hasItems(new Point(1, 1))));
    }

    @Theory
    public void test_that_a_dead_cell_with_three_live_neighbours_becomes_alive(GameOfLife gameOfLife) {
        gameOfLife.setCellAlive(0, 0);
        gameOfLife.setCellAlive(1, 0);
        gameOfLife.setCellAlive(0, 1);

        gameOfLife.calculateNextGeneration();

        Set<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());
        assertThat(coordinatesOfAliveCells, hasItems(new Point(1, 1)));
    }

    @Theory
    public void test_pentadecathlon(GameOfLife gameOfLife) {
        Set<Point> pentadecathlon = newHashSet(
            new Point(0,1), new Point(1,1), new Point(2,0), new Point(2,2),
            new Point(3,1), new Point(4,1), new Point(5,1), new Point(6,1),
            new Point(7,0), new Point(7,2), new Point(8,1), new Point(9,1)
        );
        for (Point p : pentadecathlon) {
            gameOfLife.setCellAlive(p.x, p.y);
        }

        for (int i = 0; i < 15; ++i) {
            gameOfLife.calculateNextGeneration();
        }

        Set<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());
        assertThat(coordinatesOfAliveCells, is(pentadecathlon));
    }

    @Theory
    public void test_glider_at_large_coordinates(GameOfLife gameOfLife) {
        Set<Point> glider = newHashSet(
            new Point(100001,100002),
            new Point(100002,100003),
            new Point(100003,100001),
            new Point(100003,100002),
            new Point(100003,100003)
        );
        for (Point p : glider) {
            gameOfLife.setCellAlive(p.x, p.y);
        }

        for (int i = 0; i < 4; ++i) {
            gameOfLife.calculateNextGeneration();
        }

        HashSet<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());
        assertThat(coordinatesOfAliveCells, is(newHashSet(
                new Point(100002, 100003),
                new Point(100003, 100004),
                new Point(100004, 100002),
                new Point(100004, 100003),
                new Point(100004, 100004)
        )));
    }

    @Theory
    public void test_that_the_world_is_a_torus(GameOfLife gameOfLife) {
        gameOfLife.setCellAlive(Integer.MIN_VALUE, Integer.MIN_VALUE);
        gameOfLife.setCellAlive(Integer.MIN_VALUE, Integer.MAX_VALUE);
        gameOfLife.setCellAlive(Integer.MAX_VALUE, Integer.MIN_VALUE);
        gameOfLife.setCellAlive(Integer.MAX_VALUE, Integer.MAX_VALUE);

        gameOfLife.calculateNextGeneration();

        HashSet<Point> coordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());
        assertThat(coordinatesOfAliveCells, is(newHashSet(
                new Point(Integer.MIN_VALUE, Integer.MIN_VALUE),
                new Point(Integer.MIN_VALUE, Integer.MAX_VALUE),
                new Point(Integer.MAX_VALUE, Integer.MIN_VALUE),
                new Point(Integer.MAX_VALUE, Integer.MAX_VALUE)
        )));
    }

    private static Set<Point> _1000th_generation_of_linepuf_pattern_;
    
    @Theory
    public void test_linepuf_pattern_after_1000_generations(GameOfLife gameOfLife) {
        assumeThat(gameOfLife, is(not(instanceOf(SaschasGameOfLife1.class))));
        if (_1000th_generation_of_linepuf_pattern_ == null) {
            GameOfLife referenceGameOfLife = new SaschasGameOfLife1();
            loadLifFile(referenceGameOfLife, "linepuf.lif");
            for (int i = 1; i <= 1000; ++i) {
                referenceGameOfLife.calculateNextGeneration();
            }
            _1000th_generation_of_linepuf_pattern_ = newHashSet(referenceGameOfLife.getCoordinatesOfAliveCells());
        }        
        loadLifFile(gameOfLife, "linepuf.lif");
        for (int i = 1; i <= 1000; ++i) {
            gameOfLife.calculateNextGeneration();
        }
        HashSet<Point> actualCoordinatesOfAliveCells = newHashSet(gameOfLife.getCoordinatesOfAliveCells());
        assertThat(actualCoordinatesOfAliveCells, is(_1000th_generation_of_linepuf_pattern_));
    }

    private void loadLifFile(GameOfLife gameOfLife, String resourceFile) {
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourceFile);
        final Pattern pattern = new Pattern(new BufferedReader(new InputStreamReader(resourceAsStream)));
        for (int i = 0; i < pattern.getNumberOfCellBlocks(); ++i) {
            final CellBlock cellBlock = pattern.getCellBlock(i);
            for (Point p : cellBlock.getLivingCells()) {
                gameOfLife.setCellAlive(cellBlock.getXOffset()+ p.x, cellBlock.getYOffset() + p.y);
            }
        }
    }
}