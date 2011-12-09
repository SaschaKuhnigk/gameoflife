package gameoflife;

import gameoflife.impl.SaschasGameOfLife1;
import org.fest.assertions.Assertions;
import org.fest.assertions.IntArrayAssert;

public class Assert {

    public static GenerationAssert assertThat(SaschasGameOfLife1 actual) {
        return new GenerationAssert(actual);
    }

    public static IntArrayAssert assertThat(int[] actual) {
      return Assertions.assertThat(actual);
    }

}
