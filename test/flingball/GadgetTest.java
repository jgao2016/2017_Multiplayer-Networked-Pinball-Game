package flingball;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import org.junit.Test;

import physics.Angle;
import physics.Vect;

public class GadgetTest {
    // phase 2 tests
    /*
     * Testing Strategy:
     * 
     * Flipper:
     * 
     * action:
     * flipper is left/ right, orientation is 0/90/180/270
     * 
     * getRotation:
     * flipper is left/ right, orientation is 0/90/180/270
     * has not yet started rotating, started rotating, finished rotating
     * 
     * getAngle:
     * flipper is left/ right, orientation is 0/90/180/270
     * has not yet started rotating, started rotating, finished rotating
     * 
     * getFlipperVariation:
     * left, right
     * 
     * getLocation:
     * Location Vect X position = 0, 0 < x < 19, 19
     * Location Vect Y position = 0, 0 < y< 19, 19
     * 
     * getCollisionRedirection:
     * flipper is rotating, not rotating
     * ball collides with flipper or not
     * 
     * triggered:
     * get triggered by given ball, not triggered
     * 
     * equals:
     * equals, not equals
     * 
     * Portal:
     * 
     * setTargetPortal:
     * otherPortal is itself, is not itself
     * 
     * hasPortal
     * result is true, false
     * 
     * getTargetPortal
     * otherPortal is itself, is not itself
     * 
     * getLocation
     * 
     * triggered:
     * triggered, not triggered
     * 
     * equals:
     * equals, not equals
     */

    /* covers: flipper, action, getRotation, getAngle
     * flipper is left, orientation is 0
     * has not yet started rotating, started rotating, finished rotating
     */
    @Test public void testFlipperActionLeft1( ) {
        Board board = new Board("b1");
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.ZERO, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect flipper rotation", 0, Math.toDegrees(flipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", 0, Math.toDegrees(flipper.getAngle().radians()),0.001);
        board.addFlipper(flipper);
        board.setTarget(flipper, KeyEvent.VK_UP, false);
        board.keyPressed(KeyEvent.VK_UP);
        board.updateBoard();
        Flipper newFlipper = board.getFlippers().get(0);
        final int FLIPPER_DEGREE_PER_SECOND=1080;
        final int MILLISECOND_PER_SECOND = 1000;
        double angle = FLIPPER_DEGREE_PER_SECOND/(double)MILLISECOND_PER_SECOND * Simulator.TIMER_INTERVAL_MILLISECONDS;
        assertEquals("Expect flipper rotation", 0, Math.toDegrees(newFlipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", -angle, Math.toDegrees(newFlipper.getAngle().radians()),0.001);
    }
    /* covers: flipper, action, getRotation, getAngle
     * flipper is right, orientation is 90
     * has not yet started rotating, started rotating, finished rotating
     */
    @Test public void testFlipperActionLeft2( ) {
        Board board = new Board("b1");
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_90, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect flipper rotation", 90, Math.toDegrees(flipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", 0, Math.toDegrees(flipper.getAngle().radians()),0.001);
        board.addFlipper(flipper);
        board.setTarget(flipper, KeyEvent.VK_UP, false);
        board.keyPressed(KeyEvent.VK_UP);
        board.updateBoard();
        Flipper newFlipper = board.getFlippers().get(0);
        final int FLIPPER_DEGREE_PER_SECOND=1080;
        final int MILLISECOND_PER_SECOND = 1000;
        double angle = FLIPPER_DEGREE_PER_SECOND/(double)MILLISECOND_PER_SECOND * Simulator.TIMER_INTERVAL_MILLISECONDS;
        assertEquals("Expect flipper rotation", 90, Math.toDegrees(newFlipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", -angle, Math.toDegrees(newFlipper.getAngle().radians()),0.001);
    }
    /* covers: flipper, action, getRotation, getAngle
     * flipper is left, orientation is 180
     * has not yet started rotating, started rotating, finished rotating
     */
    @Test public void testFlipperActionLeft3( ) {
        Board board = new Board("b1");
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_180, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect flipper rotation", 180, Math.toDegrees(flipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", 0, Math.toDegrees(flipper.getAngle().radians()),0.001);
        board.addFlipper(flipper);
        board.setTarget(flipper, KeyEvent.VK_UP, false);
        board.keyPressed(KeyEvent.VK_UP);
        board.updateBoard();
        Flipper newFlipper = board.getFlippers().get(0);
        final int FLIPPER_DEGREE_PER_SECOND=1080;
        final int MILLISECOND_PER_SECOND = 1000;
        double angle = FLIPPER_DEGREE_PER_SECOND/(double)MILLISECOND_PER_SECOND * Simulator.TIMER_INTERVAL_MILLISECONDS;
        assertEquals("Expect flipper rotation", 180, Math.toDegrees(newFlipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", -angle, Math.toDegrees(newFlipper.getAngle().radians()),0.001);
    }
    /* covers: flipper, action, getRotation, getAngle
     * flipper is left, orientation is 270
     * has not yet started rotating, started rotating, finished rotating
     */
    @Test public void testFlipperActionLeft4( ) {
        Board board = new Board("b1");
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_270, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect flipper rotation", -90, Math.toDegrees(flipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", 0, Math.toDegrees(flipper.getAngle().radians()),0.001);
        board.addFlipper(flipper);
        board.setTarget(flipper, KeyEvent.VK_UP, true);
        board.keyReleased(KeyEvent.VK_UP);
        board.updateBoard();
        Flipper newFlipper = board.getFlippers().get(0);
        final int FLIPPER_DEGREE_PER_SECOND=1080;
        final int MILLISECOND_PER_SECOND = 1000;
        double angle = FLIPPER_DEGREE_PER_SECOND/(double)MILLISECOND_PER_SECOND * Simulator.TIMER_INTERVAL_MILLISECONDS;
        assertEquals("Expect flipper rotation", -90, Math.toDegrees(newFlipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", -angle, Math.toDegrees(newFlipper.getAngle().radians()),0.001);

    }
    /* covers: flipper, action, getRotation, getAngle
     * flipper is right, orientation is 0
     * has not yet started rotating, started rotating, finished rotating
     */
    @Test public void testFlipperActionRight1( ) {
        Board board = new Board("b1");
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.ZERO, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect flipper rotation", 0, Math.toDegrees(flipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", 0, Math.toDegrees(flipper.getAngle().radians()),0.001);
        board.addFlipper(flipper);
        board.setTarget(flipper, KeyEvent.VK_UP, true);
        board.keyReleased(KeyEvent.VK_UP);
        board.updateBoard();
        Flipper newFlipper = board.getFlippers().get(0);
        final int FLIPPER_DEGREE_PER_SECOND=1080;
        final int MILLISECOND_PER_SECOND = 1000;
        double angle = FLIPPER_DEGREE_PER_SECOND/(double)MILLISECOND_PER_SECOND * Simulator.TIMER_INTERVAL_MILLISECONDS;
        assertEquals("Expect flipper rotation", 0, Math.toDegrees(newFlipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", -angle, Math.toDegrees(newFlipper.getAngle().radians()),0.001);
    }
    /* covers: flipper, action, getRotation, getAngle
     * flipper is right, orientation is 90 
     * has not yet started rotating, started rotating, finished rotating
     */
    @Test public void testFlipperActionRight2( ) {
        Board board = new Board("b1");
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_90, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect flipper rotation", 90, Math.toDegrees(flipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", 0, Math.toDegrees(flipper.getAngle().radians()),0.001);
        board.addFlipper(flipper);
        board.setTarget(flipper, KeyEvent.VK_UP, true);
        board.keyReleased(KeyEvent.VK_UP);
        board.updateBoard();
        Flipper newFlipper = board.getFlippers().get(0);
        final int FLIPPER_DEGREE_PER_SECOND=1080;
        final int MILLISECOND_PER_SECOND = 1000;
        double angle = FLIPPER_DEGREE_PER_SECOND/(double)MILLISECOND_PER_SECOND * Simulator.TIMER_INTERVAL_MILLISECONDS;
        assertEquals("Expect flipper rotation", 90, Math.toDegrees(newFlipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", -angle, Math.toDegrees(newFlipper.getAngle().radians()),0.001);

    }
    /* covers: flipper, action, getRotation, getAngle
     * flipper is right, orientation is 180
     * has not yet started rotating, started rotating, finished rotating
     */
    @Test public void testFlipperActionRight3( ) {
        Board board = new Board("b1");
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_180, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect flipper rotation", 180, Math.toDegrees(flipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", 0, Math.toDegrees(flipper.getAngle().radians()),0.001);
        board.addFlipper(flipper);
        board.setTarget(flipper, KeyEvent.VK_UP, true);
        board.keyReleased(KeyEvent.VK_UP);
        board.updateBoard();
        Flipper newFlipper = board.getFlippers().get(0);
        final int FLIPPER_DEGREE_PER_SECOND=1080;
        final int MILLISECOND_PER_SECOND = 1000;
        double angle = FLIPPER_DEGREE_PER_SECOND/(double)MILLISECOND_PER_SECOND * Simulator.TIMER_INTERVAL_MILLISECONDS;
        assertEquals("Expect flipper rotation", 180, Math.toDegrees(newFlipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", -angle, Math.toDegrees(newFlipper.getAngle().radians()),0.001);

    }
    /* covers: flipper, action, getRotation, getAngle
     * flipper is right, orientation is 270
     * has not yet started rotating, started rotating, finished rotating
     */
    @Test public void testFlipperActionRight4( ) {
        Board board = new Board("b1");
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_270, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect flipper rotation", -90, Math.toDegrees(flipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", 0, Math.toDegrees(flipper.getAngle().radians()),0.001);
        board.addFlipper(flipper);
        board.setTarget(flipper, KeyEvent.VK_UP, true);
        board.keyReleased(KeyEvent.VK_UP);
        board.updateBoard();
        Flipper newFlipper = board.getFlippers().get(0);
        final int FLIPPER_DEGREE_PER_SECOND=1080;
        final int MILLISECOND_PER_SECOND = 1000;
        double angle = FLIPPER_DEGREE_PER_SECOND/(double)MILLISECOND_PER_SECOND * Simulator.TIMER_INTERVAL_MILLISECONDS;
        assertEquals("Expect flipper rotation", -90, Math.toDegrees(newFlipper.getRotation().radians()),0.001);
        assertEquals("Expect flipper angle", -angle, Math.toDegrees(newFlipper.getAngle().radians()),0.001);
    }
    /* 
     * covers:getLocation:            
     */
    @Test public void testFlipperLocation( ) {
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_270, FlipperLeftOrRight.LEFT_FLIPPER);
        assertEquals("Expect flipper location", 1, flipper.getLocation().x(),0.0001);
        assertEquals("Expect flipper location", 1, flipper.getLocation().y(),0.0001);        
    }

    /* 
     * covers:getCollisionRedirection:
     * flipper not rotating
     * ball doesn't collide with flipper
     * triggered:
     * not triggered
     */
    @Test public void testFlipperGetCollisionRedirectionAndTriggered( ) {
        Flipper flipper=new Flipper("f1", new Vect(1,1), Angle.DEG_270, FlipperLeftOrRight.LEFT_FLIPPER);
        Ball ball = new Ball("ball", new Vect(10,10), new Vect(5,5));
        assertEquals("Expect not triggered", false, flipper.triggered(ball));
        assertEquals("Expect same ball", ball, flipper.getCollisionRedirection(ball));
    }
    /* 
     * covers:getCollisionRedirection:
     * flipper is rotating
     * ball collide with flipper
     * triggered: is triggered
     */
    @Test public void testFlipperGetCollisionRedirectionAndTriggered2( ) throws InterruptedException {
        Board board = new Board(" ");
        Flipper flipper=new Flipper("f1", new Vect(10,10), Angle.DEG_90, FlipperLeftOrRight.RIGHT_FLIPPER);
        Ball ball = new Ball("ball", new Vect(11.6,11.6), new Vect(10,10));
        board.addBall(ball);
        board.addFlipper(flipper);
        assertEquals("Expect triggered", true, flipper.triggered(ball));
        assertEquals("Expect reflected ball", ball.getName(), flipper.getCollisionRedirection(ball).getName());
        assertEquals("Expect reflected ball", ball.getLocation(), flipper.getCollisionRedirection(ball).getLocation());
        assertEquals("Expect reflected ball", 10, flipper.getCollisionRedirection(ball).getVelocity().x(),0.001);
        assertEquals("Expect reflected ball", -9.5, flipper.getCollisionRedirection(ball).getVelocity().y(),0.001);
        
    }

    
    /*covers flipper, equals:
     *equals, not equals
     */
    @Test public void testFlipperEquals( ) {
        Flipper flipper=new Flipper("f1", new Vect(10,10), Angle.ZERO, FlipperLeftOrRight.RIGHT_FLIPPER);
        Flipper flipper2=new Flipper("f1", new Vect(10,10), Angle.ZERO, FlipperLeftOrRight.RIGHT_FLIPPER);
        Flipper flipper3=new Flipper("f1", new Vect(10,10), Angle.DEG_180, FlipperLeftOrRight.RIGHT_FLIPPER);
        assertEquals("Expect equals",true,flipper.equals(flipper2));
        assertEquals("Expect not equals",false,flipper.equals(flipper3));
    }
    /*
     * covers portal 
      
     * otherPortal is itself
     * hasPortal: true, false
     */
    @Test public void testPortalSetHasGetTargetPortal( ) {
        Portal portal = new Portal("p1", "p2", "", new Vect(1,1));
        Board board = new Board("b1");
        board.addPortal(portal);
        assertEquals("Expect portal",true,board.hasPortal(portal.getName()));
    }
    /*
     * covers portal 
      
     * otherPortal is itself
     * hasPortal: true, false
     */
    @Test public void testPortalSetHasGetTargetPortal2( ) {
        Portal portal = new Portal("p1", "p1", "b1", new Vect(1,1));
        Board board = new Board("b1");
        board.addPortal(portal);
        assertEquals("Expect other portal","p1", portal.getOtherPortalName());
        assertEquals("Expect other portal","b1", portal.getOtherBoardName());
        assertEquals("Expect has target portal",true,board.hasPortal(portal.getName()));
    }
    /*
     * covers portal 
     * otherPortal is not itself
     * hasPortal: false, true
     */
    @Test public void testPortalSetHasGetTargetPortal3( ) {
        Portal portal = new Portal("p1", "p2", "b1", new Vect(1,1));
        Board board = new Board("b1");
        board.addPortal(portal);
        assertEquals("Expect other portal","p2", portal.getOtherPortalName());
        assertEquals("Expect other portal","b1", portal.getOtherBoardName());
        assertEquals("Expect has target portal",true,board.hasPortal(portal.getName()));
    }

    /*
     * covers portal 
     * otherPortal is not itself
     * hasPortal: false, true
     */
    @Test public void testPortalGetLocation( ) {
        Portal portal = new Portal("p1", "p2", "b1", new Vect(1,1));
        assertEquals("Expect location",new Vect(1,1),portal.getLocation());
    }
    /*
     * covers portal, equals
     * equals, not equals
     */
    @Test public void testPortalEquals( ) {
        Portal portal = new Portal("p1", "p2", "b1", new Vect(1,1));
        Portal portal2 =new Portal("p1", "p2", "b1", new Vect(1,1));
        Portal portal3 =new Portal("p2", "p2", "b1", new Vect(1,1));
        assertEquals("Expect equals",true,portal.equals(portal2));
        assertEquals("Expect equals",false,portal.equals(portal3));
    }

    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // phase 1 tests
    @Test(expected = AssertionError.class) public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    /*
     * Types of Gadgets
     * Bumpers: Circle, Triangle, Square
     * Ball
     * Wall
     * Absorber
     * 
     * Testing Strategy:
     *
     * getLocation
     * Location Vect X position = 0, 0 < x < 19, 19
     * Location Vect Y position = 0, 0 < y< 19, 19
     * 
     * triggered
     * Ball hitting the gadget (Vect locations are equal)
     * Ball close to hitting gadget (vect locations are very close)
     * Ball not close/not hitting (Vect locations are significantly different)
     * 
     * hasTriggerTarget
     * number of trigger targets = 0, 1, > 1 (expect: false, true, true)
     * 
     * getTriggerTargets
     * number of trigger targets = 0, 1, > 1
     * 
     * getName
     * name empty
     * name not empty
     * 
     * equals
     * transitive
     * symmetric
     * reflexive
     * aliased objects (expect true)
     * objects constructed same way (expect true)
     * different components (name, position, etc) (expect false)
     * 
     * Absorber specific:
     * getSize
     * Width, height = 1, 1 < w,h < 19, 19 // could take up the whole board
     * theoretically. Must have non zero width/height
     * 
     * action
     * # of balls holding: 1, >1
     * expect number to reduce by 1 if >= 1
     * 
     * getNumberOfBalls
     * # of balls holding: 0, 1, >1
     * 
     * draw (drawSquare, drawCircle, drawTriangle, drawAbsorber)
     *  These gadgets will be drawn on a Graphics object that is the result of calling 
     *  .getGraphics() on a BufferedImage. This BufferedImage can then be tested for
     *  appropriate dimensions, and accurate pixel placement using examinePixelsOfImage(),
     *  a method adapted from the example code in PSet3.
     * 
     */


    /* covers: equals
     * transitive, reflexive
     * aliased objects
     */
    @Test public void testEqualsAliasedGadgets( ) {
        Ball ballOne = new Ball("ball", new Vect(10,10), new Vect(5,5));
        Ball ballTwo = ballOne;
        Ball ballThree = ballOne;
        assertTrue("Expect aliases to be equal", ballOne.equals(ballTwo));
        assertTrue("expect aliases to be equal", ballTwo.equals(ballThree));
        assertTrue("expect aliases to be equal", ballThree.equals(ballOne));
        assertTrue("expect object to equal itself", ballOne.equals(ballOne));
    }

    /* covers: equals
     * constructed same way
     * symmetry
     */
    @Test public void testEqualsSameButNotAliases() {
        SquareBumper bumperOne = new SquareBumper("bumper", new Vect(10,10));
        SquareBumper bumperTwo = new SquareBumper("bumper", new Vect(10,10));
        assertTrue("expect to be equal", bumperOne.equals(bumperTwo));
        assertTrue("expect to be equal on symmetry", bumperTwo.equals(bumperOne));
    }

    /* 
     * covers: equals
     * different components (name, position, etc) (expect false)
     */
    @Test public void testEqualsNotSame() {
        Absorber absOne = new Absorber("abs1", new Vect(4,4), new Vect(5,5));
        Absorber absTwo = new Absorber("abs2",new Vect(4,4), new Vect(5,5));
        assertFalse("expect to be not equals because different names", absOne.equals(absTwo));
    }

    /*
     * covers: getLocation
     * X start = 0
     * Y start = 0
     */
    @Test public void testGetLocationTopLeftCorner() {
        SquareBumper bumper = new SquareBumper("bummer", new Vect(0, 0));
        assertEquals("Expect position to be at origin", new Vect(0, 0), bumper.getLocation());
    }

    /*
     * covers: getLocation
     * 0 < X < 19
     * 0 < Y < 19
     */
    @Test public void testGetLocationMiddleBoard() {
        TriangleBumper bumper = new TriangleBumper("bummer", new Vect(5, 10), new Angle(0));
        assertEquals("expected positions to match", new Vect(5, 10), bumper.getLocation());
    }

    /*
     * covers: getLocation
     * X = 19
     * Y = 19
     */
    @Test public void testGetLocationBottomRightCorner() {
        CircleBumper bumper = new CircleBumper("bummer", new Vect(19, 19));
        assertEquals("expected bottom right corner to be valid position", new Vect(19, 19), bumper.getLocation());
    }

    /*
     * covers: triggered
     * Ball hitting the gadget (Vect locations are equal)
     * expect: true
     */
    @Test public void testTriggeredCollisionTrue() {
        Ball ball = new Ball("ball", new Vect(10, 10), new Vect(1, 0));
        CircleBumper bumper = new CircleBumper("bummer", new Vect(10, 10));
        assertTrue("expect ball to have collided with circle bumper", bumper.triggered(ball));
    }

    /*
     * covers: triggered
     * Ball close to hitting gadget (vect locations are very close)
     * expect: false
     */
    @Test public void testTriggeredAlmostCollisionFalse() {
        Ball ball = new Ball("ball", new Vect(10, 10), new Vect(1, 0));
        CircleBumper bumper = new CircleBumper("bummer", new Vect(11, 11));
        assertFalse("expect ball to not have collided with circle bumper", bumper.triggered(ball));
    }

    /*
     * covers: triggered
     * Ball not close/not hitting (Vect locations are significantly different)
     * expect false
     */
    @Test public void testTriggeredNoCollisionFalse() {
        Ball ball = new Ball("ball", new Vect(10, 10), new Vect(1, 0));
        CircleBumper bumper = new CircleBumper("bummer", new Vect(1, 1));
        assertFalse("expect ball to not have collided with circle bumper", bumper.triggered(ball));
    }

    /*
     * covers: getName
     * name not empty (Can't be empty)
     */
    @Test public void testGetName() {
        TriangleBumper bumper = new TriangleBumper("Bump", new Vect(10, 10), new Angle(0));
        assertEquals("expect name to be Bump", "Bump", bumper.getName());
    }

    /*
     * covers: getSize for absorber
     * Width, height = 1
     */
    @Test public void testAbsorberGetSizeSmall() {
        Absorber abs = new Absorber("abs1", new Vect(2, 5), new Vect(1, 1));
        assertEquals("Expected width and height to be 1", new Vect(1, 1), abs.getSize());
    }

    /*
     * covers: getSize
     * 1 < width, height < 19
     */
    @Test public void testAbsorberGetSizeMedium() {
        Absorber abs = new Absorber("abs1", new Vect(2, 5), new Vect(5, 10));
        assertEquals("Expected width and height to be 5, 10", new Vect(5, 10), abs.getSize());
    }

    /*
     * covers: getSize
     * width, height = 19
     */
    @Test public void testAbsorberGetSizeLarge() {
        Absorber abs = new Absorber("abs1", new Vect(0, 0), new Vect(19, 19));
        assertEquals("Expected width and height to be 19", new Vect(19, 19), abs.getSize());
    }

    /*
     * covers: action, getNumberOfBalls
     * # of balls held = 1, 0 (after)
     */
    @Test public void testAbsorberActionOneBall() {
        Absorber abs = new Absorber("abs1", new Vect(2, 5), new Vect(5, 10));
        Ball firedBall = abs.action("ball");
        Ball expectedBall = new Ball("ball", new Vect(6.75,5.0), new Vect(0, -50));
        assertEquals("expected an equal fired ball", expectedBall, firedBall);
    }


    // A unit on the board, L, is equal to 20 pixels
    private static final int L = 20;

    // Test drawSquare
    @Test
    public void testDrawSquare() {
        final int outputImageWidth = 3*L;
        final int outputImageHeight = 3*L;
        final BufferedImage buffImg = 
                new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = buffImg.getGraphics();

        SquareBumper squareBump = new SquareBumper("SimpleSquare", new Vect(1.0, 1.0));
        squareBump.draw(graphics);

        assertEquals("Image width should not have changed.", 3*L, buffImg.getWidth());
        assertEquals("Image height should not have changed.", 3*L, buffImg.getHeight());

        // pixels outside of center square should be transparent
        BufferedImage subImage = buffImg.getSubimage(0, 0, 3*L, L);
        assertEquals("Sub image should be transparent", Transparency.TRANSLUCENT, subImage.getTransparency());
    }

    // Test drawCircle
    @Test
    public void testDrawCircle() {
        final int outputImageWidth = L;
        final int outputImageHeight = L;
        final BufferedImage buffImg = 
                new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = buffImg.getGraphics();

        CircleBumper circleBump = new CircleBumper("Simple Circle", new Vect(0.5, 0.5));
        circleBump.draw(graphics);

        assertEquals("Image width should not have changed.", L, buffImg.getWidth());
        assertEquals("Image height should not have changed.", L, buffImg.getHeight());

        // pixels outside of center square should be transparent
        BufferedImage subImage = buffImg.getSubimage(19, 19, 1, 1);
        assertEquals("Sub image should be transparent", Transparency.TRANSLUCENT, subImage.getTransparency());
    }

    // Test drawTriangle
    @Test
    public void testDrawTriangle() {
        final int outputImageWidth = L;
        final int outputImageHeight = L;
        final BufferedImage buffImg = 
                new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = buffImg.getGraphics();

        TriangleBumper triangle1 = new TriangleBumper("Triangle1", new Vect(0.0, 0.0), Angle.ZERO);
        triangle1.draw(graphics);
        BufferedImage subImage = buffImg.getSubimage(18, 18, 2, 2);
        assertEquals("Sub image should be transparent", Transparency.TRANSLUCENT, subImage.getTransparency());

        // Two triangles together will make a square
        TriangleBumper triangle2 = new TriangleBumper("Triangle2", new Vect(0.0, 0.0), Angle.DEG_180);
        triangle2.draw(graphics);

        assertEquals("Image width should not have changed", L, buffImg.getWidth());
        assertEquals("Image height should not have changed", L, buffImg.getHeight());
    }

    // Test drawAbsorber
    @Test
    public void testDrawAbsorber() {
        final int outputImageWidth = 20*L;
        final int outputImageHeight = 20*L;
        final BufferedImage buffImg = 
                new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = buffImg.getGraphics();

        Absorber absorber = new Absorber("Simple Absorber", new Vect(0.0, 1.0), new Vect(20, 19));
        absorber.draw(graphics);

        assertEquals("Image width should not have changed", 20*L, buffImg.getWidth());
        assertEquals("Image height should not have changed", 20*L, buffImg.getHeight());

        assertEquals("Absorber color should be pink", Color.PINK, graphics.getColor());

        // pixels outside of center square should be transparent
        BufferedImage subImage = buffImg.getSubimage(0, 0, L, 20*L);
        assertEquals("Sub image should be transparent", Transparency.TRANSLUCENT, subImage.getTransparency());
    }

}
