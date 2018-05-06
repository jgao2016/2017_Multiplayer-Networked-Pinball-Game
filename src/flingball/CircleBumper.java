package flingball;

import java.awt.Color;
import java.awt.Graphics;

import physics.Circle;
import physics.Physics;
import physics.Vect;

/**
 * An immutable circle bumper in the Flingball game, an implementation of the Bumper interface
 */
public class CircleBumper implements Bumper {

    private final String name;
    private final Vect location;
    private final Circle circle;

    private static final Color COLOR = Bumper.BUMPER_COLOR;
    private static final int DIAMETER = 1;
    private static final double RADIUS = 0.5;

    // Abstraction Function
    //  AF(name, location, circle) = A 2D image of a static circular object that has a label of name.
    //             In a pinball game, this represents a bumper that balls can bounce off. The object's
    //             center is located at coordinates (x, y), where (x, y) are integers between 0 and 19
    //             inclusive that determine the triangle's position on a pinball board. circle contains
    //             the borders of the circular object to check if any balls have hit it.
    // Representation Invariant
    // --| location must be integers, [0, 19]
    // --| other things that would break representation exposure (incorrect names) fail faster in
    //          other classes such as BoardParser
    // Safety from Representation Exposure
    // --| all fields are private and final
    // --| all methods return immutable types

    /*
     * Checks the Representation Invariant to ensure that no representation exposure occurs
     */
    private void checkRep() {
        // sanity checks :)
        assert name != null;
        assert location != null;
        assert circle != null;

        assert (int) 0 <= location.x() && location.x() <= Board.L - 1;
        assert (int) 0 <= location.y() && location.y() <= Board.L - 1;
    }

    /**
     * Construct a circle bumper with given location
     */
    CircleBumper(String name, Vect location) {
        this.name = name;
        this.location = location;
        this.circle = new Circle(location.plus(new Vect(RADIUS, RADIUS)), RADIUS);
        checkRep();
    }

    @Override 
    public Vect getLocation() {
        return location;
    }

    @Override 
    public String getName() {
        return name;
    }

    @Override 
    public Color getColor() {
        return COLOR;
    }

    @Override 
    public boolean triggered(Ball ball) {
        return (Physics.timeUntilCircleCollision(circle, ball.getCircle(), ball.getVelocity()) < Board.TIME);
    }

    @Override 
    public Ball getCollisionRedirection(Ball ball) {
        if (Physics.timeUntilCircleCollision(circle, ball.getCircle(), ball.getVelocity()) < Board.TIME) {
            Vect newVelocity = Physics.reflectCircle(circle.getCenter(), ball.getLocation(), ball.getVelocity());
            return new Ball(ball.getName(), ball.getLocation(), newVelocity);
        }
        return new Ball(ball.getName(), ball.getLocation(), ball.getVelocity());
    }

    @Override 
    public void draw(Graphics g) {
        // xCoord & yCoord specify the circle's center
        final int xCoord = (int) (location.x() * Board.PIXEL_PER_L);
        final int yCoord = (int) (location.y() * Board.PIXEL_PER_L);
        final int startAngleDegrees = 0;
        final int arcAngleDegrees = 360;
        g.setColor(COLOR);
        g.fillArc(xCoord, yCoord, Board.L * DIAMETER, Board.L * DIAMETER, startAngleDegrees, arcAngleDegrees);

        checkRep();
    }

    @Override 
    public String toString() {
        return "Circle Bumper: " + name + " @ " + location + "\n";
    }

    @Override 
    public int hashCode() {
        return name.hashCode() + location.hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof CircleBumper)) { return false; }
        final CircleBumper that = (CircleBumper) obj;
        return this.name    .equals(that.name) && 
               this.location.equals(that.location);
    }
}
