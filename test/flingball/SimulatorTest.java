package flingball;

import org.junit.Test;

import edu.mit.eecs.parserlib.UnableToParseException;
import physics.Vect;

/**
 * Tests the functionality and correctness of the Simulator Class.
 * 
 * @category no_didit
 */
public class SimulatorTest {
   
    
    /* 
     * Testing Strategy for Simulator
     * 
     * phase 2
     * Tests are tested visually, to check if the gadgets' behaviors are correct.
     * 
     * gadget type:
     * bumper, portal, flipper
     *
     * Flipper related fire:
     *      key trigger, gadget trigger
     *         any key triggers flipper
     *         stationary object (circle, triangle, square) trigger flipper
     *         absorber trigger flipper
     *      flipper can trigger absorber, flipper
     *        flipper self trigger
     *        flipper triggers other flipper
     *        flipper trigger absorber
     * 
     * ball-flipper interaction:
     *      ball hitting flipper when flipper moving BUG
     *      ball hitting flipper when flipper is not moving
     * 
     * ball-portal action:
     *      action, no action
     * 
     * phase 1
     * 
     * Simulator was tested visually in a combination of the other tests suites
     * While manually testing, looked at:
     *  - whether the ball was moving in expected direction
     *  - whether the angle of reflection was accurate/the collisions were functioning
     *  - whether balls would come to rest when they're supposed to be at rest
     *  - whether balls that were supposed to be at rest on gadgets, stayed where they were
     *  - whether it was moving at the right frame rate
     *  - whether when the absorbers fire that the ball has enough velocity to hit the top wall
     *  - when absorbers are triggered, that they exhibit the correct behaviour (releasing a ball if they have a ball from lower right corner)
     */
    
    @Test(expected = AssertionError.class) public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    // Testing strategy
    // test simulator with ball
    @Test public void testBoardBall() throws InterruptedException {
        Board board = new Board("board");
        board.addBall(new Ball("ball",new Vect(5,12),new Vect(10,20)));
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(2000);
    }
 
    // test simulator with ball
    @Test public void testBoardBumper() throws InterruptedException, UnableToParseException {
        final String boardString = "board name=Example\n squareBumper name=Square x=10 y=10\n";
        Board board = BoardParser.parse(boardString);
        board.addBall(new Ball("ball",new Vect(10,5),new Vect(0,20)));
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(20000);
    }
    
    // test simulator with ball
    @Test public void testBallball() throws InterruptedException, UnableToParseException {
        Board board = new Board("b",0,0,0);
        board.addBall(new Ball("ball",new Vect(10,5),new Vect(3,0)));
        board.addBall(new Ball("ball",new Vect(17,5),new Vect(30,0)));
        board.addBall(new Ball("ball",new Vect(10,15),new Vect(3,0)));
        board.addBall(new Ball("ball",new Vect(17,15),new Vect(-3,0)));
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(20000);
    }
    
    // test absorber, flipper triggered by key, and trigger by itself
    @Test public void testFlipperSelfTriggerOppositeSide() throws InterruptedException, UnableToParseException {
        final String boardString = "board name=Example\n "
                + "leftFlipper name=Flip x=10 y=10 orientation=90 "
                + "keyup key=shift action=Abs\n"
                + "keydown key=backslash action=Flip\n"
                + "fire trigger=Flip action=Flip\n"; 
        Board board = BoardParser.parse(boardString);
        board.addBall(new Ball("ball",new Vect(10.5,8),new Vect(0,0)));
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(20000);
    }


    // test portal
    @Test public void testPortal() throws InterruptedException, UnableToParseException {
        final String boardString = "board name=Example\n "
                + "portal name=p1 x=6 y=16 otherPortal = p2 "
                + "portal name=p2 x=12 y=16 otherPortal = p1 ";
        Board board = BoardParser.parse(boardString);
        board.addBall(new Ball("ball",new Vect(6,8),new Vect(0,0)));
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(20000);
    }
    
        
    // test absorber trigger flipper
    // test circle trigger flipper
    @Test public void testCircleAndAbsorberTriggerFlipper() throws InterruptedException, UnableToParseException {

        final String boardString = "board name=Flippers gravity = 25.0\n" + 
                "\n" + 
                "# define a ball\n" + 
                "ball name=BallA x=0.25 y=3.25 xVelocity=0 yVelocity=0\n" + 
                "ball name=BallB x=5.25 y=3.25 xVelocity=0 yVelocity=0 \n" + 
                "ball name=BallC x=10.25 y=3.25 xVelocity=0 yVelocity=0 \n" + 
                "ball name=BallD x=15.25 y=3.25 xVelocity=0 yVelocity=0 \n" + 
                "ball name=BallE x=19.25 y=3.25 xVelocity=0 yVelocity=0 \n" + 
                "\n" + 
                "# define some left flippers\n" + 
                "leftFlipper name=FlipA x=0 y=8 orientation=90 \n" + 
                "leftFlipper name=FlipB x=4 y=10 orientation=90 \n" + 
                "leftFlipper name=FlipC x=9 y=8 orientation=90\n" + 
                "leftFlipper name=FlipD x=15 y=8 orientation=90\n" + 
                "\n" + 
                "# define some right flippers \n" + 
                "rightFlipper name=FlipE x=2 y=15 orientation=0\n" + 
                "rightFlipper name=FlipF x=17 y=15 orientation=0\n" + 
                "\n" + 
                "# define some circle bumpers\n" + 
                "circleBumper name=CircleA x=5 y=18\n" + 
                "circleBumper name=CircleB x=7 y=13\n" + 
                "circleBumper name=CircleC x=0 y=5\n" + 
                "circleBumper name=CircleD x=5 y=5\n" + 
                "circleBumper name=CircleE x=10 y=5\n" + 
                "circleBumper name=CircleF x=15 y=5\n" + 
                "\n" + 
                "# define some triangle bumpers\n" + 
                "triangleBumper name=TriA x=19 y=0 orientation=90\n" + 
                "triangleBumper name=TriB x=10 y=18 orientation=180\n" + 
                "\n" + 
                "# define an absorber\n" + 
                "absorber name=Abs x=0 y=19 width=20 height=1 \n" + 
                "\n" + 
                "\n" + 
                "# define events between gizmos\n" + 
                "fire trigger=CircleC action=FlipA\n" + 
                "fire trigger=CircleE action=FlipC\n" + 
                "fire trigger=CircleF action=FlipD\n" + 
                "fire trigger=Abs action=FlipE\n" + 
                "fire trigger=Abs action=FlipF\n" + 
                "fire trigger=Abs action=Abs\n" + 
                "\n";
        Board board = BoardParser.parse(boardString);
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(200000);
    }
    
    // test flipper triggers an absorber
    @Test public void testFlipperTriggersAnAbsorber() throws InterruptedException, UnableToParseException {
        final String boardString = "board name=Example\n "
                + "rightFlipper name=Flip x=10 y=10 orientation=90 "
                + "absorber name=Abs x=0 y=19 width=20 height=1 \n"
                + "keyup key=shift action=Abs\n"
                + "keydown key=backslash action=Flip\n"
                + "fire trigger=Flip action=Abs\n"; 

        Board board = BoardParser.parse(boardString);
        board.addBall(new Ball("ball",new Vect(10.5,8),new Vect(0,0)));
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(20000);
    }
    

    // test absorber, flipper triggered by key, and trigger by itself
    // test hitting the ball while flipper is in motion!!!!!!
    @Test public void testFlipperSelfTriggerSideOfTheBall() throws InterruptedException, UnableToParseException {
        final String boardString = "board name=Example\n"
                + "rightFlipper name=Flip x=10 y=17 orientation=90 "
                + "leftFlipper name=Flip2 x=5 y=17 orientation=270 "
                + "keyup key=shift action=Abs\n"
                + "keydown key=backslash action=Flip\n"
                + "keydown key=backslash action=Flip2\n"
                + "fire trigger=Flip action=Flip\n"
                + "fire trigger=Flip2 action=Flip2\n";

        Board board = BoardParser.parse(boardString);
        board.addBall(new Ball("ball",new Vect(10.5,17.5),new Vect(0,0)));
        board.addBall(new Ball("ball",new Vect(5.5,17.5),new Vect(0,0)));
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(200000);
    }
    
    // flipper triggered by square
    @Test public void testTriggersExample() throws InterruptedException, UnableToParseException {
        final String boardString = "board name=Triggers gravity = 10.0\n" + 
                "\n" + 
                "# define a ball\n" + 
                "ball name=BallA x=1.8 y=4.5 xVelocity=10.4 yVelocity=10.3 \n" + 
                "ball name=BallB x=10.0 y=13.0 xVelocity=-3.4 yVelocity=-2.3 \n" + 
                "\n" + 
                "# define some bumpers\n" + 
                "squareBumper name=Square x=0 y=10\n" + 
                "squareBumper name=SquareB x=1 y=10\n" + 
                "squareBumper name=SquareC x=2 y=10\n" + 
                "squareBumper name=SquareD x=3 y=10\n" + 
                "squareBumper name=SquareE x=4 y=10\n" + 
                "squareBumper name=SquareF x=5 y=10\n" + 
                "squareBumper name=SquareG x=6 y=10\n" + 
                "squareBumper name=SquareH x=7 y=10\n" + 
                "\n" + 
                "circleBumper name=Circle x=4 y=3\n" + 
                "triangleBumper name=Tri x=19 y=3 orientation=90\n" + 
                "\n" + 
                "\n" + 
                "# define some flippers\n" + 
                "  leftFlipper name=FlipL x=10 y=7 orientation=0 \n" + 
                "rightFlipper name=FlipR x=12 y=7 orientation=0\n" + 
                "\n" + 
                "\n" + 
                "# define an absorber to catch the ball\n" + 
                " absorber name=Abs x=10 y=17 width=10 height=2 \n" + 
                "\n" + 
                "# define events between gizmos\n" + 
                "fire trigger=Square action=FlipL\n" + 
                "fire trigger=SquareB action=FlipL\n" + 
                "fire trigger=SquareC action=FlipL\n" + 
                "fire trigger=SquareD action=FlipL\n" + 
                "fire trigger=SquareE action=FlipL\n" + 
                "fire trigger=SquareF action=FlipL\n" + 
                "fire trigger=SquareG action=FlipL\n" + 
                "fire trigger=SquareH action=FlipL\n" + 
                "\n" + 
                "# make the absorber self-triggering\n" + 
                " fire trigger=Abs action=Abs ";
        Board board = BoardParser.parse(boardString);
        Simulator simulator=new Simulator(board);
        simulator.makeAndShowGUI();
        Thread.sleep(200000);
    }
        
}
