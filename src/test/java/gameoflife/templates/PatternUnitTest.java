package gameoflife.templates;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import static org.fest.assertions.Assertions.assertThat;

public class PatternUnitTest {

    @Test
    public void readPatternWithOneCellBlock() {
        final StringReader stringReader = new StringReader(
                        "#Life 1.05\n" +
                        "#P -1 -1\n" +
                        ".*\n " +
                        "***\n " +
                        "*.**)\n"
        );
//        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("bhepto.lif");
        final BufferedReader bufferedReader = new BufferedReader(stringReader);
        final Pattern pattern = new Pattern(bufferedReader);
        assertThat(pattern.getNumberOfCellBlocks()).isEqualTo(1);
        final CellBlock cellBlock = pattern.getCellBlock(0);
        assertThat(cellBlock.getXOffset()).isEqualTo(-1);
        assertThat(cellBlock.getYOffset()).isEqualTo(-1);
        assertThat(cellBlock.isAlife(0, 0)).isEqualTo(false);
        assertThat(cellBlock.isAlife(1, 0)).isEqualTo(true);
        assertThat(cellBlock.isAlife(0, 1)).isEqualTo(true);
        assertThat(cellBlock.isAlife(1, 1)).isEqualTo(true);
        assertThat(cellBlock.isAlife(2, 1)).isEqualTo(true);
        assertThat(cellBlock.isAlife(0, 2)).isEqualTo(true);
        assertThat(cellBlock.isAlife(1, 2)).isEqualTo(false);
        assertThat(cellBlock.isAlife(2, 2)).isEqualTo(true);
        assertThat(cellBlock.isAlife(3, 2)).isEqualTo(true);
    }
}
