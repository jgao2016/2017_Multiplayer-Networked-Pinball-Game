package flingball;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import physics.Circle;
import physics.LineSegment;
import physics.Physics;
import physics.Vect;
/**
 * An immutable absorber in the flingball game, an implementation of Gadget
 */
public class Absorber implements Gadget {
    private final String name;
    private final Vect location;
    private final Vect size;
    private final List<LineSegment> lineSegments;
    private final List<Circle> circles;
    
    private static final Color COLOR = Color.PINK;
    
    // Abstraction Function
    //  AF(name, location, size, lineSegments, circles) = A 2D image of a rectangular 
    //           object whose name is name. In a pinball game, this represents an object that absorbs
    //           balls that hit it or another object, and fires balls that it has cached. The object's 
    //           top left corner is located at coordinates 
    //           (x, y), where (x, y) are integers between 0 and 19 determine the absorber's position
    //           on a pinball board. The height and width of the rectangle are given by size's (x, y) 
    //           components. lineSegments represents the individual line segment components that make 
    //           up the boards of the rectangular image. circles represents the circles that lie at all
    //           of the corners of the rectangle image to make smooth corners.
    // Representation Invariant
    //  --| location must be integers, [0, 19]
    //  --| width and height given by x,y of size must be positive integers <= 20
    //  --| lineSegments size must be equal to the number of sides of a rectangle (4)
    //  --| circles size must be equal to the number of corners of a rectangle (4)
    //  --| other things that would break representation exposure (incorrect names) fail faster in
    //          other classes such as BoardParser
    // Safety from Representation Exposure
    //  --| all fields are private and final
    //  --| all methods return immutable types
    // Thread Safety:
    //  --| All fields are private and final. All instances are immutable. no rep exposure.
    //  --| Used thread safe datatype for lists and maps.
    
 // Checks the Representation Invariant to ensure that no representation exposure occurs
    private void checkRep() {
        // sanity checks :)
        assert name != null;
        assert location != null;
        assert size != null;
        assert lineSegments != null;
        assert circles != null;
        
        final int numberOfEdges = 4;
        assert lineSegments.size() == numberOfEdges;
        assert circles.size() == numberOfEdges;
        
        assert (int) 0 <= location.x() && location.x() <= Board.L-1;
        assert (int) 0 <= location.y() && location.y() <= Board.L-1;
        
        assert (int) 0 < size.x() && size.x() <= Board.L;
        assert (int) 0 < size.y() && size.y() <= Board.L;
    }
    
    /**
     * Construct a new Absorber with given location, size
     */
    Absorber(String name, Vect location, Vect size){
        this.name = name;
        this.location = location;
        this.size = size;
        Vect locationTopRight=location.plus(new Vect(size.x(),0));
        Vect locationBottomLeft=location.plus(new Vect(0, size.y()));
        Vect locationBottomRight=location.plus(size);
        lineSegments = Collections.synchronizedList(new ArrayList<>());
        circles = Collections.synchronizedList(new ArrayList<>());
        lineSegments.add(new LineSegment(locationBottomLeft,locationBottomRight));
        lineSegments.add(new LineSegment(location,locationTopRight));
        lineSegments.add(new LineSegment(location,locationBottomLeft));
        lineSegments.add(new LineSegment(locationTopRight,locationBottomRight));
        circles.add(new Circle(location, 0));
        circles.add(new Circle(locationTopRight, 0));
        circles.add(new Circle(locationBottomLeft, 0));
        circles.add(new Circle(locationBottomRight, 0));
        
        checkRep();
    }
    
    /**
     * @return the width of the Absorber. 
     */
    public int getWidth() {
        return (int) size.x();
    }
    
    /**
     * @return the height of the Absorber. 
     */
    public int getHeight() {
        return (int) size.y();
    }
    
    /**
     * @return the size of the Absorber. 
     */
    public Vect getSize() {
        return size;
    }
    
    /**
     * @return one of the balls the Absorber holds
     */
    protected Ball action(String ballName) {
        final double distanceToEdge = 0.25;
        final int velocity = -50;
        Vect ballLocation = location.plus(new Vect(size.x()-distanceToEdge,0));
        return new Ball(ballName, ballLocation, new Vect(0, velocity));
    }
    
    @Override
    public Vect getLocation() {
        return location;
    }
        
    @Override
    public boolean triggered(Ball ball) {
        for (int i = 0; i < lineSegments.size(); i++) {
            if (Physics.timeUntilWallCollision(
                    lineSegments.get(i), ball.getCircle(), ball.getVelocity()) < Board.TIME) {
                return true;
            }
        }
        for (int i = 0; i < circles.size(); i++) {
            if (Physics.timeUntilCircleCollision(
                    circles.get(i), ball.getCircle(), ball.getVelocity()) < Board.TIME) {
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
    public Color getColor() {
        return COLOR;
    }
    
    @Override
    public void draw(Graphics g) {
        final int xCoord = (int) location.x() * Board.PIXEL_PER_L;
        final int yCoord = (int) location.y() * Board.PIXEL_PER_L;
        g.setColor(COLOR);
        g.fillRect(xCoord, yCoord, Board.L * getWidth(), Board.L * getHeight());
        g.drawRect(xCoord, yCoord, Board.L * getWidth(), Board.L * getHeight());
        
        // no sharp edges!
        final int startAngleDegrees = 0;
        final int arcAngleDegrees = 360;
        g.fillArc(xCoord, yCoord, 0, 0, startAngleDegrees, arcAngleDegrees);
        g.fillArc(xCoord + Board.L, yCoord, 0, 0, startAngleDegrees, arcAngleDegrees);
        g.fillArc(xCoord, yCoord + Board.L, 0, 0, startAngleDegrees, arcAngleDegrees);
        g.fillArc(xCoord + Board.L, yCoord + Board.L, 0, 0, startAngleDegrees, arcAngleDegrees);
    }
    
    @Override 
    public String toString() {
        return "Absorber: " + name + " @ " + location.toString() + 
                ", " + getWidth() + "x" + getHeight() + "\n";
    }

    @Override 
    public int hashCode() {
        return name.hashCode() + location.hashCode() + size.hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if ( ! (obj instanceof Absorber)) { return false; }
        final Absorber that = (Absorber) obj;
        return this.name    .equals(that.name) &&
               this.location.equals(that.location) &&
               this.size    .equals(that.size);
    }

}
