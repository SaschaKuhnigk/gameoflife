package gameoflife;

import gameoflife.impl.*;
import gameoflife.templates.CellBlock;
import gameoflife.templates.Pattern;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Measure {

    public static void main(String... args) {
        try {
            measure(SaschasGameOfLife1.class);
            measure(MichasGameOfLife3.class);
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void measure(Class<? extends GameOfLife> gameOfLifeClass) throws Exception {
        // Warm up ...
        System.out.println("Warm up for " + gameOfLifeClass.getSimpleName() + " ...");
        GameOfLife gameOfLife = gameOfLifeClass.newInstance();
        init(gameOfLife, "linepuf.lif");
        for (int i = 0; i < 100; ++i) {
            gameOfLife.calculateNextGeneration();
        }
        gameOfLife = null;
        System.gc();
        gameOfLife = gameOfLifeClass.newInstance();
        init(gameOfLife, "linepuf.lif");
        System.out.println("Run " + gameOfLifeClass.getSimpleName() + " for 10 seconds ...");
        int n = 0;
        long t1 = System.nanoTime();
        long timeout = t1 + 10000000000L;
        long t2;
        do {
            gameOfLife.calculateNextGeneration();
            ++n;
        } while ((t2 = System.nanoTime()) < timeout);
        System.out.println(gameOfLifeClass.getSimpleName() + " calculated " + n + " generations in " + ((t2 - t1) + 500000)/1000000 + "ms -- " + (n * 1000000000.0) / (t2 - t1) + " generations/second");
    }
    
    private static void init(GameOfLife gameOfLife, String resourceFile) {
        final InputStream resourceAsStream = Measure.class.getClassLoader().getResourceAsStream(resourceFile);
        final Pattern pattern = new Pattern(new BufferedReader(new InputStreamReader(resourceAsStream)));
        for (int i = 0; i < pattern.getNumberOfCellBlocks(); ++i) {
            final CellBlock cellBlock = pattern.getCellBlock(i);
            for (Point p : cellBlock.getLivingCells()) {
                gameOfLife.setCellAlive(cellBlock.getXOffset()+ p.x, cellBlock.getYOffset() + p.y);
            }
        }
    }
}
