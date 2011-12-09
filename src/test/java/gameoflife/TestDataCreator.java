package gameoflife;

import gameoflife.impl.SaschasGameOfLife1;

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

    public static class GenerationBuilder implements Builder<SaschasGameOfLife1> {

        private final int width;
        private final int height;
        private int[] _matrix;
        private boolean _initRandom;

        public GenerationBuilder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Builder<SaschasGameOfLife1> withCells(int... matrix) {
            _matrix = matrix;
            return this;
        }

        public Builder<SaschasGameOfLife1> initRandom() {
            _initRandom = true;
            return this;
        }

        public SaschasGameOfLife1 build() {
            final SaschasGameOfLife1 result = new SaschasGameOfLife1(width, height);
            if (_matrix != null) {
                result.addMatrix(_matrix);
            } else if (_initRandom) {
                initRandom(result);
            }
            return result;
        }

        private void initRandom(GameOfLife gameOfLife) {
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    final double v = Math.random() * 2;
                    if (v > 1.7) {
                        gameOfLife.setCellAlive(x, y);
                    }
                }
            }
        }

    }
}
