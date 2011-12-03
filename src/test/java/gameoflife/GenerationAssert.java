package gameoflife;

import gameoflife.game.Generation;
import org.fest.assertions.GenericAssert;

public class GenerationAssert extends GenericAssert<GenerationAssert, Generation> {

    public GenerationAssert(Generation actual) {
        super(GenerationAssert.class, actual);
    }


    public void cellAtPointIsAlive(int x, int y) {

    }


    public GenerationAssert cellIsDeadAt(int x, int y) {
        isNotNull();
        if (!actual.isAliveAt(x, y)) return myself;
        failIfCustomMessageIsSet();
        throw failure("Expected dead cell at ( " + x + " / " + y + " )");
    }

    public GenerationAssert cellIsAlifeAt(int x, int y) {
        isNotNull();
        if (actual.isAliveAt(x, y)) return myself;
        failIfCustomMessageIsSet();
        throw failure("Expected living cell cell at ( " + x + " / " + y + " )");
    }
}
