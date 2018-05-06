package flingball;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import physics.*;

/**
 * An immutable triangle bumper in the flingball game, an implementation of
 * Gadget and Bumper
 */
public class TriangleBumper implements Bumper {

    private final String name;
    private final Vect location;
    private final Angle orientation;
    private final List<LineSegment> lineSegments;
    private final List<Circle> circles;

    private static final double EDGE_LENGTH = 1;
    private static final double HALF_EDGE_LENGTH = 0.5;
    private static final Color COLOR = Bumper.BUMPER_COLOR;

    // Abstraction Function
    //  AF(name, location, orientation, lineSegments, circles) = A 2D image of a static triangle
    //                      object that has a label of name and a rotation given by orientation.
    //                      In a pinball game, this represents a bumper that balls can bounce off.
    //                      The object's top left corner is located at coordinates (x, y), where
    //                      (x, y) are integers between 0 and 19 inclusive that determine the
    //                      triangle's position on a pinball board. lineSegments represents the individual
    //                      line segment components that make up the borders of the triangle image.
    //                      circles represents the circles that lie at all of the corners of the
    //                      triangle image to make smooth corners for ball to bounce off.
    // Representation Invariant
    // --| location must be integers, [0, 19]
    // --| orientation must be either 0, 90, 180, 270 or the radial equivalent
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
        assert orientation != null;
        assert lineSegments != null;
        assert circles != null;

        final int numberOfEdges = 3;
        assert lineSegments.size() == numberOfEdges;
        assert circles.size() == numberOfEdges;

        assert (orientation.compareTo(Angle.ZERO) == 0) || (orientation.compareTo(Angle.DEG_90) == 0)
                || (orientation.compareTo(Angle.DEG_180) == 0) || (orientation.compareTo(Angle.DEG_270) == 0);

        assert (int) 0 <= location.x() && location.x() <= Board.L;
        assert (int) 0 <= location.y() && location.y() <= Board.L;
    }

    /**
     * Construct a triangle bumper with given location
     */
    TriangleBumper(String name, Vect location, Angle orientation) {
        this.name = name;
        this.location = location; //location of top left corner
        this.orientation = orientation;
        Vect locationTopRight = location.plus(new Vect(EDGE_LENGTH, 0));
        Vect locationBottomLeft = location.plus(new Vect(0, EDGE_LENGTH));
        Vect centerLocation = location.plus(new Vect(HALF_EDGE_LENGTH, HALF_EDGE_LENGTH));
        lineSegments = Collections.synchronizedList(new ArrayList<>());
        circles = Collections.synchronizedList(new ArrayList<>());
        lineSegments.add(new LineSegment(locationBottomLeft, locationTopRight));
        lineSegments.add(new LineSegment(location, locationTopRight));
        lineSegments.add(new LineSegment(location, locationBottomLeft));
        circles.add(new Circle(location, 0));
        circles.add(new Circle(locationTopRight, 0));
        circles.add(new Circle(locationBottomLeft, 0));

        List<Circle> newCircles = new ArrayList<>();
        for(Circle circle: circles)
            newCircles.add(Physics.rotateAround(circle, centerLocation, orientation));
        circles.clear();
        circles.addAll(newCircles);
        List<LineSegment> newLineSegments = new ArrayList<>();
        for(LineSegment lineSegment:lineSegments)
            newLineSegments.add(Physics.rotateAround(lineSegment, centerLocation, orientation));
        lineSegments.clear();
        lineSegments.addAll(newLineSegments);

        checkRep();
    }

    /**
     * @return the rotation of this TriangleBumper
     */
    public Angle getRotation() {
        return orientation;
    }

    @Override 
    public Vect getLocation() {
        return location;
    }

    @Override 
    public Color getColor() {
        return COLOR;
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
    public boolean triggered(Ball ball) {
        for (int i = 0; i < lineSegments.size(); i++) {
            if (Physics.timeUntilWallCollision(lineSegments.get(i), ball.getCircle(),
                    ball.getVelocity()) < Board.TIME) {
                return true;
            }
        }
        for (int i = 0; i < circles.size(); i++) {
            if (Physics.timeUntilCircleCollision(circles.get(i), ball.getCircle(), 
                    ball.getVelocity()) < Board.TIME) {
                return true;
            }
        }
        return false;
    }

    @Override 
    public String getName() {
        return name;
    }

    @Override 
    public void draw(Graphics g) {
        g.setColor(COLOR);
        final int pointsInTriangle = 3;
        final int[] xPoints = new int[pointsInTriangle];
        final int[] yPoints = new int[pointsInTriangle];
        final int xCoord = (int) (location.x() * Board.PIXEL_PER_L);
        final int yCoord = (int) (location.y() * Board.PIXEL_PER_L);

        final int p1Index = 0; // for indexing in the array! :)
        final int p2Index = 1;
        final int p3Index = 2;

        if (orientation.compareTo(Angle.ZERO) == 0) {
            // East -> West -> West |/
            xPoints[p1Index] = xCoord + Board.L;
            xPoints[p2Index] = xCoord;
            xPoints[p3Index] = xCoord;
            // North -> North -> South
            yPoints[p1Index] = yCoord;
            yPoints[p2Index] = yCoord;
            yPoints[p3Index] = yCoord + Board.L;
        } else if (orientation.compareTo(Angle.RAD_PI_OVER_TWO) == 0) {
            // West -> East -> East \|
            xPoints[p1Index] = xCoord;
            xPoints[p2Index] = xCoord + Board.L;
            xPoints[p3Index] = xCoord + Board.L;
            // North -> North -> South
            yPoints[p1Index] = yCoord;
            yPoints[p2Index] = yCoord;
            yPoints[p3Index] = yCoord + Board.L;
        } else if (orientation.compareTo(Angle.RAD_PI) == 0) {
            // East -> East -> West /|
            xPoints[p1Index] = xCoord + Board.L;
            xPoints[p2Index] = xCoord + Board.L;
            xPoints[p3Index] = xCoord;
            // North -> South -> South
            yPoints[p1Index] = yCoord;
            yPoints[p2Index] = yCoord + Board.L;
            yPoints[p3Index] = yCoord + Board.L;
        } else { // 3Pi/2 or 270 degrees
            // West -> West -> East |\
            xPoints[p1Index] = xCoord;
            xPoints[p2Index] = xCoord;
            xPoints[p3Index] = xCoord + Board.L;
            // North -> South -> South
            yPoints[p1Index] = yCoord;
            yPoints[p2Index] = yCoord + Board.L;
            yPoints[p3Index] = yCoord + Board.L;
        }

        // scale the triangle by the side length just in case specs for edge length
        // change
        for (int i = 0; i < pointsInTriangle; i++) {
            xPoints[i] *= EDGE_LENGTH;
            yPoints[i] *= EDGE_LENGTH;
        }

        // Round those edges :)
        final int startAngleDegrees = 0;
        final int arcAngleDegrees = 360;
        g.fillArc(xPoints[p1Index], yPoints[p1Index], 0, 0, startAngleDegrees, arcAngleDegrees);
        g.fillArc(xPoints[p2Index], yPoints[p2Index], 0, 0, startAngleDegrees, arcAngleDegrees);
        g.fillArc(xPoints[p3Index], yPoints[p3Index], 0, 0, startAngleDegrees, arcAngleDegrees);

        g.fillPolygon(xPoints, yPoints, pointsInTriangle);
    }

    @Override 
    public String toString() {
        return "Triangle Bumper: " + name + " @ " + location.toString() + ", Rotated: " + orientation.toString() + "\n";
    }

    @Override 
    public int hashCode() {
        return name.hashCode() + location.hashCode() + orientation.hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof TriangleBumper)) { return false; }
        final TriangleBumper that = (TriangleBumper) obj;
        return this.name       .equals(that.name) && 
               this.location   .equals(that.location) && 
               this.orientation.equals(that.orientation);
    }
}
