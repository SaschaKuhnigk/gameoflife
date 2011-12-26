package gameoflife.ui;

import gameoflife.GameOfLife;
import gameoflife.impl.*;
import gameoflife.templates.CellBlock;
import gameoflife.templates.Pattern;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class Main extends JDialog {

    public static final int INITIAL_WIDTH = 800;
    public static final int INITIAL_HEIGHT = 800;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        Main dialog = new Main();
        dialog.pack();
        dialog.setVisible(true);
    }

    private static GameOfLife initWithPattern(GameOfLife gameOfLife) {
        final InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("linepuf.lif");
        final Pattern pattern = new Pattern(new BufferedReader(new InputStreamReader(resourceAsStream)));
        for (int i = 0; i < pattern.getNumberOfCellBlocks(); ++i) {
            final CellBlock cellBlock = pattern.getCellBlock(i);
            for (Point livingCell : cellBlock.getLivingCells()) {
                gameOfLife.setCellAlive(
                    livingCell.x + (INITIAL_WIDTH / 2) + cellBlock.getXOffset(),
                    livingCell.y + (INITIAL_HEIGHT / 2) + cellBlock.getYOffset()
                );
            }
        }
        return gameOfLife;
    }

    private static GameOfLife initRandomly(GameOfLife gameOfLife) {
        for (int x = 0; x < INITIAL_WIDTH; ++x) {
            for (int y = 0; y < INITIAL_HEIGHT; ++y) {
                final double v = Math.random() * 2;
                if (v > 1.7) {
                    gameOfLife.setCellAlive(x, y);
                }
            }
        }
        return gameOfLife;
    }

    private static class GameOfLifeSnapshot {
        final int generation;
        final Set<Point> coordinatesOfAliveCells;

        private GameOfLifeSnapshot(int generation, Set<Point> coordinatesOfAliveCells) {
            this.generation = generation;
            this.coordinatesOfAliveCells = coordinatesOfAliveCells;
        }
    }
    
    private static class CalculateThread extends Thread {
        private final GameOfLife _gameOfLife;
        private volatile RunMode _runMode;
        private volatile int _generation;

        public enum RunMode {
            PAUSED,
            SYNC,
            ASYNC
        }

        private CalculateThread(GameOfLife gameOfLife) {
            setDaemon(true);
            setPriority(MIN_PRIORITY);
            _gameOfLife = gameOfLife;
            _runMode = RunMode.PAUSED;
        }

        public void calculateNextGeneration() {
            synchronized (_gameOfLife) {
                if (_runMode == RunMode.PAUSED) {
                    _gameOfLife.calculateNextGeneration();
                    _generation++;
                }
            }
        }
        
        public void setRunMode(RunMode runMode) {
            synchronized (_gameOfLife) {
                _runMode = runMode;
                _gameOfLife.notifyAll();
            }
        }

        public GameOfLifeSnapshot getSnapshot() {
            synchronized (_gameOfLife) {
                if (_runMode == RunMode.SYNC) {
                    _gameOfLife.calculateNextGeneration();
                    _generation++;
                }
                return new GameOfLifeSnapshot(_generation, newHashSet(_gameOfLife.getCoordinatesOfAliveCells()));
            }
        }

        @Override
        public void run() {
            for (;;) {
                synchronized (_gameOfLife) {
                    if (_runMode == RunMode.ASYNC) {
                        _gameOfLife.calculateNextGeneration();
                        _generation++;
                    } else {
                        try {
                            _gameOfLife.wait(1000);
                        } catch (InterruptedException ignored) {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private JPanel _contentPane;
    private JPanel _theWorldPanel;
    private JButton _pauseButton;
    private JButton _stepButton;
    private JButton _runButton;
    private JButton _fastForwardButton;
    private JLabel _generationLabel;

    private int _sizePerCell = 1;

    private CalculateThread _calculateThread;
    private GameOfLifeSnapshot _displayedSnapshot;
    
    private volatile Point _dragStartPoint;
    private volatile int xOffset;
    private volatile int yOffset;
    
    public Main() {
        _calculateThread = new CalculateThread(initWithPattern(new MichasGameOfLife5()));
        _calculateThread.start();
        _displayedSnapshot = _calculateThread.getSnapshot();
        setContentPane(_contentPane);
        setModal(true);
        final Dimension dimension = new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT);
        setMinimumSize(dimension);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        final MouseInputAdapter mouseListener = new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                _dragStartPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                xOffset += p.x - _dragStartPoint.x;
                yOffset += p.y - _dragStartPoint.y;
                _dragStartPoint = p;
                _theWorldPanel.repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0 && _sizePerCell < 30) {
                    Point dp = e.getPoint();
                    float x_at_e = ((float) dp.x - xOffset) / _sizePerCell;
                    xOffset = Math.round(dp.x - (x_at_e * (_sizePerCell + 1)));
                    float y_at_e = ((float) dp.y - yOffset) / _sizePerCell;
                    yOffset = Math.round(dp.y - (y_at_e * (_sizePerCell + 1)));
                    _sizePerCell++;
                    _theWorldPanel.repaint();
                } else if (_sizePerCell > 1) {
                    Point dp = e.getPoint();
                    float x_at_e = ((float) dp.x - xOffset) / _sizePerCell;
                    xOffset = Math.round(dp.x - (x_at_e * (_sizePerCell - 1)));
                    float y_at_e = ((float) dp.y - yOffset) / _sizePerCell;
                    yOffset = Math.round(dp.y - (y_at_e * (_sizePerCell - 1)));
                    _sizePerCell--;
                    _theWorldPanel.repaint();
                }
            }
        };
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);
        _pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _pauseButton.setEnabled(false);
                _stepButton.setEnabled(true);
                _runButton.setEnabled(true);
                _fastForwardButton.setEnabled(true);
                _calculateThread.setRunMode(CalculateThread.RunMode.PAUSED);
            }
        });
        _stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _calculateThread.calculateNextGeneration();
                update();
            }
        });
        _runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _pauseButton.setEnabled(true);
                _stepButton.setEnabled(false);
                _runButton.setEnabled(false);
                _fastForwardButton.setEnabled(true);
                _calculateThread.setRunMode(CalculateThread.RunMode.SYNC);
            }
        });
        _fastForwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _pauseButton.setEnabled(true);
                _stepButton.setEnabled(false);
                _runButton.setEnabled(true);
                _fastForwardButton.setEnabled(false);
                _calculateThread.setRunMode(CalculateThread.RunMode.ASYNC);
            }
        });
        new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        }).start();
    }

    private void update() {
        GameOfLifeSnapshot snapshot = _calculateThread.getSnapshot();
        if (_displayedSnapshot.generation < snapshot.generation) {
            _displayedSnapshot = snapshot;
            _theWorldPanel.repaint();
            _generationLabel.setText("Generation: " + snapshot.generation);
        }
    }

    private void createUIComponents() {
        _theWorldPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.RED);
                g.fillRect(0, 0, _sizePerCell, _sizePerCell);
                g.setColor(Color.BLACK);
                for (Point p : _displayedSnapshot.coordinatesOfAliveCells) {
                    int x = p.x * _sizePerCell + xOffset;
                    int y = p.y * _sizePerCell + yOffset;
                    g.fillRect(x, y, _sizePerCell, _sizePerCell);
                }
            }
        };
        _theWorldPanel.setVisible(true);
    }
}
