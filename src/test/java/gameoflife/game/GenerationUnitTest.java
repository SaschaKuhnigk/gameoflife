package gameoflife.game;

import org.junit.Test;

import static gameoflife.Assert.assertThat;
import static gameoflife.game.Generation.createForWidthAndHeight;

public class GenerationUnitTest {

    @Test
    public void testSetLivingCell() throws Exception {
        final Generation generation = createForWidthAndHeight(3, 3);
        assertThat(generation).cellIsDeadAt(0, 1);
        generation.setAlive(0, 1);
        assertThat(generation).cellIsAlifeAt(0, 1);
    }

    @Test
    public void testAddMatrix() {
        final Generation generation = createForWidthAndHeight(3, 3);
        generation.addMatrix(
                1, 0, 0,
                0, 1, 0,
                1, 0, 1
        );
        assertThat(generation)
                .cellIsAlifeAt(0, 0)
                .cellIsDeadAt(1, 0)
                .cellIsDeadAt(2, 0)
                .cellIsDeadAt(0, 1)
                .cellIsAlifeAt(1, 1)
                .cellIsDeadAt(2, 1)
                .cellIsAlifeAt(0, 2)
                .cellIsDeadAt(1, 2)
                .cellIsAlifeAt(2, 2);
    }

    @Test
    public void testToMatrix() {
        final Generation generation = createForWidthAndHeight(3, 3);
        int[] inputMatrix  = {
            1, 0, 0,
            0, 1, 0,
            1, 0, 1
        };
        generation.addMatrix(inputMatrix);
        int[] matrix = generation.toMatrix();
        assertThat(matrix).isEqualTo(inputMatrix);
    }

    @Test
    public void testThatLiveCellWithTwoNeighboursLive() {
        final Generation generation = createForWidthAndHeight(2, 3);
        setAliveCellsOn(generation,
                1, 0,
                1, 0,
                1, 0
        );

        final Generation nextGeneration = generation.next();
        assertThat(nextGeneration)
                .cellIsAlifeAt(1, 1);
    }

    @Test
    public void testThatLiveCellWithThreeNeighboursLive() {
        final Generation generation = createForWidthAndHeight(2, 3);
        setAliveCellsOn(generation,
                1, 1,
                1, 0,
                1, 0
        );

        final Generation nextGeneration = generation.next();
        assertThat(nextGeneration).cellIsAlifeAt(0, 1);
    }

    @Test
    public void testThatLiveCellWithFourNeighbourdDies() {
        final Generation generation = createForWidthAndHeight(2, 3);
        setAliveCellsOn(generation,
                1, 1,
                1, 0,
                1, 1
        );

        final Generation nextGeneration = generation.next();
        assertThat(nextGeneration).cellIsDeadAt(0, 1);
    }

    @Test
    public void testThatLiveCellDieOnUnderpopulation() {
        final Generation generation = new Generation(3, 3);
        setAliveCellsOn(generation,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0
        );
        final Generation nextGeneration = generation.next();
        assertThat(nextGeneration)
                .cellIsDeadAt(1, 0)
                .cellIsDeadAt(1, 2);
    }

    @Test
    public void testScenario1() throws Exception {
        final Generation generation = new Generation(3, 3);
        setAliveCellsOn(generation,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0
        );
        assertThat(generation).nextGenerationIs(
                0, 0, 0,
                1, 1, 1,
                0, 0, 0
        );
    }

    @Test
    public void testScenario2() throws Exception {
        final Generation generation = new Generation(3, 3);
        setAliveCellsOn(generation,
                1, 1, 0,
                1, 0, 0,
                0, 0, 1
        );
        assertThat(generation).nextGenerationIs(
                1, 1, 0,
                1, 0, 0,
                0, 0, 0
        );
    }

    @Test
    public void testScenario3() throws Exception {
        final Generation generation = new Generation(4, 4);
        setAliveCellsOn(generation,
                1, 1, 0, 0,
                1, 0, 1, 0,
                0, 0, 1, 0,
                0, 1, 0, 0
        );
        assertThat(generation).nextGenerationIs(
                1, 1, 0, 0,
                1, 0, 1, 0,
                0, 0, 1, 0,
                0, 0, 0, 0
        );
    }

    private void setAliveCellsOn(Generation generation, int... values) {
        generation.addMatrix(values);
    }
}
