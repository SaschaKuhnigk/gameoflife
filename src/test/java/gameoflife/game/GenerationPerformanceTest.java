package gameoflife.game;

import gameoflife.TestDataCreator;
import org.junit.Test;

import static gameoflife.TestDataCreator.create;
import static gameoflife.TestDataCreator.generation;

public class GenerationPerformanceTest {

    @Test
    public void testPerformance() throws Exception {
        final int iterations = 100;
        final Generation generation = create(generation(600, 600).initRandom());

        Generation currentGeneration = generation.next();
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            currentGeneration = currentGeneration.next();
        }
        final long timeInMs = System.currentTimeMillis() - startTime;
        System.out.println(timeInMs + " ms");

    }
}
