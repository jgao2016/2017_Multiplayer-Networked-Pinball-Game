package flingball;

import static org.junit.Assert.assertEquals;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import edu.mit.eecs.parserlib.UnableToParseException;
import physics.Angle;
import physics.Vect;

public class ParserTest {

    private final static double DELTA = 0.001;

    @Test(expected = AssertionError.class) public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // Testing strategy
    // partition on the board:
    // board is empty, board is not empty
    // board has / doesn't have specified gravity, friction1 and friction2
    // board has / doesn't have comments
    // board has / doesn't have balls
    // board has / doesn't have squareBumpers
    // board has / doesn't have circleBumpers
    // board doesn't have triangleBumpers,
    // has triangleBumpers without specified orientation
    // has triangleBumpers with specified orientation
    // board has / doesn't have absorber
    // board doesn't have fire trigger, have some gadgets that triggers themselves,
    // have some gadgets that triggers other gadgets

    // this test covers board is empty
    @Test public void testParseEmptyBoard() throws UnableToParseException {
        final String board = "board name=Example\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected name", "Example", parsedBoard.getName());
        assertEquals("expected gravity", 25.0f, parsedBoard.getGravity(), DELTA);
        assertEquals("expected friction1", 0.025f, parsedBoard.getFriction1(), DELTA);
        assertEquals("expected friction2", 0.025f, parsedBoard.getFriction2(), DELTA);
        assertEquals("expected zero static gadgets", 0, parsedBoard.getStaticGadgets().size());
        assertEquals("expected zero balls", 0, parsedBoard.getBalls().size());
    }

    // this test covers board is empty, has specified gravity, friction1 and
    // friction2
    @Test public void testParseEmptyBoardWithGravityFriction() throws UnableToParseException {
        final String board = "board name=Example gravity=10 friction1=0.2 friction2=0.1\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected name", "Example", parsedBoard.getName());
        assertEquals("expected gravity", 10.0f, parsedBoard.getGravity(), DELTA);
        assertEquals("expected friction1", 0.2f, parsedBoard.getFriction1(), DELTA);
        assertEquals("expected friction2", 0.1f, parsedBoard.getFriction2(), DELTA);
        assertEquals("expected zero static gadgets", 0, parsedBoard.getStaticGadgets().size());
        assertEquals("expected zero balls", 0, parsedBoard.getBalls().size());
    }

    // this test covers board is empty with comment
    @Test public void testParseEmptyBoardWithComment() throws UnableToParseException {
        final String board = "board name=Example\n #gravity=10 friction1=0.1\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected name", "Example", parsedBoard.getName());
        assertEquals("expected gravity", 25.0f, parsedBoard.getGravity(), DELTA);
        assertEquals("expected friction1", 0.025f, parsedBoard.getFriction1(), DELTA);
        assertEquals("expected friction2", 0.025f, parsedBoard.getFriction2(), DELTA);
        assertEquals("expected zero static gadgets", 0, parsedBoard.getStaticGadgets().size());
        assertEquals("expected zero balls", 0, parsedBoard.getBalls().size());
    }

    // this test covers board has ball
    @Test public void testParseBall() throws UnableToParseException {
        final String board = "board name=Example\n ball name=Ball x=1.8 y=4.5 xVelocity=-3.4 yVelocity=-2.3\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected zero static gadgets", 0, parsedBoard.getStaticGadgets().size());
        assertEquals("expected one ball", 1, parsedBoard.getBalls().size());
        assertEquals("expected ball", new Ball("Ball", new Vect(1.8F, 4.5F), new Vect(-3.4F, -2.3F)),
                parsedBoard.getBalls().get(0));
    }

    // this test covers board has SquareBumper
    @Test public void testParseSquareBumper() throws UnableToParseException {
        final String board = "board name=Example\n squareBumper name=Square x=0 y=2\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected one bumper", 1, parsedBoard.getStaticBumpers().size());
        assertEquals("expected bumper", new SquareBumper("Square", new Vect(0, 2)), parsedBoard.getStaticBumpers().get(0));
        assertEquals("expected zero other gadgets", 0,
                parsedBoard.getBalls().size() + parsedBoard.getAbsorbers().size());
    }

    // this test covers board has SquareBumper
    @Test public void testParseCircleBumper() throws UnableToParseException {
        final String board = "board name=Example\n circleBumper name=circle x=0 y=2\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected one bumper", 1, parsedBoard.getStaticBumpers().size());
        assertEquals("expected bumper", new CircleBumper("circle", new Vect(0, 2)), parsedBoard.getStaticBumpers().get(0));
        assertEquals("expected zero other gadgets", 0,
                parsedBoard.getBalls().size() + parsedBoard.getAbsorbers().size());
    }

    // this test covers board has triangleBumpers without specified orientation
    @Test public void testParseTriangleBumpers() throws UnableToParseException {
        final String board = "board name=Example\n triangleBumper name=Triangle x=0 y=2\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected one bumper", 1, parsedBoard.getStaticBumpers().size());
        assertEquals("expected bumper", new TriangleBumper("Triangle", new Vect(0, 2), new Angle(0)),
                parsedBoard.getStaticBumpers().get(0));
        assertEquals("expected zero other gadgets", 0,
                parsedBoard.getBalls().size() + parsedBoard.getAbsorbers().size());
    }

    // this test covers board has triangleBumpers with specified orientation
    @Test public void testParseTriangleBumpersWithOrientation() throws UnableToParseException {
        final String board = "board name=Example\n triangleBumper name=Triangle x=0 y=2 orientation=90\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected one bumper", 1, parsedBoard.getStaticBumpers().size());
        assertEquals("expected bumper", new TriangleBumper("Triangle", new Vect(0, 2), Angle.DEG_90),
                parsedBoard.getStaticBumpers().get(0));
        assertEquals("expected zero other gadgets", 0,
                parsedBoard.getBalls().size() + parsedBoard.getAbsorbers().size());
    }

    // this test covers board has Absorber
    @Test public void testParseAbsorber() throws UnableToParseException {
        final String board = "board name=Example\n absorber name=Abs x=0 y=2 width=20 height=1 \n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected one Absorber", 1, parsedBoard.getAbsorbers().size());
        assertEquals("expected Absorber", new Absorber("Abs", new Vect(0, 2), new Vect(20, 1)),
                parsedBoard.getAbsorbers().get(0));
        assertEquals("expected zero other gadgets", 0, parsedBoard.getBalls().size() + parsedBoard.getStaticBumpers().size());
    }

    // this test covers board has some gadgets that triggers themselves
    @Test public void testParseAbsorberTriggeredByItself() throws UnableToParseException {
        final String board = "board name=Example\n absorber name=Abs x=0 y=19 width=20 height=1 \nfire trigger=Abs action=Abs\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected one Absorber", 1, parsedBoard.getAbsorbers().size());
        Absorber abs = parsedBoard.getAbsorbers().get(0);
        parsedBoard.setTarget(abs, abs);

        assertEquals("expected fire trigger and action", abs, parsedBoard.getTargetAbsorbers(abs).get(0));
    }

    // this test covers board has some gadgets that triggers other gadget
    @Test public void testParseAbsorberTriggeredByOther() throws UnableToParseException {
        final String board = "board name=Example\n absorber name=Abs x=0 y=19 width=20 height=1 \n"
                + "squareBumper name=Square x=0 y=2\n fire trigger=Square action=Abs\n";
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected one Absorber", 1, parsedBoard.getAbsorbers().size());
        assertEquals("expected one bumper", 1, parsedBoard.getStaticBumpers().size());
        Absorber abs = parsedBoard.getAbsorbers().get(0);
        SquareBumper square = (SquareBumper) parsedBoard.getStaticBumpers().get(0);
        parsedBoard.setTarget(abs, square);

        assertEquals("expected fire trigger and action", abs, parsedBoard.getTargetAbsorbers(square).get(0));
    }
    
    // this test covers boards with more gadgets and balls
    @Test public void testParseSample() throws UnableToParseException, IOException {
        File sampleBoardFile = new File("boards/sampleBoard.fb");
        final String board = Flingball.boardFileToString(sampleBoardFile);
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected board", 1, parsedBoard.getBalls().size());
        assertEquals("expected board", 3, parsedBoard.getStaticBumpers().size());
        assertEquals("expected board", 1, parsedBoard.getAbsorbers().size());
        
        File sampleBoardFile2 = new File("boards/sampleBoard2.fb");
        final String board2 = Flingball.boardFileToString(sampleBoardFile2);
        Board parsedBoard2 = BoardParser.parse(board2);
        assertEquals("expected board", 3, parsedBoard2.getBalls().size());
        assertEquals("expected board", 8, parsedBoard2.getStaticBumpers().size());
        assertEquals("expected board", 2, parsedBoard2.getAbsorbers().size());
        
        File sampleBoardFile3 = new File("boards/sampleBoard3.fb");
        final String board3 = Flingball.boardFileToString(sampleBoardFile3);
        Board parsedBoard3 = BoardParser.parse(board3);
        assertEquals("expected board", 1, parsedBoard3.getBalls().size());
        assertEquals("expected board", 8, parsedBoard3.getStaticBumpers().size());
        assertEquals("expected board", 0, parsedBoard3.getAbsorbers().size());
    }
    
    // this test covers boards with flippers
    @Test public void testParseFlipper() throws UnableToParseException, IOException {
        File sampleBoardFile = new File("boards/flippers.fb");
        final String board = Flingball.boardFileToString(sampleBoardFile);
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected board", 5, parsedBoard.getBalls().size());
        assertEquals("expected board", 8, parsedBoard.getStaticBumpers().size());
        assertEquals("expected board", 6, parsedBoard.getFlippers().size());
        assertEquals("expected board", 1, parsedBoard.getAbsorbers().size());
        // leftFlipper name=FlipA x=0 y=8 orientation=90 
        Flipper flipper = new Flipper("FlipA", new Vect(0,8), Angle.DEG_90, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect the right first flipper", flipper, parsedBoard.getFlippers().get(0));
    }
    
    // this test covers boards with portals
    @Test public void testParsePortal() throws UnableToParseException, IOException {
        File sampleBoardFile = new File("boards/portals.fb");
        final String board = Flingball.boardFileToString(sampleBoardFile);
        Board parsedBoard = BoardParser.parse(board);
        assertEquals("expected board", 2, parsedBoard.getPortals().size());
        // portal name=p1 x=1 y=1 otherPortal=p2
        // portal name=p2 x=5 y=5 otherPortal=p1
        // String name, String otherPortal, String toBoard, Vect location, Board board
        Portal p1 = new Portal("p1", "p2", "Example", new Vect(1,1));
        Portal p2 = new Portal("p2", "p1", "Example", new Vect(5,5));
        assertEquals("Expect the first portal", p1, parsedBoard.getPortals().get(0));
        assertEquals("Expect the second portal", p2, parsedBoard.getPortals().get(1));
    }
    // this test covers boards with portals
    @Test public void testParseKeyFire() throws UnableToParseException, IOException {
        File sampleBoardFile = new File("boards/key.fb");
        final String board = Flingball.boardFileToString(sampleBoardFile);
        Board parsedBoard = BoardParser.parse(board);
        /*
         * fire trigger=CircleF action=Abs
         * fire trigger=Abs action=FlipB
         * fire trigger=FlipB action=FlipA
         * fire trigger=p1 action=Abs
         * 
         * keydown key=a action=FlipA
         * keyup key=b action=Abs
         * keyup key=space action=FlipA
         * keyup key=space action=FlipB
         */
        Absorber abs=(Absorber)parsedBoard.getGadgetWithName("Abs");
        Bumper circleF=(Bumper)parsedBoard.getGadgetWithName("CircleF");
        Flipper flipB=(Flipper)parsedBoard.getGadgetWithName("FlipB");
        Flipper flipA=(Flipper)parsedBoard.getGadgetWithName("FlipA");
        int keyA=KeyEvent.VK_A;
        int keyB=KeyEvent.VK_B;
        int keySpace=KeyEvent.VK_SPACE;
        System.out.println(parsedBoard);
        assertEquals("Expect fire", abs, parsedBoard.getTargetAbsorbers(circleF).get(0));
        assertEquals("Expect fire", flipB, parsedBoard.getTargetFlippers(abs).get(0));
        assertEquals("Expect fire", flipA, parsedBoard.getTargetFlippers(flipB).get(0));
        assertEquals("Expect keydown", flipA, parsedBoard.getTargetFlippers(keyA,false).get(0));
        assertEquals("Expect keyup", abs, parsedBoard.getTargetAbsorbers(keyB,true).get(0));
        assertEquals("Expect keyup", flipA, parsedBoard.getTargetFlippers(keySpace,true).get(0));
    }
}
