package gameoflife.impl;

import gameoflife.GameOfLife;
import org.junit.Test;

import static gameoflife.TestDataCreator.create;
import static gameoflife.TestDataCreator.generation;

public class GenerationPerformanceTest {

    @Test
    public void testPerformance() throws Exception {
        final int iterations = 100;
        final GameOfLife gameOfLife = create(generation(600, 600).initRandom());
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            gameOfLife.calculateNextGeneration();
        }
        final long timeInMs = System.currentTimeMillis() - startTime;
        System.out.println(timeInMs + " ms");
    }
}
