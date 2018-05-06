package flingball;

import java.awt.Color;
import java.awt.Graphics;

import physics.Circle;
import physics.Physics;
import physics.Vect;

/**
 * An immutable ball in the flingball game.
 */
public class Ball {
    private final String name;
    private final Circle ball;
    private final Vect velocity;
    private final Vect location;
    
    private static final Color COLOR =new Color(246, 126, 125);
    private static final double DIAMETER = 0.5;
    public static final double RADIUS = 0.25;
    private static final int PIXELS_PER_L = 20;

    // Abstraction Function
    //  AF(name, location, velocity) = A 2D image of a circular ball whose name is name. This
    //                                 ball's center is located at coordinates (x, y), where (x, y)
    //                                 are integers between 0 and 19 inclusive that determine where
    //                                 the ball is on a rendered image. The ball travels around a pinball
    //                                 board at a velocity given by velocity's (x, y) components which 
    //                                 specify the velocity in those directions as a unit vector. 
    // Representation Invariant 
    //  --| location must be, [0, 19.5]
    //  --| velocity must be: -300 <= velocity <= 300
    //  --| other things that would break representation exposure (incorrect names) fail faster in
    //      other classes such as BoardParser
    
    /*
     *  Checks the Representation Invariant to ensure that no representation exposure occurs
     */
    private void checkRep() {
        // sanity checks :)
        assert name != null;
        assert location != null;
        assert velocity != null;
        assert ball != null;

        final double lowerBallLocationBound = -21; // values account for floating point imprecision
        final double upperBallLocationBound = 41;
        if(!(lowerBallLocationBound < location.x() && location.x() < upperBallLocationBound)
                ||!(lowerBallLocationBound < location.y() && location.y() < upperBallLocationBound)) {
            System.err.println("checkrep fail! "+this);
        }
        assert lowerBallLocationBound < location.x() && location.x() < upperBallLocationBound;
        assert lowerBallLocationBound < location.y() && location.y() < upperBallLocationBound;
       
        final int maxVelocity = 400;
        final int minVelocity = -400;
        assert minVelocity <= velocity.x() && velocity.x() <= maxVelocity;
        assert minVelocity <= velocity.y() && velocity.y() <= maxVelocity;
    }

    
    /**
     * Construct a ball with given position, radius and velocity.
     * 
     * @param location is the center of the ball on the Board in L.
     * @param radius the radius of the Ball in L 
     * @param velocity the velocity of the Ball in L/sec
     */
    Ball(String name, Vect location, Vect velocity){
        this.name = name;
        this.ball = new Circle(location, RADIUS);
        this.velocity = velocity;
        this.location = location;
        checkRep();
    }
    
    /**
     * @return the Circle object of the Ball
     */
    Circle getCircle() {
        return ball;
    }
    
    double getRadius(){
        return RADIUS;
    }
    /**
     * @return the velocity of the Ball
     */
    Vect getVelocity(){
        return velocity;
    }

    /**
     * @return location of of the Ball's center
     */
    public Vect getLocation() {
        return location;
    }
    
    /**
     * checks if two balls will be collided
     * @param ball
     * @return whether the balls are triggered
     */
    public boolean triggered(Ball ball) {
        return (Physics.timeUntilCircleCollision(this.ball, ball.getCircle(), ball.getVelocity()) < Board.TIME);
    }
    
    /**
     * get the new direction of a collision of balls
     * @param ball
     * @return a new ball with the new direction
     */
    public Ball getCollisionRedirection(Ball ball) {
        if (Physics.timeUntilCircleCollision(this.ball, ball.getCircle(), ball.getVelocity()) < Board.TIME) {
            Vect newVelocity = Physics.reflectCircle(this.ball.getCenter(), ball.getLocation(), ball.getVelocity());
            return new Ball(ball.getName(), ball.getLocation(), newVelocity);
        }
        return new Ball(ball.getName(), ball.getLocation(), ball.getVelocity());
    }
    
    /**
     * Draws a Gadget.
     * 
     * @param g Graphics the 2D render on which to draw.
     */
    public void draw(Graphics g) {
        // xCoord & yCoord specify the circle's top left corner as per the fillArc spec
        final int xCoord = (int) ((location.x() - RADIUS) * PIXELS_PER_L);
        final int yCoord = (int) ((location.y() - RADIUS) * PIXELS_PER_L);
        final int pixelDiameter = (int) (PIXELS_PER_L * DIAMETER);
        final int startAngleDegrees = 0;
        final int arcAngleDegrees = 360;
        g.setColor(COLOR);
        g.fillArc(xCoord, yCoord, pixelDiameter, pixelDiameter, startAngleDegrees, arcAngleDegrees);
        checkRep(); // just to make sure nothing mutated in-class :)
    }

    /**
     * @return the given name of the Ball
     */
    public String getName() {
        return name;
    }
    
    @Override 
    public String toString() {
        return "Ball: " + name + " @ " + location.toString() + 
                ", Velocity: " + velocity.toString() + "\n";
    }

    @Override 
    public int hashCode() {
        return name.hashCode() + location.hashCode() + velocity.hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if ( ! (obj instanceof Ball)) { return false; }
        final Ball that = (Ball) obj;
        return this.name       .equals(that.name) &&
               this.location   .equals(that.location) &&
               this.velocity   .equals(that.velocity);
    }
    
}
