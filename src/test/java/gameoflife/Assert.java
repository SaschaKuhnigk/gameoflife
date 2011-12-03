package gameoflife;

import gameoflife.game.Generation;
import org.fest.assertions.Assertions;
import org.fest.assertions.IntArrayAssert;

public class Assert {

    public static GenerationAssert assertThat(Generation actual) {
        return new GenerationAssert(actual);
    }

    public static IntArrayAssert assertThat(int[] actual) {
      return Assertions.assertThat(actual);
    }

}
