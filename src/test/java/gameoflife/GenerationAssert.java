package gameoflife;

import gameoflife.game.Generation;
import org.fest.assertions.GenericAssert;

import java.util.Arrays;

import static java.util.Arrays.toString;

public class GenerationAssert extends GenericAssert<GenerationAssert, Generation> {

    public GenerationAssert(Generation actual) {
        super(GenerationAssert.class, actual);
    }

    public GenerationAssert hasDeadCellAt(int x, int y) {
        isNotNull();
        if (!actual.isAliveAt(x, y)) return myself;
        failIfCustomMessageIsSet();
        throw failure("Expected dead cell at ( " + x + " / " + y + " )");
    }

    public GenerationAssert hasLivingCellAt(int x, int y) {
        isNotNull();
        if (actual.isAliveAt(x, y)) return myself;
        failIfCustomMessageIsSet();
        throw failure("Expected living cell cell at ( " + x + " / " + y + " )");
    }

    public GenerationAssert hasCells(int... matrix) {
        isNotNull();
        final int[] actualMatrix = actual.toMatrix();
        if (Arrays.equals(actualMatrix, matrix)) return myself;
        failIfCustomMessageIsSet();
        throw failure("Next generation should be '" + Arrays.toString(matrix)  + " but is: " + Arrays.toString(actualMatrix) + "'.");
    }
}
