package gameoflife.game;

import org.junit.Test;

import static gameoflife.Assert.assertThat;
import static gameoflife.TestDataCreator.*;


public class GenerationUnitTest {

    @Test
    public void testSetLivingCell() throws Exception {
        final Generation generation = create(generation(3, 3));
        assertThat(generation).hasDeadCellAt(0, 1);
        generation.setAlive(0, 1);
        assertThat(generation).hasLivingCellAt(0, 1);
    }

    @Test
    public void testAddMatrix() {
        final Generation generation = create(generation(3, 3).withCells(
                1, 0, 0,
                0, 1, 0,
                1, 0, 1
        ));
        assertThat(generation)
                .hasLivingCellAt(0, 0)
                .hasLivingCellAt(1, 1)
                .hasLivingCellAt(0, 2)
                .hasLivingCellAt(2, 2)
                .hasDeadCellAt(1, 0)
                .hasDeadCellAt(2, 0)
                .hasDeadCellAt(2, 1)
                .hasDeadCellAt(0, 1)
                .hasDeadCellAt(1, 2);
    }

    @Test
    public void testToMatrix() {
        int[] inputMatrix  = {
            1, 0, 0,
            0, 1, 0,
            1, 0, 1
        };
        final Generation generation = create(generation(3, 3).withCells(inputMatrix));
        int[] matrix = generation.toMatrix();
        assertThat(matrix).isEqualTo(inputMatrix);
    }

    @Test
    public void testThatLiveCellWithTwoNeighboursLive() {
        final Generation generation = create(generation(2, 3).withCells(
                1, 0,
                1, 0,
                1, 0
        ));
        assertThat(next(generation)).hasLivingCellAt(1, 1);
    }

    @Test
    public void testThatLiveCellWithThreeNeighboursLive() {
        final Generation generation = create(generation(2, 3).withCells(
                1, 1,
                1, 0,
                1, 0
        ));
        assertThat(next(generation)).hasLivingCellAt(0, 1);
    }

    @Test
    public void testThatLiveCellWithFourNeighboursDies() {
        final Generation generation = create(generation(2, 3).withCells(
                1, 1,
                1, 0,
                1, 1
        ));
        assertThat(next(generation)).hasDeadCellAt(0, 1);
    }

    @Test
    public void testThatLiveCellDieOnUnderpopulation() {
        final Generation generation = create(generation(3, 3).withCells(
                0, 1, 0,
                0, 1, 0,
                0, 1, 0
        ));
        assertThat(next(generation))
                .hasDeadCellAt(1, 0)
                .hasDeadCellAt(1, 2);
    }

    @Test
    public void testScenario1() throws Exception {
        final Generation generation = create(generation(3, 3).withCells(
                0, 1, 0,
                0, 1, 0,
                0, 1, 0
        ));
        assertThat(next(generation)).hasCells(
                0, 0, 0,
                1, 1, 1,
                0, 0, 0
        );
    }

    @Test
    public void testScenario2() throws Exception {
        final Generation generation = create(generation(3, 3).withCells(
                1, 1, 0,
                1, 0, 0,
                0, 0, 1
        ));
        assertThat(next(generation)).hasCells(
                1, 1, 0,
                1, 0, 0,
                0, 0, 0
        );
    }

    @Test
    public void testScenario3() throws Exception {
        final Generation generation = create(generation(4, 4).withCells(
                1, 1, 0, 0,
                1, 0, 1, 0,
                0, 0, 1, 0,
                0, 1, 0, 0
        ));
        assertThat(next(generation)).hasCells(
                1, 1, 0, 0,
                1, 0, 1, 0,
                0, 0, 1, 0,
                0, 0, 0, 0
        );
    }

    private Generation next(Generation generation) {
        return generation.next();
    }
}
