package flingball;

import java.awt.Color;
import java.awt.Graphics;

import physics.*;

/**
 * An immutable gadget in the flingball game
 */
public interface Gadget {

    /**
     * Get the location of a Gadget on a Board object.
     * 
     * @return the top-left location of the Gadget. For a ball,
     *         the location is the location of the circle's center.
     */
    public Vect getLocation();

    /**
     * Check if a Gadget is triggered (hit) by the given ball.
     * 
     * @param ball the ball to test and see if it has collided with the Gadget.
     * @return true if this Gadget is triggered (hit), false otherwise
     */
    public boolean triggered(Ball ball);

    /**
     * Gets the color of the Gadget.
     * 
     * @return the color of the Gadget
     */
    public Color getColor();

    /**
     * Draws a Gadget on a Graphics object.
     * 
     * @param g the 2D Graphics render on which to draw.
     */
    public void draw(Graphics g);

    /**
     * Gets the Gaget's name.
     * 
     * @return return the name of a Gadget.
     */
    public String getName();
}
