package gameoflife.ui;

import gameoflife.game.*;
import gameoflife.templates.CellBlock;
import gameoflife.templates.Pattern;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main extends JDialog {
    private JPanel contentPane;
    private JPanel _panel;
    private JLabel currentGeneration;

    private final int _width = 300;
    private final int _height = 300;
    private final int _sizePerCell = 5;
    private final Dimension _dimensions;

    private Generation _currentGeneration;
    private final Object _currentGenerationMonitor = new Object();

    public Main() {
        setContentPane(contentPane);
        setModal(true);
        _dimensions = new Dimension(_width * _sizePerCell, _height * _sizePerCell);
        _currentGeneration = new Generation(_width, _height);
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("rpento.lif");
        final Pattern pattern = new Pattern(new BufferedReader(new InputStreamReader(resourceAsStream)));
        for (int i = 0; i < pattern.getNumberOfCellBlocks(); ++i) {
            final CellBlock cellBlock = pattern.getCellBlock(i);
            for (gameoflife.game.Point livingCell : cellBlock.getLivingCells()) {
                _currentGeneration.setAlive(
                        livingCell.x + (_width / 2) + cellBlock.getXOffset(),
                        livingCell.y + (_height / 2) + cellBlock.getYOffset()
                );
            }
        }
//        initRandom();
        setMinimumSize(_dimensions);
    }

    private void initRandom() {
        for (int x = 0; x < _width; ++x) {
            for (int y = 0; y < _height; ++y) {
                final double v = Math.random() * 2;
                if (v > 1.7) {
                    _currentGeneration.setAlive(x, y);
                }
            }
        }
    }

    public static void main(String[] args) {
        Main dialog = new Main();
        dialog.pack();
        dialog.start();
        dialog.setVisible(true);
    }

    private void start() {
        final Timer timer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synchronized (_currentGenerationMonitor) {
                    _currentGeneration = _currentGeneration.next();
                    _panel.repaint();
                    currentGeneration.setText("" +_currentGeneration.generationNumber());
                }
            }
        });
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                timer.start();
            }
        }).start();
    }

    private void createUIComponents() {
        _panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.clearRect(0, 0, _dimensions.width, _dimensions.height);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, _dimensions.width, _dimensions.height);
                g.setColor(Color.BLACK);
                final Set<gameoflife.game.Point> livingCells = _currentGeneration.getLivingCells();
                for (Point livingCell : livingCells) {
                    g.fillRect(livingCell.x * _sizePerCell, livingCell.y * _sizePerCell, _sizePerCell, _sizePerCell);
                }
            }
        };
        _panel.setVisible(true);
    }
}
