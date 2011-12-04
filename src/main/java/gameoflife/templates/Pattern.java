package gameoflife.templates;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gameoflife.util.Validate.notNull;

public class Pattern {

    private final List<CellBlock> _cellBlocks = new ArrayList<CellBlock>();
    private int _numberOfCellBlocks;

    public Pattern(BufferedReader bufferedReader) {
        _numberOfCellBlocks = 0;
        try {
            notNull(bufferedReader);
            final String headLine = readLine(bufferedReader);
            if (headLine == null || !headLine.trim().equals("#Life 1.05")) {
                throw new RuntimeException("Illegal headline '" + headLine + "'.");
            }
            CellBlock currentCellBlock = null;
            int currentRow = 0;
            String rawLine;
            while ((rawLine = readLine(bufferedReader)) != null) {
                final String currentLine = rawLine.trim();
                if (currentLine.startsWith("#P")) {
                    _numberOfCellBlocks++;
                    currentRow = 0;
                    final String[] split = currentLine.split("\\s");
                    final int readXOffset = Integer.parseInt(split[1].trim());
                    final int readYOffset = Integer.parseInt(split[2].trim());
                    currentCellBlock = new CellBlock(readXOffset, readYOffset);
                    _cellBlocks.add(currentCellBlock);
                } else if (currentCellBlock != null && !currentLine.startsWith("#")) {
                    for (int i = 0; i < currentLine.trim().length(); ++i) {
                        final char currentChar = currentLine.charAt(i);
                        final String currentCharAsString = Character.toString(currentChar);
                        if (currentCharAsString.equals("*")) {
                            currentCellBlock.addCellAlive(i, currentRow);
                        }
                    }
                    currentRow++;
                }
            }
        } finally {
            close(bufferedReader);
        }
    }

    public int getNumberOfCellBlocks() {
        return _numberOfCellBlocks;
    }

    private void close(BufferedReader bufferedReader){
        try {
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readLine(BufferedReader bufferedReader) {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CellBlock getCellBlock(int i) {
        return _cellBlocks.get(i);
    }
}
