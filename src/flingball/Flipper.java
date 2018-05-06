package flingball;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import physics.*;

/**
 * flipper type enum
 */
enum FlipperLeftOrRight {
    LEFT_FLIPPER,
    RIGHT_FLIPPER
}
/**
 * An immutable class representing a flipper in flingball game, implements Bumper
 */
public class Flipper implements Bumper {
    
    private final String name;
    private final Vect location;
    private final Angle orientation;
    private final FlipperLeftOrRight variation;
    private final Angle angleOfRotation;
    private final Angle startingAngle;
    
    private final List<LineSegment> lineSegments;
    private final List<Circle> circles;
    private final Vect centerOfRotation;
    private final Color COLOR = Color.BLACK;
    private final boolean initialPosition;
    private final boolean stillNeedToMove;
    private static final int BOUNDING_L = 2;
    private static final double LENGTH_OF_SHORTER_SIDE = 0.1;
    private static final double ANGULAR_VELOCITY = 1080.0;
    private final double ANGULAR_VELOCITY_RADIAN;
    private static final double COEFFICIENT = 0.95;
    
    // Abstraction Function
    //  AF(name, location, orientation, variation) = A flipper object that has a label of name.
    //             represents a bumper that is movable and that balls can bounce off and trigger the flipper 
    //             to rotate 90 degrees. 
    //             The object's center is located at coordinates (x, y), where (x, y) are integers between 0 and 19
    //             inclusive. For a variation of left,  the default orientation (0 degrees) places the 
    //             flipper’s pivot point in the northwest corner. For a variation of right, the default orientation 
    //             puts the pivot point in the northeast corner.

    // Representation Invariant
    // --| location must be integers, [0, 19]
    // --| other things that would break representation exposure (incorrect names) fail faster in
    //          other classes such as BoardParser
    // Safety from Representation Exposure
    // --| all fields are private and final
    // --| all methods return immutable types

    /**
     * Construct a Flipper with given location, orientation, and variation (left or right) and angleOfRotation
     * 
     */
    public Flipper(String name, Vect location, Angle orientation, FlipperLeftOrRight variation, Angle angleOfRotation, boolean initialPosition, boolean stillNeedToMove) {
        this.name = name;
        this.location = location;
        this.orientation = orientation;
        this.variation = variation;
        this.angleOfRotation = angleOfRotation;
        this.initialPosition = initialPosition;
        this.stillNeedToMove = stillNeedToMove;
        this.centerOfRotation = this.getCenterOfRotation();
        lineSegments = Collections.synchronizedList(new ArrayList<>());
        circles = Collections.synchronizedList(new ArrayList<>());
        this.startingAngle = privateGetStartingAngle();
        lineSegments.addAll(this.getAllLineSegments());
        circles.addAll(this.getAllCircles());
        if (this.getFlipperVariation().equals(FlipperLeftOrRight.LEFT_FLIPPER)) {
            if (this.initialPosition) {
                this.ANGULAR_VELOCITY_RADIAN = -Math.toRadians(ANGULAR_VELOCITY);
            } else {
                this.ANGULAR_VELOCITY_RADIAN = Math.toRadians(ANGULAR_VELOCITY);
            }
        } else {
            if (this.initialPosition) {
                this.ANGULAR_VELOCITY_RADIAN = Math.toRadians(ANGULAR_VELOCITY);
            } else {
                this.ANGULAR_VELOCITY_RADIAN = -Math.toRadians(ANGULAR_VELOCITY);
            }
        }
        checkRep();
    }
    
    /**
     * checks the rep of one Flipper instance
     */
    private void checkRep() {
        assert name!=null;
        assert location!=null;
        assert orientation!=null;
        assert variation!=null;
        assert angleOfRotation!=null;
        assert startingAngle!=null;
        assert lineSegments!=null;
        assert circles!=null;
        assert centerOfRotation!=null;
    }
    
    /**
     * get the starting angle of a flipper instance
     * @return the starting angle
     */
    public Angle getStartingAngle() {
        return this.startingAngle;
    }
    
    /**
     * get the private starting angle
     * @return the private starting angle
     */
    private Angle privateGetStartingAngle() {
        if (variation.equals(FlipperLeftOrRight.LEFT_FLIPPER)) {
            if (this.orientation.equals(Angle.ZERO)) {
                return Angle.DEG_270;
            } else if (orientation.equals(Angle.DEG_90)) {
                return Angle.DEG_180;
            } else if (orientation.equals(Angle.DEG_180)) {
                return Angle.DEG_90;
            } else if (orientation.equals(Angle.DEG_270)) {
                return Angle.ZERO;
            }
        } else {
            if (this.orientation.equals(Angle.ZERO)) {
                return Angle.DEG_270;
            } else if (orientation.equals(Angle.DEG_90)) {
                return Angle.DEG_180;
            } else if (orientation.equals(Angle.DEG_180)) {
                return Angle.DEG_90;
            } else if (orientation.equals(Angle.DEG_270)) {
                return Angle.ZERO;
            }
        }
        return Angle.ZERO;
    }
    
    /**
     * get all the circles belonging to an instance
     * @return the list of all circles
     */
    private List<Circle> getAllCircles() {
        List<Circle> localCircles = new ArrayList<Circle>();
        Vect topLeftCorner = this.location;
        Vect bottomLeftCorner = this.location.plus(new Vect(0, BOUNDING_L));
        Vect topRightCorner = this.location;
        Vect bottomRightCorner = this.location;
        if (variation.equals(FlipperLeftOrRight.LEFT_FLIPPER)) {
            if (this.orientation.equals(Angle.ZERO)) {
                topLeftCorner = this.location;
                topRightCorner = this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE, 0));
                bottomRightCorner = this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(0, BOUNDING_L));
            } else if (orientation.equals(Angle.DEG_90)) {
                topLeftCorner = this.location;
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, 0));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, LENGTH_OF_SHORTER_SIDE));
                bottomLeftCorner = this.location.plus(new Vect(0, LENGTH_OF_SHORTER_SIDE));
            } else if (orientation.equals(Angle.DEG_180)) {
                topLeftCorner = this.location.plus(new Vect(BOUNDING_L-LENGTH_OF_SHORTER_SIDE, 0));
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, 0));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(BOUNDING_L-LENGTH_OF_SHORTER_SIDE, BOUNDING_L));
            } else if (orientation.equals(Angle.DEG_270)) {
                topLeftCorner = this.location.plus(new Vect(0, BOUNDING_L-LENGTH_OF_SHORTER_SIDE));
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L-LENGTH_OF_SHORTER_SIDE));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(BOUNDING_L, 0));
            }
        } else {
            if (this.orientation.equals(Angle.ZERO)) {
                topLeftCorner = this.location.plus(new Vect(BOUNDING_L, 0)).minus(new Vect(LENGTH_OF_SHORTER_SIDE, 0));
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, 0));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L)).minus(new Vect(LENGTH_OF_SHORTER_SIDE, 0));
            } else if (orientation.equals(Angle.DEG_90)) {
                topLeftCorner = this.location.plus(new Vect(0, BOUNDING_L-LENGTH_OF_SHORTER_SIDE));
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L-LENGTH_OF_SHORTER_SIDE));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(BOUNDING_L, 0));
            } else if (orientation.equals(Angle.DEG_180)) {
                topLeftCorner = this.location;
                topRightCorner = this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE, 0));
                bottomRightCorner = this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(0, BOUNDING_L));
            } else if (orientation.equals(Angle.DEG_270)) {
                topLeftCorner = this.location;
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, 0));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, LENGTH_OF_SHORTER_SIDE));
                bottomLeftCorner = this.location.plus(new Vect(0, LENGTH_OF_SHORTER_SIDE));
            }
        }
        Circle topLeftCornerCircle =  new Circle(topLeftCorner.x(), topLeftCorner.y(), 0);
        Circle topRightCornerCircle = new Circle(topRightCorner.x(), topRightCorner.y(), 0);
        Circle bottomRightCornerCircle = new Circle(bottomRightCorner.x(), bottomRightCorner.y(), 0);
        Circle bottomLeftCornerCircle = new Circle(bottomLeftCorner.x(), bottomLeftCorner.y(), 0.0);
        localCircles.add(topLeftCornerCircle);
        localCircles.add(topRightCornerCircle);
        localCircles.add(bottomRightCornerCircle);
        localCircles.add(bottomLeftCornerCircle);
        return localCircles;  
    }

    /** 
     * Get all line segment in the order of left, top, right, bottom
     * @return the list of line segments in the order of left, top, right, bottom
     */
    private List<LineSegment> getAllLineSegments() {
        List<LineSegment> lines = new ArrayList<LineSegment>();
        Vect topLeftCorner = this.location;
        Vect bottomLeftCorner = this.location.plus(new Vect(0, BOUNDING_L));
        Vect topRightCorner = this.location;
        Vect bottomRightCorner = this.location;
        if (variation.equals(FlipperLeftOrRight.LEFT_FLIPPER)) {
            if (this.orientation.equals(Angle.ZERO)) {
                topLeftCorner = this.location;
                topRightCorner = this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE, 0));
                bottomRightCorner = this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(0, BOUNDING_L));
            } else if (orientation.equals(Angle.DEG_90)) {
                topLeftCorner = this.location;
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, 0));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, LENGTH_OF_SHORTER_SIDE));
                bottomLeftCorner = this.location.plus(new Vect(0, LENGTH_OF_SHORTER_SIDE));
            } else if (orientation.equals(Angle.DEG_180)) {
                topLeftCorner = this.location.plus(new Vect(BOUNDING_L-LENGTH_OF_SHORTER_SIDE, 0));
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, 0));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(BOUNDING_L-LENGTH_OF_SHORTER_SIDE, BOUNDING_L));
            } else if (orientation.equals(Angle.DEG_270)) {
                topLeftCorner = this.location.plus(new Vect(0, BOUNDING_L-LENGTH_OF_SHORTER_SIDE));
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L-LENGTH_OF_SHORTER_SIDE));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(0, BOUNDING_L));
            }
        } else {
            if (this.orientation.equals(Angle.ZERO)) {
                topLeftCorner = this.location.plus(new Vect(BOUNDING_L, 0)).minus(new Vect(LENGTH_OF_SHORTER_SIDE, 0));
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, 0));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L)).minus(new Vect(LENGTH_OF_SHORTER_SIDE, 0));
            } else if (orientation.equals(Angle.DEG_90)) {
                topLeftCorner = this.location.plus(new Vect(0, BOUNDING_L-LENGTH_OF_SHORTER_SIDE));
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L-LENGTH_OF_SHORTER_SIDE));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(0, BOUNDING_L));
            } else if (orientation.equals(Angle.DEG_180)) {
                topLeftCorner = this.location;
                topRightCorner = this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE, 0));
                bottomRightCorner = this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE, BOUNDING_L));
                bottomLeftCorner = this.location.plus(new Vect(0, BOUNDING_L));
            } else if (orientation.equals(Angle.DEG_270)) {
                topLeftCorner = this.location;
                topRightCorner = this.location.plus(new Vect(BOUNDING_L, 0));
                bottomRightCorner = this.location.plus(new Vect(BOUNDING_L, LENGTH_OF_SHORTER_SIDE));
                bottomLeftCorner = this.location.plus(new Vect(0, LENGTH_OF_SHORTER_SIDE));
            }
        }
        LineSegment leftLine = new LineSegment(topLeftCorner, bottomLeftCorner);
        leftLine = Physics.rotateAround(leftLine, this.centerOfRotation, this.angleOfRotation);
        LineSegment topLine = new LineSegment(topLeftCorner, topRightCorner);
        topLine = Physics.rotateAround(topLine, this.centerOfRotation, this.angleOfRotation);
        LineSegment rightLine = new LineSegment(topRightCorner, bottomRightCorner);
        rightLine = Physics.rotateAround(rightLine, this.centerOfRotation, this.angleOfRotation);
        LineSegment bottomLine = new LineSegment(bottomLeftCorner, bottomRightCorner);
        bottomLine = Physics.rotateAround(bottomLine, this.centerOfRotation, this.angleOfRotation);
        lines.add(leftLine);
        lines.add(topLine);
        lines.add(rightLine);
        lines.add(bottomLine);
        return lines;  
    }
    
    /**
     * Construct a Flipper with given location, orientation, and variation (left or right)
     */
    public Flipper(String name, Vect location, Angle orientation, FlipperLeftOrRight variation) {
        this(name, location, orientation, variation, Angle.ZERO, true, false);
        checkRep();
    }
    
    /**
     * see if the flipper still needs to rotate
     * @return the stillNeedToMove instance field
     */
    public boolean stillNeedToMove() {
        return this.stillNeedToMove;
    }
    
    /**
     * see if the flipper is in its initial position
     * @return the initialPosition instance field
     */
    public boolean isInitialPostion() {
        return this.initialPosition;
    }
    
    /**
     * get the angular velocity 
     * @return the angular velocity
     */
    public double getAngularVelocityPerSecond() {
        return ANGULAR_VELOCITY;
    }
    
    /**
     * this flipper is triggered by key, and it starts to rotate
     */
    public void action() {
        
    }
    
    /**
     * @return the rotation of this Flipper, which is the Angle it has rotated 
     * from its original orientation
     */
    public Angle getRotation() {
        return this.orientation;
    }
    
    /**
     * @return current angle of this flipper
     * which is the angle this flipper points to in cartesian coordinate system
     * 
     * i.e. A left flipper with default orientation (0 degrees) has angle 270 degrees.
     * When it rotates 45 degrees(counterclockwise), the current orientation is Angle.DEG_315,
     * 
     * A right flipper with default orientation (0 degrees) has angle 270 degrees.
     * When it rotates 45 degrees, the current orientation is Angle.DEG_225.
     */
    public Angle getAngle() {
        return this.angleOfRotation;
    }
    
    /**
     * @return the flipper variation, whether it is on the left or right
     */
    public FlipperLeftOrRight getFlipperVariation() {
        return this.variation;
    }

    @Override 
    public Vect getLocation() {
        return this.location;
    }

    @Override 
    public Color getColor() {
        return this.COLOR;
    }

    @Override 
    public Ball getCollisionRedirection(Ball ball) {
        for (int i = 0; i < lineSegments.size(); i++) {
            if (this.stillNeedToMove) {
                if (Physics.timeUntilRotatingWallCollision(lineSegments.get(i), centerOfRotation, ANGULAR_VELOCITY_RADIAN, ball.getCircle(), ball.getVelocity()) < Board.TIME) { 
                    Vect newVelocity = Physics.reflectRotatingWall(lineSegments.get(i), centerOfRotation, ANGULAR_VELOCITY_RADIAN, ball.getCircle(), ball.getVelocity(), COEFFICIENT);
                    return new Ball(ball.getName(), ball.getLocation(), newVelocity);
                }
            } else {
                if (Physics.timeUntilWallCollision(lineSegments.get(i), ball.getCircle(),
                        ball.getVelocity()) < Board.TIME) {
                    Vect newVelocity = Physics.reflectWall(lineSegments.get(i), ball.getVelocity(), COEFFICIENT);
                    return new Ball(ball.getName(), ball.getLocation(), newVelocity);
                }
            }
        }
        for (int i = 0; i < circles.size(); i++) {
            if (this.stillNeedToMove) {
                if (Physics.timeUntilRotatingCircleCollision(circles.get(i), centerOfRotation, ANGULAR_VELOCITY_RADIAN, ball.getCircle(), ball.getVelocity()) < Board.TIME) {
                    Vect newVelocity = Physics.reflectRotatingCircle(circles.get(i), centerOfRotation, ANGULAR_VELOCITY_RADIAN, ball.getCircle(), ball.getVelocity(), COEFFICIENT);
                    return new Ball(ball.getName(), ball.getLocation(), newVelocity);
                }
            } else {
                if (Physics.timeUntilCircleCollision(circles.get(i), ball.getCircle(), ball.getVelocity()) < Board.TIME) {
                    Vect newVelocity = Physics.reflectCircle(circles.get(i).getCenter(), ball.getLocation(),
                            ball.getVelocity(), COEFFICIENT);
                    return new Ball(ball.getName(), ball.getLocation(), newVelocity);
                }
            }

        }
        return new Ball(ball.getName(), ball.getLocation(), ball.getVelocity());
    }

    @Override 
    public boolean triggered(Ball ball) {
        for (int i = 0; i < lineSegments.size(); i++) {
            if (this.stillNeedToMove) {
                if (Physics.timeUntilRotatingWallCollision(lineSegments.get(i), centerOfRotation, ANGULAR_VELOCITY_RADIAN, ball.getCircle(), ball.getVelocity()) < Board.TIME) {         
                    return true;
                }
            } else {
                if (Physics.timeUntilWallCollision(lineSegments.get(i), ball.getCircle(),
                        ball.getVelocity()) < Board.TIME) {
                    return true;
                }
            }

        }
        for (int i = 0; i < circles.size(); i++) {
            if (Physics.timeUntilRotatingCircleCollision(circles.get(i), centerOfRotation, ANGULAR_VELOCITY_RADIAN, ball.getCircle(), ball.getVelocity()) < Board.TIME) {
                return true;
            } else {
                if (Physics.timeUntilCircleCollision(circles.get(i), ball.getCircle(), 
                        ball.getVelocity()) < Board.TIME) {
                    return true;
                }
            }
        }
        return false;    
    }

    @Override 
    public String getName() {
        return this.name;
    }

    @Override 
    public void draw(Graphics g) {
        g.setColor(COLOR);
        final int pointsInRectangle = 4;
        final int[] xPoints = new int[pointsInRectangle];
        final int[] yPoints = new int[pointsInRectangle];
        List<Vect> allPoints = new ArrayList<Vect>();
        LineSegment leftLine = this.lineSegments.get(0);
        LineSegment topLine = this.lineSegments.get(1);
        LineSegment rightLine = this.lineSegments.get(2);
        LineSegment bottomLine = this.lineSegments.get(3);

        Vect topLeftPoint = leftLine.p1();
        Vect topRightPoint = topLine.p2();
        Vect bottomRightPoint = rightLine.p2();
        Vect bottomLeftPoint = bottomLine.p1();

        allPoints.add(topLeftPoint);
        allPoints.add(topRightPoint);
        allPoints.add(bottomRightPoint);
        allPoints.add(bottomLeftPoint);

        for (int i=0; i<allPoints.size(); i++) {
            Vect eachPoint = allPoints.get(i);
            double x  = eachPoint.x() * Board.PIXEL_PER_L;
            double y = eachPoint.y() * Board.PIXEL_PER_L;
            xPoints[i] = (int) Math.round(x);
            yPoints[i] = (int) Math.round(y);
        }
        g.fillPolygon(xPoints, yPoints, pointsInRectangle);
    }
    
    /**
     * get the center of rotation for a flipper
     * @return the center location of a flipper
     */
    private Vect getCenterOfRotation() {
        if (variation.equals(FlipperLeftOrRight.LEFT_FLIPPER)) {
            if (this.orientation.equals(Angle.ZERO)) {
                return this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE/2.0, LENGTH_OF_SHORTER_SIDE/2.0));
            } else if (orientation.equals(Angle.DEG_90)) {
                return this.location.plus(new Vect(BOUNDING_L - LENGTH_OF_SHORTER_SIDE/2.0, LENGTH_OF_SHORTER_SIDE/2.0));
            } else if (orientation.equals(Angle.DEG_180)) {
                return this.location.plus(new Vect(BOUNDING_L - LENGTH_OF_SHORTER_SIDE/2.0, BOUNDING_L - LENGTH_OF_SHORTER_SIDE/2.0));
            } else if (orientation.equals(Angle.DEG_270)) {
                return this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE/2.0, BOUNDING_L - LENGTH_OF_SHORTER_SIDE/2.0));
            }
        } else {
            if (this.orientation.equals(Angle.ZERO)) {
                return this.location.plus(new Vect(BOUNDING_L - LENGTH_OF_SHORTER_SIDE/2.0, LENGTH_OF_SHORTER_SIDE/2.0));
            } else if (orientation.equals(Angle.DEG_90)) {
                return this.location.plus(new Vect(BOUNDING_L - LENGTH_OF_SHORTER_SIDE/2.0, BOUNDING_L - LENGTH_OF_SHORTER_SIDE/2.0));
            } else if (orientation.equals(Angle.DEG_180)) {
                return this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE/2.0, BOUNDING_L - LENGTH_OF_SHORTER_SIDE/2.0));
            } else if (orientation.equals(Angle.DEG_270)) {
                return this.location.plus(new Vect(LENGTH_OF_SHORTER_SIDE/2.0, LENGTH_OF_SHORTER_SIDE/2.0));
            }
        }
        return this.location;
    }
    
    @Override 
    public String toString() {
        return "Flipper: " + name + " @ " + location.toString() + ", Rotated: " + orientation.toString() + "at rotation " + this.angleOfRotation.toString() + "\n";
    }

    @Override 
    public int hashCode() {
        return name.hashCode() + location.hashCode() + orientation.hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof Flipper)) { return false; }
        final Flipper that = (Flipper) obj;
        return this.name       .equals(that.name) && 
               this.location   .equals(that.location) && 
               this.orientation.equals(that.orientation);
    }
}
