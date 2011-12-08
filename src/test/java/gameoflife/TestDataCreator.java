package gameoflife;

import gameoflife.game.Generation;
import gameoflife.game.SaschaGenerationImpl1;

public class TestDataCreator {

    public static interface Builder<T> {
        public T build();
    }

    public static <T> T create(Builder<T> builder) {
        return builder.build();
    }

    public static GenerationBuilder generation(int width, int height) {
        return new GenerationBuilder(width, height);
    }

    public static class GenerationBuilder implements Builder<SaschaGenerationImpl1> {

        private final int width;
        private final int height;
        private int[] _matrix;
        private boolean _initRandom;

        public GenerationBuilder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Builder<SaschaGenerationImpl1> withCells(int... matrix) {
            _matrix = matrix;
            return this;
        }

        public Builder<SaschaGenerationImpl1> initRandom() {
            _initRandom = true;
            return this;
        }

        public SaschaGenerationImpl1 build() {
            final SaschaGenerationImpl1 result = new SaschaGenerationImpl1(width, height);
            if (_matrix != null) {
                result.addMatrix(_matrix);
            } else if (_initRandom) {
                initRandom(result);
            }
            return result;
        }

        private void initRandom(Generation generation) {
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    final double v = Math.random() * 2;
                    if (v > 1.7) {
                        generation.setAlive(x, y);
                    }
                }
            }
        }

    }
}
