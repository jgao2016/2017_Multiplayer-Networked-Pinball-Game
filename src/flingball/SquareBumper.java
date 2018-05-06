package flingball;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import physics.*;

/**
 * An immutable square bumper in the flingball game, an implementation of Gadget
 * and Bumper
 */
public class SquareBumper implements Bumper {

    private final String name;
    private final Vect location;
    private final List<LineSegment> lineSegments;
    private final List<Circle> circles;

    private static final Color COLOR = Bumper.BUMPER_COLOR;
    private static final int EDGE_LENGTH = 1;

    // Abstraction Function
    // AF(name, location, lineSegments, circles) = A 2D image of a static square object that has
    //               a label of name. In a pinball game, this represents a bumper that
    //               balls can bounce off. The object's top left corner is located at coordinates
    //               (x, y), where (x, y) are integers between 0 and 19 inclusive
    //               that that determine the square's position on a pinball board.
    //               lineSegments represents the individual line segment components that make
    //               up the borders of the square image. circles represents the circles
    //               that lie at all of the corners of the square image to make smooth corners.
    // Representation Invariant
    // --| location must be integers, [0, 19]
    // --| lineSegments size must be equal to the number of sides of a rectangle (4)
    // --| circles size must be equal to the number of corners of a rectangle (4)
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
        assert lineSegments != null;
        assert circles != null;
        
        final int numberOfEdges = 4;
        assert lineSegments.size() == numberOfEdges;
        assert circles.size() == numberOfEdges;

        assert (int) 0 <= location.x() && location.x() <= Board.L-1;
        assert (int) 0 <= location.y() && location.y() <= Board.L-1;
    }

    /**
     * Construct a square bumper with given location
     */
    SquareBumper(String name, Vect location) {
        this.name = name;
        this.location = location;
        Vect locationTopRight = location.plus(new Vect(EDGE_LENGTH, 0));
        Vect locationBottomLeft = location.plus(new Vect(0, EDGE_LENGTH));
        Vect locationBottomRight = location.plus(new Vect(EDGE_LENGTH, EDGE_LENGTH));
        lineSegments = Collections.synchronizedList(new ArrayList<>());
        circles = Collections.synchronizedList(new ArrayList<>());
        lineSegments.add(new LineSegment(locationBottomLeft, locationBottomRight));
        lineSegments.add(new LineSegment(location, locationTopRight));
        lineSegments.add(new LineSegment(location, locationBottomLeft));
        lineSegments.add(new LineSegment(locationTopRight, locationBottomRight));
        circles.add(new Circle(location, 0));
        circles.add(new Circle(locationTopRight, 0));
        circles.add(new Circle(locationBottomLeft, 0));
        circles.add(new Circle(locationBottomRight, 0));

        checkRep();
    }

    @Override 
    public Vect getLocation() {
        return location;
    }

    @Override 
    public boolean triggered(Ball ball) {
        for (int i = 0; i < lineSegments.size(); i++) {
            if (Physics.timeUntilWallCollision(lineSegments.get(i), ball.getCircle(), ball.getVelocity()) < Board.TIME)
                return true;
        }
        for (int i = 0; i < circles.size(); i++) {
            if (Physics.timeUntilCircleCollision(circles.get(i), ball.getCircle(), ball.getVelocity()) < Board.TIME)
                return true;
        }
        return false;
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
    public void draw(Graphics g) {
        final int xCoord = (int) location.x() * Board.PIXEL_PER_L;
        final int yCoord = (int) location.y() * Board.PIXEL_PER_L;
        g.setColor(COLOR);
        g.fillRect(xCoord, yCoord, Board.L * EDGE_LENGTH, Board.L * EDGE_LENGTH);

        // no sharp edges! adding zero-radius circles to the corners of SquareBumper
        final int startAngleDegrees = 0;
        final int arcAngleDegrees = 360;
        g.fillArc(xCoord, yCoord, 0, 0, startAngleDegrees, arcAngleDegrees);
        g.fillArc(xCoord + Board.L, yCoord, 0, 0, startAngleDegrees, arcAngleDegrees);
        g.fillArc(xCoord, yCoord + Board.L, 0, 0, startAngleDegrees, arcAngleDegrees);
        g.fillArc(xCoord + Board.L, yCoord + Board.L, 0, 0, startAngleDegrees, arcAngleDegrees);
    }

    @Override 
    public Ball getCollisionRedirection(Ball ball) {
        for (int i = 0; i < lineSegments.size(); i++) {
            if (Physics.timeUntilWallCollision(lineSegments.get(i), ball.getCircle(),
                    ball.getVelocity()) < Board.TIME) {
                Vect newVelocity = Physics.reflectWall(lineSegments.get(i), ball.getVelocity());
                return new Ball(ball.getName(), ball.getLocation(), newVelocity);
            }
        }
        for (int i = 0; i < circles.size(); i++) {
            if (Physics.timeUntilCircleCollision(circles.get(i), ball.getCircle(), ball.getVelocity()) < Board.TIME) {
                Vect newVelocity = Physics.reflectCircle(circles.get(i).getCenter(), ball.getLocation(),
                        ball.getVelocity());
                return new Ball(ball.getName(), ball.getLocation(), newVelocity);
            }
        }
        return new Ball(ball.getName(), ball.getLocation(), ball.getVelocity());
    }

    @Override 
    public String toString() {
        return "Square Bumper: " + name + " @ " + location.toString() + "\n";
    }

    @Override 
    public int hashCode() {
        return name.hashCode() + location.hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof SquareBumper)) { return false; }
        final SquareBumper that = (SquareBumper) obj;
        return this.name    .equals(that.name) && 
               this.location.equals(that.location);
    }
}
