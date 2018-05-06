package flingball;

import static org.junit.Assert.*;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import edu.mit.eecs.parserlib.UnableToParseException;
import physics.*;

public class BoardTest {
    
    /*
     * Testing Strategy
     * 
     * Phase 2
     * 
     * hasPortalName:
     * result is true, false
     * 
     * addPortal:
     * otherBoard == this board, != this board
     * otherPortal== thisPortal, != thisPortal
     * 
     * addFilpper:
     * single flipper
     * 
     * keyPressed:
     * key can trigger an antion/ can't 
     * action gadget is absorbor, flipper
     * 
     * keyReleased:
     * key can trigger an antion/ can't
     * action gadget is absorbor, flipper
     * 
     * joinBoard:
     * set left, right, top, bottom
     * is itself, not itself
     * 
     * disjoinBoard:
     * left, right, top, bottom
     * is itself, not itself
     * 
     * hasJoinedBoard
     * true,false
     * 
     * getJoinedBoard
     * left, right, top, bottom
     * 
     * checkOutBoundary
     * exist ball on edge, no ball on edge
     * ball on left edge, right edge, bottom edge, top edge
     * 
     * getGadgetWithName
     * gadget is bumper, flipper, portal
     * 
     * receiveBall
     * 
     * setFlingball
     * 
     * setTarget
     * trigger is gadget, key
     * target is absorber, flipper
     * 
     * update:
     * there are some joined boards, there aren't
     * ball transporting from one board to the other
     * ball teleport or not
     * flippers triggered or not
     * 
     * phase 1
     * 
     * addBall
     * Single ball
     * list of balls size = 0, 1, >1
     * 
     * addBumper
     * single bumper
     * list of bumpers size = 0, 1, >1
     * 
     * addAbsorber
     * single absorber
     * list of absorbers size = 0, 1, > 1
     * 
     * getBalls
     * list of balls size = 0, 1, >1
     * 
     * getStaticBumpers
     * list of bumpers size = 0, 1, >1
     * 
     * getAbsorbers
     * list of absorbers size = 0, 1, > 1
     * 
     * getStaticGadgets
     * list size = 0, 1, > 1
     * 
     * getGravity
     * gravity = default (25), > default, 0 < gravity < default
     * 
     * getFriction1
     * mu1 = 0, default (0.025), 0 < mu1 < default, > default
     * 
     * getFriction2
     * mu2 = 0, default (0.025), 0 < mu2 < default, > default
     * 
     * getName
     * Name is lowercase, uppercase, mixed case
     * 
     * updateBoard
     * check output of ball: velocity, position
     * ball doesn't collide
     * 
     * toString
     * # of gadgets = 0, 1, >1
     * 
     * hashCode
     * two equivalent boards should have same hashCode
     *
     * equals
     * check: symmetry, reflexive, transitivity
     * empty (expect: true)
     * same number and types of gadgets (expect: true)
     * same number, but not types of gadgets (expect: false)
     */
    /*
     * covers hasPortalName: result is true, false
     * covers addPortal: otherBoard != this board, otherPortal!= thisPortal
     */
    @Test public void testHasPortalNameAddPortalOther() {
        Board board1 = new Board("b1");
        Board board2 = new Board("b2");
        Portal p1 = new Portal("p1", "p1", "b1", new Vect(1,1));
        Portal p2 = new Portal("p2", "p1", "b2", new Vect(5,5));
        board1.addPortal(p1);
        board2.addPortal(p2);
        assertEquals("Expect has portal name", true, board1.hasPortal("p1"));
        assertEquals("Expect doesn't have portal name", false, board2.hasPortal("p1"));
        assertEquals("Expect doesn't have portal name", false, board1.hasPortal("p2"));
        assertEquals("Expect has portal name", true, board2.hasPortal("p2"));
    }
    
    /*
     * covers addPortal:
     * otherBoard == this board, otherPortal== thisPortal
     */
    @Test public void testaddPortalSameBoard() {
        Board board1 = new Board("b1");
        Portal portal1 = new Portal("p1", "p1", "b1", new Vect(1,1));
        board1.addPortal(portal1);
        assertEquals("Expect has portal name", true, board1.hasPortal("p1"));
    }
    /*
     * covers getPortals:
     * size=0,=1,>1
     */
    @Test public void testGetPortals() {
        Board board1 = new Board("b1");
        assertEquals("Expect portals size", 0, board1.getPortals().size());
        Portal portal1 = new Portal("p1", "p1", "b1", new Vect(1,1));
        board1.addPortal(portal1);
        assertEquals("Expect portals size", 1, board1.getPortals().size());
        assertEquals("Expect portal",portal1, board1.getPortals().get(0));
        Portal portal2 = new Portal("p2", "p1", "b1", new Vect(1,1));
        board1.addPortal(portal2);
        assertEquals("Expect portals size", 2, board1.getPortals().size());
        assertEquals("Expect portal",portal2, board1.getPortals().get(1));
    }
    /*
     * covers getFlippers:
     * size=0,=1,>1
     */
    @Test public void testAddGetFlippers() {
        Board board1 = new Board("b1");
        assertEquals("Expect flippers size", 0, board1.getFlippers().size());
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_90,FlipperLeftOrRight.LEFT_FLIPPER);
        board1.addFlipper(flipper);
        assertEquals("Expect flippers size", 1, board1.getFlippers().size());
        assertEquals("Expect flipper", flipper, board1.getFlippers().get(0));
        Flipper flipper2=new Flipper("f1", new Vect(1,1), Angle.DEG_90,FlipperLeftOrRight.LEFT_FLIPPER);
        board1.addFlipper(flipper2);
        assertEquals("Expect flippers size", 2, board1.getFlippers().size());
        assertEquals("Expect flipper", flipper2, board1.getFlippers().get(1));
    }

    /*
     * covers joinBoard, disjoinBoard: set left, right, top, bottom
     */
    @Test public void testJoinBoardDisjoinBoard() {
        Board board1 = new Board("b1");
        Board board2 = new Board("b2");
        board1.joinBoard(board1.getName(), BOARD_DIRECTION.LEFT);
        board1.joinBoard(board1.getName(), BOARD_DIRECTION.RIGHT);
        board1.joinBoard(board2.getName(), BOARD_DIRECTION.TOP);
        board1.joinBoard(board2.getName(), BOARD_DIRECTION.BOTTOM);
        assertEquals("Expect other board name", "b1", board1.getJoinedBoard(BOARD_DIRECTION.LEFT));
        assertEquals("Expect other board name", "b1", board1.getJoinedBoard(BOARD_DIRECTION.RIGHT));
        assertEquals("Expect other board name", "b2", board1.getJoinedBoard(BOARD_DIRECTION.TOP));
        assertEquals("Expect other board name", "b2", board1.getJoinedBoard(BOARD_DIRECTION.BOTTOM));
        assertEquals("Expect false", true, board1.hasJoinedBoard("b1"));
        assertEquals("Expect false", true, board1.hasJoinedBoard("b2"));
        assertEquals("Expect false", false, board1.hasJoinedBoard("b3"));
        assertEquals("Expect false", false, board2.hasJoinedBoard("b1"));
        assertEquals("Expect false", false, board2.hasJoinedBoard("b2"));
        assertEquals("Expect false",false, board2.hasJoinedBoard("b3"));
        board1.disjoinBoard("b1");
        board1.disjoinBoard("b2");
        assertEquals("Expect false", false, board1.hasJoinedBoard("b1"));
        assertEquals("Expect false", false, board1.hasJoinedBoard("b2"));
    }

    /*
     * covers getGadgetWithName
     */
    @Test public void testgetGadgetWithName() {
        Board board1 = new Board("b1");
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_90,FlipperLeftOrRight.LEFT_FLIPPER);
        board1.addFlipper(flipper);
        Absorber abs = new Absorber("abs", new Vect(0, 0), new Vect(5, 5));
        board1.addAbsorber(abs);
        assertEquals("Expect flipper", flipper, board1.getGadgetWithName("f1"));
        assertEquals("Expect absorber", abs, board1.getGadgetWithName("abs"));
    }
    //covers ball collision
    @Test
    public void testFlingballcollisionTest_visualTest() throws IOException, InterruptedException {
        Board board = new Board("b1",0,0,0);
        Ball b1=new Ball("ball", new Vect(9,10), new Vect(1,0));
        Ball b2=new Ball("ball"+1, new Vect(11,10), new Vect(-1,0));
        board.addBall(b1);
        board.addBall(b2);
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(5000);
    }
    /* covers: toString
     * # of gadgets = 1
     */
    @Test public void testBoardToString2() throws UnableToParseException {
        final String boardString = "board name=Example\n "
                + "leftFlipper name=Flip x=10 y=10 orientation=90 "
                + "keyup key=shift action=Abs\n"
                + "keydown key=a action=Flip\n"
                + "fire trigger=Flip action=Flip\n"; 
        Board board = BoardParser.parse(boardString);
        String expected = "Board: Example\n" + 
                "--| Gravity: 25.0\n" + 
                "--| Friction1: 0.025\n" + 
                "--| Friction2: 0.025\n" + 
                "--| Balls:\n" + 
                "--| staticBumpers:\n" + 
                "--| Absorbers:\n" + 
                "--| Flippers:\n" + 
                "-----| Flipper: Flip @ <10.0,10.0>, Rotated: Angle(0.0,1.0)at rotation Angle(1.0,0.0)\n" + 
                "--| Portals:\n" + 
                "\n" + 
                "--| fire:\n" + 
                "-----| Flip triggers [Flip]\n" + 
                "--| keyup:\n" + 
                "\n" + 
                "--| keydown:\n" + 
                "-----| A triggers [Flip]";
        assertEquals("Expected strings to match", expected, board.toString());

    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * Phase 1 tests
     */
    @Test(expected = AssertionError.class) public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    public static final float GRAVITY_DEFAULT = 25;
    public static final float FRICTION_DEFAULT = 0.025f;
    

    /*
     * covers: addBall, getBalls
     * Single ball, list size = 1
     */
    @Test public void testAddBallSingleBall() {
        Board board = new Board("board name");
        Ball newBall = new Ball("ball name", new Vect(10, 10), new Vect(5, 5));
        board.addBall(newBall);
        assertEquals("Expect number of balls in board to be 1", 1, board.getBalls().size());
    }

    /*
     * covers: addBall, getBalls
     * List size = 0
     */
    @Test public void testAddBallsNoBalls() {
        Board board = new Board("board name");
        assertEquals("Expected number of balls in board to be 0", 0, board.getBalls().size());
    }

    /*
     * covers: addBall, getBalls
     * list size = 1;
     */
    @Test public void testAddBallsOneBallInList() {
        Board board = new Board("board name");
        board.addBall(new Ball("ball name", new Vect(10, 10), new Vect(5, 5)));
        assertEquals("Expect number of balls in board to be 1", 1, board.getBalls().size());
    }

    /*
     * covers: addBall, getBalls
     * list size > 1
     */
    @Test public void testAddBAllsMultipleBallsInList() {
        Board board = new Board("board name");
        board.addBall(new Ball("ball1", new Vect(10, 10), new Vect(5, 5)));
        board.addBall(new Ball("ball1", new Vect(2, 10), new Vect(1, 0)));
        assertEquals("Expect number of balls in board to be 2", 2, board.getBalls().size());
    }

    /*
     * covers: addBumper, getStaticBumpers
     * single bumper, list size = 1
     */
    @Test public void testAddBumperSingleBumper() {
        Board board = new Board("board name");
        Bumper newBumper = new CircleBumper("circle", new Vect(3, 15));
        board.addBumper(newBumper);
        assertEquals("Expected number of bumpers in board to be 1", 1, board.getStaticBumpers().size());
    }

    /*
     * covers: addBumper, getStaticBumpers
     * list size = 0
     */
    @Test public void testAddBumperNoBumpers() {
        Board board = new Board("board name");
        assertEquals("Expected number of bumpers in board to be 0", 0, board.getStaticBumpers().size());
    }

    /*
     * covers: addBumper, getStaticBumpers
     * list size = 1
     */
    @Test public void testAddBumperOneBumperInList() {
        Board board = new Board("board name");
        board.addBumper(new CircleBumper("circle", new Vect(3, 15)));
        assertEquals("Expected number of bumpers in board to be 1", 1, board.getStaticBumpers().size());
    }

    /*
     * covers: addBumper, getStaticBumpers
     * list size > 1
     */
    @Test public void testAddBumperMultipleBumpers() {
        Board board = new Board("board name");
        board.addBumper(new CircleBumper("circle", new Vect(3, 15)));
        board.addBumper(new TriangleBumper("triangle", new Vect(5, 6), new Angle(0)));
        assertEquals("expect number of bumpers in board to be 2", 2, board.getStaticBumpers().size());
    }

    /*
     * covers: addAbsorber, getAbsobers
     * single Absorber, list size = 1
     */
    @Test public void testAddAbsorberSingle() {
        Board board = new Board("board");
        Absorber abs = new Absorber("abs", new Vect(0, 0), new Vect(5, 5));
        board.addAbsorber(abs);
        assertEquals("expected number of absorbers to be 1", 1, board.getAbsorbers().size());
    }

    /*
     * covers: addAbsorber, getAbsorber
     * list size = 0
     */
    @Test public void testAddAbsorberNone() {
        Board board = new Board("board");
        assertEquals("expect number of absorbers in board to be 0", 0, board.getAbsorbers().size());
    }

    /*
     * covers: addAbsorber, getAbsorber
     * list size = 1
     */
    @Test public void testAddAbsorberOneInList() {
        Board board = new Board("board");
        board.addAbsorber(new Absorber("abs1", new Vect(0, 0), new Vect(5, 5)));
        assertEquals("expect number of absorbers in board to be 1", 1, board.getAbsorbers().size());
    }

    /*
     * covers: addAbsorber, getAbsorber
     * list size = 2
     */
    @Test public void testAddAbsorberMultiple() {
        Board board = new Board("board");
        board.addAbsorber(new Absorber("abs1", new Vect(0, 0), new Vect(5, 5)));
        board.addAbsorber(new Absorber("abs2", new Vect(10, 10), new Vect(1, 1)));
        assertEquals("expect number of absorbers in board to be 2", 2, board.getAbsorbers().size());
    }

    /*
     * covers: getStaticGadgets
     * list size = 0
     */
    @Test public void testGetStaticGadgetsEmpty() {
        Board board = new Board("board");
        assertEquals("Expect empty board to have no gadgets", 0, board.getStaticGadgets().size());
    }

    /*
     * covers: getStaticGadgets
     * list size = 1
     */
    @Test public void testGetStaticGadgetsOne() {
        Board board = new Board("i'm board");
        Bumper newBumper = new CircleBumper("circle", new Vect(3, 15));
        board.addBumper(newBumper);
        assertEquals("Expected board to have 1 static gadget", 1, board.getStaticGadgets().size());
    }

    /*
     * covers: getStaticGadgets
     * list size > 1
     */
    @Test public void testGetStaticGadgetsMultiple() {
        Absorber abs=new Absorber("abs1", new Vect(0, 0), new Vect(5, 5));
        Bumper bump =new CircleBumper("circle", new Vect(3, 15));
        Ball ball=new Ball("ball name", new Vect(10, 10), new Vect(5, 5));
        
        Board board = new Board("bored");
        board.addBall(ball);
        board.addAbsorber(abs);
        board.addBumper(bump);
        assertEquals("expected board to have 2 static gadgets", 2, board.getStaticGadgets().size());
    }

    /*
     * covers: getGravity, getFriction1, getFriction2
     * gravity, friction1, friction2 = default (25 L/s^2, 0.025/s, 0.025/L
     * respectively)
     */
    @Test public void testGetGravityGetFriction1GetFriction2Default() {
        Board board = new Board("board");
        assertEquals("expected gravity to be default", GRAVITY_DEFAULT, board.getGravity(), 0.001);
        assertEquals("expected friction1 to be default", FRICTION_DEFAULT, board.getFriction1(), 0.001);
        assertEquals("expected friction2 to be default", FRICTION_DEFAULT, board.getFriction1(), 0.001);
    }

    /*
     * covers: getGravity, getFriction1, getFriction2
     * gravity, friction1, friction2 < default (25 L/s^2, 0.025/s, 0.025/L
     * respectively)
     */
    @Test public void testGetGravityGetFriction1GetFriction2LessThanDefault() {
        float mu1 = 0.005f; // friction1
        float mu2 = 0.003f; // friction2
        float gravity = 5;
        Board board = new Board("board", gravity, mu1, mu2);
        assertEquals("expected gravity to be 5", 5, board.getGravity(), 0.001);
        assertEquals("expected friction1 to be 0.005", 0.005, board.getFriction1(), 0.001);
        assertEquals("expected friction2 to be 0.003", 0.003, board.getFriction2(), 0.001);
    }

    /*
     * covers: getGravity, getFriction1, getFriction2
     * gravity, friction1, friction2 > default (25 L/s^2, 0.025/s, 0.025/L
     * respectively)
     */
    @Test public void testGetGravityGetFriction1GetFriction2MoreThanDefault() {
        float mu1 = 0.50f; // friction1
        float mu2 = 1f; // friction2
        float gravity = 50;
        Board board = new Board("board", gravity, mu1, mu2);
        assertEquals("expected gravity to be 50", 50, board.getGravity(), 0.001);
        assertEquals("expected friction1 to be 0.5", 0.5, board.getFriction1(), 0.001);
        assertEquals("expected friction2 to be 1", 1, board.getFriction2(), 0.001);
    }

    /*
     * covers: getName
     * lowercase
     */
    @Test public void testGetNameAllLowercase() {
        String name = "bored";
        Board board = new Board(name);
        assertEquals("expect strings to match", name, board.getName());
    }

    /*
     * covers: getName
     * uppercase
     */
    @Test public void testGetNameAllUppercase() {
        String name = "BOARD";
        Board board = new Board(name);
        assertEquals("expect strings to match", name, board.getName());
    }

    /*
     * covers: getName
     * mixed case
     */
    @Test public void testGetNameAllMixedCase() {
        String name = "Im Bored";
        Board board = new Board(name);
        assertEquals("expect strings to match", name, board.getName());
    }

    /*
     * covers: updateBoard
     * doesn't collide
     */
    @Test public void testUpdateBoardNoCollision() {
        final double time =Simulator.TIMER_INTERVAL_MILLISECONDS/(double)1000;
        Board board = new Board("board", 0, 0, 0); // no consideration of gravity, no friction
        Ball ball = new Ball("ball", new Vect(10, 10), new Vect(0, 1));
        board.addBall(ball);
        board.updateBoard(); // move the ball for one time step (.01)
        Vect expectedPosition = ball.getLocation().plus(ball.getVelocity().times(time));
        assertEquals("expect ball to have moved one time step", expectedPosition, board.getBalls().get(0).getLocation()); // only ball                                                                                              // so index
                                                                                                            // 0
    }
    
    /* covers: toString
     * # of gadgets = 0
     */
    @Test public void testBoardToStringEmptyBoard() {
        String boardName = "empty";
        Board emptyBoard = new Board(boardName);
        String expected = "Board: empty\n" + 
                "--| Gravity: 25.0\n" + 
                "--| Friction1: 0.025\n" + 
                "--| Friction2: 0.025\n" + 
                "--| Balls:\n" + 
                "--| staticBumpers:\n" + 
                "--| Absorbers:\n" + 
                "--| Flippers:\n" + 
                "--| Portals:\n" + 
                "\n" + 
                "--| fire:\n" + 
                "\n" + 
                "--| keyup:\n" + 
                "\n" + 
                "--| keydown:\n";
        assertEquals("Expected strings to match", expected, emptyBoard.toString());
    }
    
    /* covers: toString
     * # of gadgets = 1
     */
    @Test public void testBoardToStringOneGadget() {
        String boardName = "oneBall";
        Ball ball = new Ball("ball", new Vect(10,10), new Vect(0,5));

        Board oneBall = new Board(boardName);
        oneBall.addBall(ball);
        String expected = "Board: oneBall\n" + 
                "--| Gravity: 25.0\n" + 
                "--| Friction1: 0.025\n" + 
                "--| Friction2: 0.025\n" + 
                "--| Balls:\n" + 
                "-----| Ball: ball @ <10.0,10.0>, Velocity: <0.0,5.0>\n" + 
                "--| staticBumpers:\n" + 
                "--| Absorbers:\n" + 
                "--| Flippers:\n" + 
                "--| Portals:\n" + 
                "\n" + 
                "--| fire:\n" + 
                "\n" + 
                "--| keyup:\n" + 
                "\n" + 
                "--| keydown:\n";
        assertEquals("Expected strings to match", expected, oneBall.toString());

    }
    
    /* covers: toString
     * # of gadgets > 1
     */
    @Test public void testBoardToStringMultipleGadgets() {
        String boardName = "multi";
        String ballName = "ball";
        Ball ball = new Ball(ballName, new Vect(10,10), new Vect(0,5));
        String bumperName = "cB1";
        Bumper circleBumper = new CircleBumper(bumperName, new Vect(2,5));
        Board multi = new Board(boardName);
        multi.addBall(ball);
        multi.addBumper(circleBumper);
        String expected = "Board: multi\n" + 
                "--| Gravity: 25.0\n" + 
                "--| Friction1: 0.025\n" + 
                "--| Friction2: 0.025\n" + 
                "--| Balls:\n" + 
                "-----| Ball: ball @ <10.0,10.0>, Velocity: <0.0,5.0>\n" + 
                "--| staticBumpers:\n" + 
                "-----| Circle Bumper: cB1 @ <2.0,5.0>\n" + 
                "--| Absorbers:\n" + 
                "--| Flippers:\n" + 
                "--| Portals:\n" + 
                "\n" + 
                "--| fire:\n" + 
                "\n" + 
                "--| keyup:\n" + 
                "\n" + 
                "--| keydown:\n";
        assertEquals("Expected strings to match", expected, multi.toString());
    }
    
    // Test drawBoard
    @Test
    public void testDrawBoard() {
        final Board basicBoard = new Board("Basic Board");
        final Graphics board = basicBoard.drawBackground().getGraphics();
        assertEquals("Board color should be pink", Color.WHITE, board.getColor());
    }
}
