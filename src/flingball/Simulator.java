package flingball;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import physics.Angle;
import physics.Vect;

/**
 * Simulator is a GUI interface on which to play a specific game of Flingball,
 * specified by board. It runs the simulation at a minimum of 10-20 frames per
 * second (FPS).
 *
 */
public class Simulator {

    private final BufferedImage backgroundImage;
    private final Board board;
    private static final int GAMEBOARD_SIZE = 20;
    private static final int PIXELS_PER_L = 20;
    private static final int DRAWING_AREA_SIZE_IN_PIXELS = GAMEBOARD_SIZE * PIXELS_PER_L;
    public static final int TIMER_INTERVAL_MILLISECONDS =10;//100 frames per second to make animation more smooth
    private static final ImageObserver NO_OBSERVER_NEEDED = null;

    // Abstraction Function:
    //  AF(board,backgroundImage) = the simulator of the board game with board board with
    //                        the backgroundImage which contains the background color
    //                        and all static gadgets in this board
    // Rep Invariant:
    // --| backgroundImage and board are not null
    // Safety from Rep Exposure:
    // --| backgroundImage, board are private, final and is never returned
    // Thread Safety:
    //  --| Used monitor pattern, used synchronized keyword in all instance methods.
    //  --| All fields are private and final. All instance fields left are immutable. no rep exposure.
    //  --| Used thread safe datatype for lists and maps.
    
    /**
     * Construct a Simulator with given board
     */
    Simulator(Board board) {
        this.board = board;
        backgroundImage = board.drawBackground();
        checkRep();
    }

    // Checks the Representation Invariant to ensure that no representation exposure occurs
    private void checkRep() {
        assert board != null;
        assert backgroundImage != null;
    }

    /**
     * Play the flingball game of this simulator
     * 
     */
    public void playFlingball() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                makeAndShowGUI();
            }
        });
    }

    /**
     * Makes and displays the window in which to run the Flingball game.
     * 
     */
    public void makeAndShowGUI() {
        final JFrame window = new JFrame("Flingball");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        KeyListener listener = new KeyListener() {

            public void keyTyped(KeyEvent e) {
                // do nothing
            }

            public void keyPressed(KeyEvent e) {
                board.keyPressed(e.getKeyCode());
            }

            public void keyReleased(KeyEvent e) {
                board.keyReleased(e.getKeyCode());
            }
        };
        
        window.addKeyListener(new MagicKeyListener(listener));

        final JPanel drawingArea = new JPanel() {

            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                graphics.drawImage(backgroundImage, 0, 0, DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS,
                        NO_OBSERVER_NEEDED);
                board.updateBoard();
                board.drawBalls(graphics);
                board.drawFlippers(graphics);
                board.drawJoinedBoardName(graphics);
            }
        };
        drawingArea.setPreferredSize(new Dimension(DRAWING_AREA_SIZE_IN_PIXELS, DRAWING_AREA_SIZE_IN_PIXELS));
        window.add(drawingArea);
        window.pack();
        window.setVisible(true);

        new Timer(TIMER_INTERVAL_MILLISECONDS, (ActionEvent e) -> {
            drawingArea.repaint();
        }).start();
    }
    
    public static void main(String[] args) {
        Board board = new Board("b1");
        board.addBall(new Ball("ball", new Vect(1,1), new Vect(19,19)));
        Flipper flipper = new Flipper("f1", new Vect(2, 2), Angle.DEG_270, FlipperLeftOrRight.RIGHT_FLIPPER, Angle.ZERO, true, false);
        SquareBumper sq = new SquareBumper("sq", new Vect(2, 2));
        SquareBumper sq1 = new SquareBumper("sq", new Vect(2, 3));
        SquareBumper sq2 = new SquareBumper("sq", new Vect(3, 2));
        SquareBumper sq3 = new SquareBumper("sq", new Vect(3, 3));
        board.addBumper(sq);
        board.addBumper(sq1);
        board.addBumper(sq2);
        board.addBumper(sq3);
        board.addFlipper(flipper);
        board.setTarget(flipper, KeyEvent.VK_UP, true);
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
    }
}
