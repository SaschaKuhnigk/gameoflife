package gameoflife.impl;

import gameoflife.GameOfLife;

import java.awt.Point;
import java.util.*;

/**
 * Nearly the same implementation as {@link MichasGameOfLife4b},
 * but this version removes empty tiles.
 */
public class MichasGameOfLife4c implements GameOfLife {

    /**
     * The bits of the index into this array represent:<ul>
     *     <li>{@code xxxx?xxxx} - the eight neighbours of a cell</li>
     *     <li>{@code ????x????} - the cell itself</li>
     * </ul> whereby {@code 0} stands for a dead cell and
     * {@code 1} for an alive cell.
     */
    private static final boolean[] ALIVE_IN_NEXT_GENERATION = new boolean[512];
    static {
        for (int i = 0; i <= 0x1ff; ++i) {
            if ((i & 0x10) != 0) {
                int n = Integer.bitCount(i);
                if (n == 3 || n == 4) {
                    ALIVE_IN_NEXT_GENERATION[i] = true;
                }
            } else if (Integer.bitCount(i) == 3) {
                ALIVE_IN_NEXT_GENERATION[i] = true;
            }
        }
    }

    private interface Tile {
        public void setCellAliveAt(int x, int y);
        Iterator<Point> getCoordinatesOfAliveCells();
        public void calculateNextGeneration();
        public void advanceToNextGenerationRemovingEmptyTiles();
    }

    private class TileOf32x32Cells implements Tile {
        int x0;
        int y0;
        int[] aliveCells = new int[32];
        int[] nextAliveCells = new int[32];
        TileOf32x32Cells northWest;
        TileOf32x32Cells north;
        TileOf32x32Cells northEast;
        TileOf32x32Cells east;
        TileOf32x32Cells southEast;
        TileOf32x32Cells south;
        TileOf32x32Cells southWest;
        TileOf32x32Cells west;
        boolean isCandidateForRemoval;

        private TileOf32x32Cells(int x0, int y0) {
            this.x0 = x0;
            this.y0 = y0;
            TileOf32x32Cells temp;
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 - 32, y0 - 32)) != null) { (northWest = temp).southEast = this; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0, y0 - 32)) != null) { (north = temp).south = this; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 + 32, y0 - 32)) != null) { (northEast = temp).southWest = this; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 + 32, y0)) != null) { (east = temp).west = this; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 + 32, y0 + 32)) != null) { (southEast = temp).northWest = this; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0, y0 + 32)) != null) { (south = temp).north = this; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 - 32, y0 + 32)) != null) { (southWest = temp).northEast = this; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 - 32, y0)) != null) { (west = temp).east = this; }
        }

        public void reset(int x0, int y0) {
            this.x0 = x0;
            this.y0 = y0;

            // No need to execute Arrays.fill(aliveCells, 0);
            // because only tiles with no alive cells in the
            // current generation become candidates for removal.

            // No need to execute Arrays.fill(nextAliveCells, 0);
            // because only tiles with no alive cells
            // in the next generation are added to the tilePool.

            TileOf32x32Cells temp;
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 - 32, y0 - 32)) != null) { (northWest = temp).southEast = this; } else { northWest = null; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0, y0 - 32)) != null) { (north = temp).south = this; } else { north = null; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 + 32, y0 - 32)) != null) { (northEast = temp).southWest = this; } else { northEast = null; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 + 32, y0)) != null) { (east = temp).west = this; } else { east = null; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 + 32, y0 + 32)) != null) { (southEast = temp).northWest = this; } else { southEast = null; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0, y0 + 32)) != null) { (south = temp).north = this; } else { south = null; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 - 32, y0 + 32)) != null) { (southWest = temp).northEast = this; } else { southWest = null; }
            if ((temp = theWorld.getTileOf32x32CellsAt(x0 - 32, y0)) != null) { (west = temp).east = this; } else { west = null; }

            isCandidateForRemoval = false;
        }

        @Override
        public void setCellAliveAt(int x, int y) {
            int m = 1 << (x & 0x1f);
            aliveCells[(y & 0x1f)] |= m;
        }

        @Override
        public Iterator<Point> getCoordinatesOfAliveCells() {
            return new Iterator<Point>() {
                private int i;
                private int j;
                private int m = 1;
                private Boolean hasNext;
                private Point next;

                {
                    init();
                }

                private void init() {
                    // Find first alive cell or set hasNext to false ...
                    for (;;) {
                        int row = aliveCells[j];
                        if (row != 0) {
                            while ((row & m) == 0) {
                                ++i;
                                m <<= 1;
                            }
                            next = new Point(x0 + i, y0 + j);
                            hasNext = true;
                            return;
                        } else if (++j == 32) {
                            hasNext = false;
                            return;
                        }
                    }
                }

                @Override
                public boolean hasNext() {
                    if (hasNext == null) {
                        if (++i < 32) {
                            m <<= 1;
                            return checkRemainingCells();
                        } else if (++j < 32) {
                            return checkRemainingRows();
                        } else {
                            return (hasNext = false);
                        }
                    }
                    return hasNext;
                }

                private boolean checkRemainingRows() {
                    int row = aliveCells[j];
                    while (row == 0) {
                        if (++j == 32) {
                            return (hasNext = false);
                        }
                        row = aliveCells[j];
                    }
                    i = 0;
                    m = 1;
                    while ((row & m) == 0) {
                        ++i;
                        m <<= 1;
                    }
                    next = new Point(x0 + i, y0 + j);
                    return (hasNext = true);
                }

                private boolean checkRemainingCells() {
                    int row = aliveCells[j];
                    for (;;) {
                        if ((row & m) != 0) {
                            next = new Point(x0 + i, y0 + j);
                            return (hasNext = true);
                        } else if (++i == 32) {
                            break;
                        }
                        m <<= 1;
                    }
                    if (++j < 32) {
                        return checkRemainingRows();
                    } else {
                        return (hasNext = false);
                    }
                }

                @Override
                public Point next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    try {
                        return next;
                    } finally {
                        hasNext = null;
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        private boolean isTopLeftCornerAlive() {
            return (aliveCells[0] & 1) != 0;
        }

        private boolean isTopRightCornerAlive() {
            return (aliveCells[0] & 0x80000000) != 0;
        }

        private boolean isBottomLeftCornerAlive() {
            return (aliveCells[31] & 1) != 0;
        }

        private boolean isBottomRightCornerAlive() {
            return (aliveCells[31] & 0x80000000) != 0;
        }

        private boolean isLeftCellAliveInRow(int j) {
            return (aliveCells[j] & 1) != 0;
        }

        private boolean isRightCellAliveInRow(int j) {
            return (aliveCells[j] & 0x80000000) != 0;
        }

        @Override
        public void calculateNextGeneration() {
            calculateAllCellsExceptLeftAndRightBorder();
            calculateLeftBorderExceptCorners();
            calculateRightBorderExceptCorners();
            calculateTopLeftCorner();
            calculateTopRightCorner();
            calculateBottomLeftCorner();
            calculateBottomRightCorner();
            if (north == null) {
                giveBirthToCellsInNonExistingNorthNeighbourTile();
            }
            if (east == null) {
                giveBirthToCellsInNonExistingEastNeighbourTile();
            }
            if (south == null) {
                giveBirthToCellsInNonExistingSouthNeighbourTile();
            }
            if (west == null) {
                giveBirthToCellsInNonExistingWestNeighbourTile();
            }
        }

        private void calculateAllCellsExceptLeftAndRightBorder() {
            int upperRow = (north == null ? 0 : north.aliveCells[31]);
            int row = aliveCells[0];
            int lowerRow = aliveCells[1];
            int numberOfConsecutiveEmptyRows;
            if (lowerRow != 0) {
                numberOfConsecutiveEmptyRows = 0;
            } else if (row != 0) {
                numberOfConsecutiveEmptyRows = 1;
            } else {
                numberOfConsecutiveEmptyRows = (upperRow != 0 ? 2 : 3);
            }
            int y = 0;
            for (;;) {
                if (numberOfConsecutiveEmptyRows < 3) {
                    int m1 = 0x7;
                    int m2 = 0x2;
                    int shiftUpperRow = 0;
                    int shiftRow = -3;
                    int shiftLowerRow = -6;
                    for (;;) {
                        int i = (upperRow & m1) >>> shiftUpperRow
                                | (shiftRow < 0 ? (row & m1) << -shiftRow : (row & m1) >>> shiftRow)
                                | (shiftLowerRow < 0 ? (lowerRow & m1) << -shiftLowerRow : (lowerRow & m1) >>> shiftLowerRow);
                        if (ALIVE_IN_NEXT_GENERATION[i]) {
                            nextAliveCells[y] |= m2;
                        }
                        if (m2 == 0x40000000) {
                            break;
                        }
                        m1 <<= 1;
                        m2 <<= 1;
                        ++shiftUpperRow;
                        ++shiftRow;
                        ++shiftLowerRow;
                    }
                }
                if (y == 31) {
                    break;
                }
                upperRow = row;
                row = lowerRow;
                ++y;
                lowerRow = (y < 31 ? aliveCells[y + 1] : (south == null ? 0 : south.aliveCells[0]));
                if (lowerRow != 0) {
                    numberOfConsecutiveEmptyRows = 0;
                } else {
                    ++numberOfConsecutiveEmptyRows;
                }
            }
            if (numberOfConsecutiveEmptyRows == 34) {
                isCandidateForRemoval = true;
            }
        }

        private void calculateTopLeftCorner() {
            if (ALIVE_IN_NEXT_GENERATION[indexForTopLeftCorner()]) {
                nextAliveCells[0] |= 1;
            }
        }

        private int indexForTopLeftCorner() {
            int i = ((aliveCells[0] & 3) << 4) | (aliveCells[1] & 3);
            // bits set in i: ???xx??xx
            if (north != null) {
                i |= ((north.aliveCells[31] & 3) << 2);
            }
            // bits set in i: ???xxxxxx
            if (northWest != null && northWest.isBottomRightCornerAlive()) {
                i |= 0x40;
            }
            // bits set in i: ??xxxxxxx
            if (west != null) {
                if (west.isTopRightCornerAlive()) {
                    if (west.isRightCellAliveInRow(1)) {
                        return i | 0x180;
                    } else {
                        return i | 0x80;
                    }
                } else if (west.isRightCellAliveInRow(1)) {
                    return i | 0x80;
                }
            }
            return i;
        }

        private void calculateTopRightCorner() {
            if (ALIVE_IN_NEXT_GENERATION[indexForTopRightCorner()]) {
                nextAliveCells[0] |= 0x80000000;
            }
        }

        private int indexForTopRightCorner() {
            int i = ((aliveCells[0] & 0xC0000000) >>> 27) | ((aliveCells[1] & 0xC0000000) >>> 30);
            // bits set in i: ????xx?xx
            if (north != null) {
                i |= ((north.aliveCells[31] & 0xC0000000) >>> 24);
            }
            // bits set in i: ?xx?xx?xx
            if (northEast != null && northEast.isBottomLeftCornerAlive()) {
                i |= 4;
            }
            // bits set in i: ?xx?xxxxx
            if (east != null) {
                if (east.isTopLeftCornerAlive()) {
                    if ((east.aliveCells[1] & 1) != 0) {
                        return i | 0x120;
                    } else {
                        return i | 0x100;
                    }
                } else if ((east.aliveCells[1] & 1) != 0) {
                    return i | 0x100;
                }
            }
            return i;
        }

        private void calculateBottomLeftCorner() {
            if (ALIVE_IN_NEXT_GENERATION[indexForBottomLeftCorner()]) {
                nextAliveCells[31] |= 1;
            }
        }

        private int indexForBottomLeftCorner() {
            int i = ((aliveCells[31] & 3) << 4) | (aliveCells[30] & 3);
            // bits set in i: ???xx??xx
            if (south != null) {
                i |= ((south.aliveCells[0] & 3) << 2);
            }
            // bits set in i: ???xxxxxx
            if (southWest != null && southWest.isTopRightCornerAlive()) {
                i |= 0x40;
            }
            // bits set in i: ??xxxxxxx
            if (west != null) {
                if (west.isBottomRightCornerAlive()) {
                    if (west.isRightCellAliveInRow(30)) {
                        return i | 0x180;
                    } else {
                        return i | 0x80;
                    }
                } else if (west.isRightCellAliveInRow(30)) {
                    return i | 0x80;
                }
            }
            return i;
        }

        private void calculateBottomRightCorner() {
            if (ALIVE_IN_NEXT_GENERATION[indexForBottomRightCorner()]) {
                nextAliveCells[31] |= 0x80000000;
            }
        }

        private int indexForBottomRightCorner() {
            int i = ((aliveCells[31] & 0xC0000000) >>> 27) | ((aliveCells[30] & 0xC0000000) >>> 30);
            // bits set in i: ????xx?xx
            if (south != null) {
                i |= ((south.aliveCells[0] & 0xC0000000) >>> 24);
            }
            // bits set in i: ?xx?xx?xx
            if (southEast != null && southEast.isTopLeftCornerAlive()) {
                i |= 4;
            }
            // bits set in i: ?xx?xxxxx
            if (east != null) {
                if (east.isBottomLeftCornerAlive()) {
                    if ((east.aliveCells[30] & 1) != 0) {
                        return i | 0x120;
                    } else {
                        return i | 0x100;
                    }
                } else if ((east.aliveCells[30] & 1) != 0) {
                    return i | 0x100;
                }
            }
            return i;
        }

        private void calculateLeftBorderExceptCorners() {
            if (west == null) {
                calculateLeftBorderExceptCornersWhenWestIsNull();
            } else {
                calculateLeftBorderExceptCornersWhenWestIsNotNull();
            }
        }

        private void calculateLeftBorderExceptCornersWhenWestIsNull() {
            int i = (aliveCells[0] & 3) | ((aliveCells[1] & 3) << 4) | ((aliveCells[2] & 3) << 7);
            // bits set: xx?xx??xx
            int j1 = 2;
            for (;;) {
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    nextAliveCells[j1 - 1] |= 1;
                }
                if (++j1 == 32) {
                    break;
                }
                i = (i >> 3) | ((aliveCells[j1] & 3) << 7);
            }
        }

        private void calculateLeftBorderExceptCornersWhenWestIsNotNull() {
            int i = (aliveCells[0] & 3) | ((aliveCells[1] & 3) << 4) | ((aliveCells[2] & 3) << 7);
            // bits set: xx?xx??xx
            if (west.isTopRightCornerAlive()) { i |= 4; }
            if (west.isRightCellAliveInRow(1)) { i |= 8; }
            if (west.isRightCellAliveInRow(2)) { i|= 0x40; }
            int j1 = 2;
            for (;;) {
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    nextAliveCells[j1 - 1] |= 1;
                }
                if (++j1 == 32) {
                    break;
                }
                i = (i >> 3) | ((aliveCells[j1] & 3) << 7);
                if (west.isRightCellAliveInRow(j1)) { i |= 0x40; }
            }
        }

        private void calculateRightBorderExceptCorners() {
            if (east == null) {
                calculateRightBorderExceptCornersWhenEastIsNull();
            } else {
                calculateRightBorderExceptCornersWhenEastIsNotNull();
            }
        }

        private void calculateRightBorderExceptCornersWhenEastIsNull() {
            int i = (aliveCells[0] >>> 30) | ((aliveCells[1] >>> 27) & 0x18) | ((aliveCells[2] >>> 24) & 0xC0);
            // bits set: ?xx?xx?xx
            int j1 = 2;
            for (;;) {
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    nextAliveCells[j1 - 1] |= 0x80000000;
                }
                if (++j1 == 32) {
                    break;
                }
               i = (i >> 3) | ((aliveCells[j1] >>> 24) & 0xC0);
            }
        }

        private void calculateRightBorderExceptCornersWhenEastIsNotNull() {
            int i = (aliveCells[0] >>> 30) | ((aliveCells[1] >>> 27) & 0x18) | ((aliveCells[2] >>> 24) & 0xC0);
            // bits set: ?xx?xx?xx
            if (east.isTopLeftCornerAlive()) { i |= 4; }
            if (east.isLeftCellAliveInRow(1)) { i |= 0x20; }
            if (east.isLeftCellAliveInRow(2)) { i |= 0x100; }
            int j1 = 2;
            for (;;) {
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    nextAliveCells[j1 - 1] |= 0x80000000;
                }
                if (++j1 == 32) {
                    break;
                }
                i = (i >> 3) | ((aliveCells[j1] >>> 24) & 0xC0);
                if (east.isLeftCellAliveInRow(j1)) { i |= 0x100; }
            }
        }

        private void giveBirthToCellsInNonExistingNorthNeighbourTile() {
            giveBirthToBottomLeftCornerOfNonExistingNorthNeighbourTile();
            giveBirthToBottomBorderExceptCornersOfNonExistingNorthNeighbourTile();
            giveBirthToBottomRightCornerOfNonExistingNorthNeighbourTile();
        }

        private void giveBirthToBottomLeftCornerOfNonExistingNorthNeighbourTile() {
            if (northWest != null) {
                int i = aliveCells[0] & 3;
                if (northWest.isBottomRightCornerAlive()) { i |= 4; }
                if (northWest.isRightCellAliveInRow(30)) { i |= 8; }
                if (west != null && west.isTopRightCornerAlive()) { i |= 0x20; }
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0, y0 - 1));
                }
            } else if (west != null) {
                // We only need to check the 3 bottom neighbour cells ...
                if (west.isTopRightCornerAlive() && (aliveCells[0] & 3) == 3) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0, y0 - 1));
                }
            }
        }

        private void giveBirthToBottomBorderExceptCornersOfNonExistingNorthNeighbourTile() {
            int r = aliveCells[0];
            int i = 1;
            for (;;) {
                if ((r & 7) == 7) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 + i, y0 - 1));
                }
                if (++i == 31) { break; }
                r >>>= 1;
            }
        }

        private void giveBirthToBottomRightCornerOfNonExistingNorthNeighbourTile() {
            if (northEast != null) {
                int i = aliveCells[0] >>> 30;
                if (northEast.isBottomLeftCornerAlive()) { i |= 4; }
                if (northEast.isLeftCellAliveInRow(30)) { i |= 8; }
                if (east != null && east.isTopLeftCornerAlive()) { i |= 0x20;
                }
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 + 31, y0 - 1));
                }
            } else if (east != null) {
                // We only need to check the 3 bottom neighbour cells ...
                if (east.isTopLeftCornerAlive() && (aliveCells[0] & 0xC0000000) == 0xC0000000) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 + 31, y0 - 1));
                }
            }
        }

        private void giveBirthToCellsInNonExistingSouthNeighbourTile() {
            giveBirthToTopLeftCornerOfNonExistingSouthNeighbourTile();
            giveBirthToTopBorderExceptCornersOfNonExistingSouthNeighbourTile();
            giveBirthToTopRightCornerOfNonExistingSouthNeighbourTile();
        }

        private void giveBirthToTopLeftCornerOfNonExistingSouthNeighbourTile() {
            if (southWest != null) {
                int i = aliveCells[31] & 3;
                if (southWest.isTopRightCornerAlive()) { i |= 4; }
                if (southWest.isRightCellAliveInRow(1)) { i |= 8; }
                if (west != null && west.isBottomRightCornerAlive()) { i |= 0x20; }
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0, y0 + 32));
                }
            } else if (west != null) {
                // We only need to check the 3 top neighbour cells ...
                if (west.isBottomRightCornerAlive() && (aliveCells[31] & 3) == 3) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0, y0 + 32));
                }
            }
        }

        private void giveBirthToTopBorderExceptCornersOfNonExistingSouthNeighbourTile() {
            int r = aliveCells[31];
            int i = 1;
            for (;;) {
                if ((r & 7) == 7) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 + i, y0 + 32));
                }
                if (++i == 31) { break; }
                r >>>= 1;
            }
        }

        private void giveBirthToTopRightCornerOfNonExistingSouthNeighbourTile() {
            if (southEast != null) {
                int i = aliveCells[31] >>> 30;
                if (southEast.isTopLeftCornerAlive()) { i |= 4; }
                if (southEast.isLeftCellAliveInRow(1)) { i |= 8; }
                if (east != null && east.isBottomLeftCornerAlive()) { i |= 0x20; }
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 + 31, y0 + 32));
                }
            } else if (east != null) {
                // We only need to check the 3 top neighbour cells ...
                if (east.isBottomLeftCornerAlive() && (aliveCells[31] & 0xC0000000) == 0xC0000000) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 + 31, y0 + 32));
                }
            }
        }

        private void giveBirthToCellsInNonExistingEastNeighbourTile() {
            if (northEast == null) {
                giveBirthToTopLeftCornerOfNonExistingEastNeighbourTileWhenNorthEastIsNull();
            } else {
                // We don't need to calculate the top left corner of the east tile here, because it will be calculated
                // when the northEast tile executes giveBirthToTopLeftCornerOfNonExistingSouthNeighbourTile()
            }
            giveBirthToLeftBorderExceptCornersOfNonExistingEastNeighbourTile();
            if (southEast == null) {
                giveBirthToBottomLeftCornerOfNonExistingEastNeighbourTileWhenSouthEastIsNull();
            } else {
                // We don't need to calculate the bottom left corner of the east tile here, because it will be calculated
                // when the southEast tile executes giveBirthToBottomLeftCornerOfNonExistingNorthNeighbourTile()
            }
        }

        private void giveBirthToTopLeftCornerOfNonExistingEastNeighbourTileWhenNorthEastIsNull() {
            // We only need to check if the 3 left neighbour cells are alive ...
            if (north != null && isTopRightCornerAlive() && north.isBottomRightCornerAlive() && isRightCellAliveInRow(1)) {
                coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 + 32, y0));
            }
        }

        private void giveBirthToLeftBorderExceptCornersOfNonExistingEastNeighbourTile() {
            int i = (aliveCells[0] >>> 31) | ((aliveCells[1] >>> 28) & 8) | ((aliveCells[2] >>> 25) & 0x40);
            // bits set: ??x??x??x
            int j1 = 2;
            for (;;) {
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 + 32, y0 + j1 - 1));
                }
                if (++j1 == 32) {
                    break;
                }
                i = (i >> 3) | ((aliveCells[j1] >>> 25) & 0x40);
            }
        }

        private void giveBirthToBottomLeftCornerOfNonExistingEastNeighbourTileWhenSouthEastIsNull() {
            // We only need to check if the 3 left neighbour cells are alive ...
            if (south != null && isBottomRightCornerAlive() && south.isTopRightCornerAlive() && isRightCellAliveInRow(30)) {
                coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 + 32, y0 + 31));
            }
        }

        private void giveBirthToCellsInNonExistingWestNeighbourTile() {
            if (northWest == null) {
                giveBirthToTopRightCornerOfNonExistingWestNeighbourTileWhenNorthWestIsNull();
            } else {
                // We don't need to calculate the top right corner of the west tile here, because it will be calculated
                // when the northWest tile executes giveBirthToTopRightCornerOfNonExistingSouthNeighbourTile()
            }
            giveBirthToRightBorderExceptCornersOfNonExistingWestNeighbourTile();
            if (southWest == null) {
                giveBirthToBottomRightCornerOfNonExistingWestNeighbourTileWhenSouthWestIsNull();
            } else {
                // We don't need to calculate the bottom right corner of the west tile here, because it will be calculated
                // when the southWest tile executes giveBirthToBottomRightCornerOfNonExistingNorthNeighbourTile()
            }
        }

        private void giveBirthToTopRightCornerOfNonExistingWestNeighbourTileWhenNorthWestIsNull() {
            // We only need to check if the 3 right neighbour cells are alive ...
            if (north != null && isTopLeftCornerAlive() && north.isBottomLeftCornerAlive() && isLeftCellAliveInRow(1)) {
                coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 - 1, y0));
            }
        }

        private void giveBirthToRightBorderExceptCornersOfNonExistingWestNeighbourTile() {
            int i = (aliveCells[0] & 1) | ((aliveCells[1] << 3) & 8) | ((aliveCells[2] << 6) & 0x40);
            // bits set: ??x??x??x
            int j1 = 2;
            for (;;) {
                if (ALIVE_IN_NEXT_GENERATION[i]) {
                    coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 - 1, y0 + j1 - 1));
                }
                if (++j1 == 32) {
                    break;
                }
                i = (i >> 3) | ((aliveCells[j1] << 6) & 0x40);
            }
        }

        private void giveBirthToBottomRightCornerOfNonExistingWestNeighbourTileWhenSouthWestIsNull() {
            // We only need to check if the 3 right neighbour cells are alive ...
            if (south != null && isBottomLeftCornerAlive() && south.isTopLeftCornerAlive() && isLeftCellAliveInRow(30)) {
                coordinatesOfNewAliveCellsOutsideAnyExistingTile.add(new Point(x0 - 1, y0 + 31));
            }
        }

        @Override
        public void advanceToNextGenerationRemovingEmptyTiles() {
            throw new UnsupportedOperationException();
        }

        public void advanceToNextGeneration() {
            int[] temp = aliveCells;
            aliveCells = nextAliveCells;
            Arrays.fill(nextAliveCells = temp, 0);
        }

        public boolean hasNoAliveCellsInNextGeneration() {
            for (int i = 0; i < 32; ++i) {
                if (nextAliveCells[i] != 0) {
                    return false;
                }
            }
            return true;
        }

        public void remove() {
            if (hasAliveCellsInCurrentGeneration() || hasAliveCellsInNextGeneration()) {
                throw new IllegalStateException();
            }
            if (northWest != null) { northWest.southEast = null; }
            if (north != null) { north.south = null; }
            if (northEast != null) { northEast.southWest = null; }
            if (east != null) { east.west = null; }
            if (southEast != null) { southEast.northWest = null; }
            if (south != null) { south.north = null; }
            if (southWest != null) { southWest.northEast = null; }
            if (west != null) { west.east = null; }
        }

        private boolean hasAliveCellsInNextGeneration() {
            return !hasNoAliveCellsInNextGeneration();
        }

        private boolean hasAliveCellsInCurrentGeneration() {
            for (int i = 0; i < 32; ++i) {
                if (aliveCells[i] != 0) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 32; ++j) {
                int m = 1;
                for (int i = 0; i < 32; ++i) {
                    sb.append((aliveCells[j] & m) == 0 ? '.' : 'x');
                    m <<= 1;
                }
                sb.append (j == 15 ? "  =>  " : "      ");
                m = 1;
                for (int i = 0; i < 32; ++i) {
                    sb.append((nextAliveCells[j] & m) == 0 ? '.' : 'x');
                    m <<= 1;
                }

                sb.append('\n');
            }
            return sb.toString();
        }
    }

    private abstract static class TileOfTiles<T extends Tile> implements Tile {
        final int x0;
        final int y0;
        final int numberOfLowerBitsToIgnore;
        final int bitMask;
        final T[][] children;
        final T[] nonNullChildren;
        int n;

        protected TileOfTiles(int x0, int y0, int numberOfLowerBitsToIgnore, T[][] children, T[] nonNullChildren) {
            this.x0 = x0;
            this.y0 = y0;
            this.numberOfLowerBitsToIgnore = numberOfLowerBitsToIgnore;
            bitMask = ~((1 << numberOfLowerBitsToIgnore) - 1);
            this.children = children;
            this.nonNullChildren = nonNullChildren;
        }

        @Override
        public void setCellAliveAt(int x, int y) {
            int i = (x >>> numberOfLowerBitsToIgnore) & 0x1ff;
            int j = (y >>> numberOfLowerBitsToIgnore) & 0x1ff;
            Tile child = children[i][j];
            if (child != null) {
                child.setCellAliveAt(x, y);
            } else {
                (nonNullChildren[n] = children[i][j] = newChild(x & bitMask, y & bitMask)).setCellAliveAt(x, y);
                ++n;
            }
        }

        protected abstract T newChild(int x0, int y0);

        @Override
        public Iterator<Point> getCoordinatesOfAliveCells() {
            return new Iterator<Point>() {
                Boolean hasNext;
                Point next;
                int i;
                Iterator<Point> j;

                {
                    init();
                }

                private void init() {
                    // Find first child iterator with at least one alive cell ...
                    while (i < n) {
                        j = nonNullChildren[i].getCoordinatesOfAliveCells();
                        if (j.hasNext()) {
                            next = j.next();
                            hasNext = true;
                            return;
                        }
                        ++i;
                    }
                    hasNext = false;
                }

                @Override
                public boolean hasNext() {
                    if (hasNext == null) {
                        // Check current child iterator ...
                        if (j.hasNext()) {
                            next = j.next();
                            return (hasNext = true);
                        }
                        // Check next non null children ...
                        while (++i < n) {
                            j = nonNullChildren[i].getCoordinatesOfAliveCells();
                            if (j.hasNext()) {
                                next = j.next();
                                return (hasNext = true);
                            }
                        }
                        hasNext = false;
                    }
                    return hasNext;
                }

                @Override
                public Point next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    try {
                        return next;
                    } finally {
                        hasNext = null;
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public void calculateNextGeneration() {
            for (int i = 0; i < n; ++i) {
                nonNullChildren[i].calculateNextGeneration();
            }
        }

        @Override
        public void advanceToNextGenerationRemovingEmptyTiles() {
            for (int i = 0; i < n; ++i) {
                nonNullChildren[i].advanceToNextGenerationRemovingEmptyTiles();
            }
        }
    }

    private class MegaTile extends TileOfTiles<TileOf32x32Cells> {
        private MegaTile(int x0, int y0) {
            super(x0, y0, 5, new TileOf32x32Cells[512][512], new TileOf32x32Cells[512 * 512]);
        }

        @Override
        protected TileOf32x32Cells newChild(int x0, int y0) {
            TileOf32x32Cells newChild;
            if (tilePool.isEmpty()) {
                newChild = new TileOf32x32Cells(x0, y0);                
            } else {
                newChild = tilePool.removeFirst();
                newChild.reset(x0, y0);
            }
            return newChild;
        }

        @Override
        public void advanceToNextGenerationRemovingEmptyTiles() {
            int i = 0;
            while (i < n) {
                TileOf32x32Cells child = nonNullChildren[i++];
                if (child.isCandidateForRemoval) {
                    if (child.hasNoAliveCellsInNextGeneration()) {
                        child.remove();
                        if (i == n) {
                            --n;
                        } else {
                            nonNullChildren[--i] = nonNullChildren[--n];
                        }
                        tilePool.addFirst(child);
                        children[(child.x0 >>> 5) & 0x1ff][(child.y0 >>> 5) & 0x1ff] = null;
                        continue;
                    } else {
                        child.isCandidateForRemoval = false;
                    }
                }
                child.advanceToNextGeneration();
            }
        }
    }

    private class SuperMegaTile extends TileOfTiles<MegaTile> {
        private SuperMegaTile(int x0, int y0) {
            super(x0, y0, 14, new MegaTile[512][512], new MegaTile[512 * 512]);
        }

        @Override
        protected MegaTile newChild(int x0, int y0) {
            return new MegaTile(x0, y0);
        }
    }

    private class TheWorld extends TileOfTiles<SuperMegaTile> {
        private TheWorld() {
            super(0, 0, 23, new SuperMegaTile[512][512], new SuperMegaTile[512 * 512]);
        }

        @Override
        protected SuperMegaTile newChild(int x0, int y0) {
            return new SuperMegaTile(x0, y0);
        }

        public TileOf32x32Cells getTileOf32x32CellsAt(int x0, int y0) {
            SuperMegaTile superMegaTile = children[x0 >>> 23][y0 >>> 23];
            if (superMegaTile == null) {
                return null;
            }
            MegaTile megaTile = superMegaTile.children[(x0 >>> 14) & 0x1ff][(y0 >>> 14) & 0x1ff];
            if (megaTile == null) {
                return null;
            }
            TileOf32x32Cells tile = megaTile.children[(x0 >>> 5) & 0x1ff][(y0 >>> 5) & 0x1ff];
            return tile;
        }
    }

    private final TheWorld theWorld = new TheWorld();
    private final Deque<TileOf32x32Cells> tilePool = new LinkedList<TileOf32x32Cells>();
    private final Collection<Point> coordinatesOfNewAliveCellsOutsideAnyExistingTile = new ArrayList<Point>();

    @Override
    public void setCellAlive(int x, int y) {
        theWorld.setCellAliveAt(x, y);
    }

    @Override
    public void calculateNextGeneration() {
        theWorld.calculateNextGeneration();
        theWorld.advanceToNextGenerationRemovingEmptyTiles();
        for (Point point : coordinatesOfNewAliveCellsOutsideAnyExistingTile) {
            theWorld.setCellAliveAt(point.x, point.y);
        }
        coordinatesOfNewAliveCellsOutsideAnyExistingTile.clear();
    }

    @Override
    public Iterable<Point> getCoordinatesOfAliveCells() {
        return new Iterable<Point>() {
            @Override
            public Iterator<Point> iterator() {
                return theWorld.getCoordinatesOfAliveCells();
            }
        };
    }
}
