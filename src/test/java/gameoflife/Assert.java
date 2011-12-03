package gameoflife;

import gameoflife.game.Generation;

public class Assert {

    public static GenerationAssert assertThat(Generation actual) {
        return new GenerationAssert(actual);
    }


}
