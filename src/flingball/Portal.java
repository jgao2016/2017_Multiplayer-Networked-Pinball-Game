package flingball;

import java.awt.Color;
import java.awt.Graphics;

import physics.*;
/**
 * An immutable portal in the Flingball game, an implementation of the Gadget interface
 */
public class Portal implements Gadget {
    
    private final String name;
    private final Vect location;
    private final Vect center;
    private final String toBoard;
    private final String otherPortal;
    private final Circle circle;
    
    private static final int DIAMETER = 1;
    private static final double RADIUS = 0.5;
    private static final Color COLOR = new Color(108, 195, 189);
    
    // Abstraction Function
    //  AF(name, location, circle, center, toBoard, otherPortal) = a portal with the given name and coordinates,
    //                    that links to another portal using otherPortal and (optionally) otherBoard, when a ball 
    //                    collides with the source portal, then the ball is immediately teleported to the target portal, 
    //                    exiting the target portal with the same velocity vector, with which it entered the source portal. 
    //                    If the target portal does not exist (e.g., otherPortal is not found on the board, or otherBoard is 
    //                    not currently the name of a board connected to the server), then the ball passes unaffected over 
    //                    the source portal, without teleporting or reflecting.
    // Representation Invariant
    // --| all fields cannot be null
    // --| the location of the portal has to be within the boundaries of the board
    // Safety from Representation Exposure
    // --| all fields are private and final
    // --| all methods return immutable types
  
    private void checkRep() {
        assert name != null;
        assert location != null;
        assert circle != null;
        assert toBoard != null;
        assert otherPortal != null;
        assert center != null;
        
        assert (int) 0 <= location.x() && location.x() <= Board.L - 1;
        assert (int) 0 <= location.y() && location.y() <= Board.L - 1;
    }

    /**
     * Construct a Flipper with given location and target portal
     */
    Portal(String name, String otherPortal, String toBoard, Vect location) {
        this.location = location;
        this.center=location.plus(new Vect(RADIUS,RADIUS));
        this.name = name;
        this.otherPortal = otherPortal;
        this.toBoard = toBoard;
        this.circle = new Circle(location.plus(new Vect(RADIUS, RADIUS)), RADIUS);
        checkRep();
    }
    
    @Override
    public boolean triggered(Ball ball) {
        Vect ballCenter=ball.getLocation();
        if(Physics.timeUntilCircleCollision(circle, ball.getCircle(), ball.getVelocity()) < Board.TIME
                && ball.getVelocity().dot(this.center.minus(ballCenter)) > 0) {
            return true;
        }else {
            return false;
        }
    }
    
    /**
     * @return the other board's name
     */
    public String getOtherBoardName() {
        return toBoard;
    }
    
    /**
     * @return the center of this portal
     */
    public Vect getCenter() {
        return center;
    }
    
    /**
     * @return the other portal's name
     */
    public String getOtherPortalName() {
        return otherPortal;
    }

    @Override 
    public Vect getLocation() {
        return this.location;
    }

    @Override 
    public Color getColor() {
        return Portal.COLOR;
    }

    @Override 
    public String getName() {
        return this.name;
    }

    @Override 
    public void draw(Graphics g) {
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
        return this.name + "@" + this.location.toString();
    }

    @Override 
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override 
    public boolean equals(Object obj) {
        if (!(obj instanceof Portal)) { return false; }
        final Portal that = (Portal) obj;
        return this.name    .equals(that.name) && 
               this.location.equals(that.location) && 
               this.toBoard.equals(that.getOtherBoardName()) && 
               this.otherPortal.equals(that.getOtherPortalName());
    }

}
