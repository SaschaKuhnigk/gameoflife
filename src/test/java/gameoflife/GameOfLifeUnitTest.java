package gameoflife;

import gameoflife.impl.*;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class GameOfLifeUnitTest {

    @DataPoints
    public static GameOfLife[] getImplementationsToTest() {
        return new GameOfLife[] {
            new SaschasGameOfLife1(),
            new MichasGameOfLife1(),
            new MichasGameOfLife3()
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
            new Point(100002,100003),
            new Point(100003,100004),
            new Point(100004,100002),
            new Point(100004,100003),
            new Point(100004,100004)
        )));
    }
}