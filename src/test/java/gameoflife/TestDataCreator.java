package gameoflife;

import gameoflife.game.Generation;

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

    public static class GenerationBuilder implements Builder<Generation> {

        private final int width;
        private final int height;
        private int[] _matrix;

        public GenerationBuilder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Generation build() {
            final Generation result = new Generation(width, height);
            if (_matrix != null) {
                result.addMatrix(_matrix);
            }
            return result;
        }

        public Builder<Generation> withCells(int... matrix) {
            _matrix = matrix;
            return this;
        }
    }
}
