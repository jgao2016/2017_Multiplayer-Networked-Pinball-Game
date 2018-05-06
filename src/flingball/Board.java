package flingball;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.experimental.theories.PotentialAssignment;

import physics.Angle;
import physics.LineSegment;
import physics.Physics;
import physics.Vect;
enum BOARD_DIRECTION{
    TOP,LEFT,BOTTOM,RIGHT;
}
enum KEYEVENT {
    UP, DOWN;
}
/**
 * A mutable game board of the Flingball game
 */
public class Board {
    
    private final String name;
    private final List<Ball> balls;
    private final List<Bumper> staticBumpers;
    private final List<Gadget> staticGadgets;
    private final List<Absorber> absorbers;
    private final List<LineSegment> walls;
    private final List<Flipper> flippers;
    private final List<Portal> portals;
    private final float gravity;
    private final float friction1;  // units of per second
    private final float friction2;  // units of per L
    
    private final Map<Gadget, List<Absorber>> triggerTargetAbsorberMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Gadget, List<Flipper>> triggerTargetFlipperMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Integer, List<Absorber>> keyupTriggerAndAbsorberMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Integer, List<Absorber>> keydownTriggerAndAbsorberMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Integer, List<Flipper>> keyupTriggerAndFlipperMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Integer, List<Flipper>> keydownTriggerAndFlipperMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Absorber, List<String>> absorberBallNamesMap = Collections.synchronizedMap(new HashMap<>());

    // other boards. Since board names must not be empty, we use an empty string to 
    // indicate no joined board on this edge
    private String left = "";
    private String right = "";
    private String top = "";
    private String bottom = "";
    
    private Optional<Flingball> flingball;
    
    private final Color color = Color.WHITE;
    private final static int MILLISECONDS_PER_SECOND=1000;
    public final static double TIME = Simulator.TIMER_INTERVAL_MILLISECONDS/(double)MILLISECONDS_PER_SECOND;
    public final static int L = 20;  // 1L = 20 pixels
    public final static int PIXEL_PER_L = 20;
    private final static float GRAVITY_DEFAULT = 25;  // units of L/(sec^2)
    private final static float FRICTION_DEFAULT = 0.025f;
   
    
    
    // Abstraction Function:
    //  AF(name, balls, staticBumpers, absorbers, walls, flippers, portals, absorberBallNamesMap
    //     triggerTargetAbsorberMap, triggerTargetFlipperMap,keyupTriggerAndAbsorberMap,keydownTriggerAndAbsorberMap
    //     keyupTriggerAndFlipperMap,keydownTriggerAndFlipperMap,left,right, top, bottom ) = 
    //      A pinball board where the Walls in walls come together to make the rectangular 
    //      dimensions/perimeter of the board. The board has a label provided by name. 
    //      Additionally, every The board has a number of objects on it, 
    //      namely absorbers, portals, flippers and bumpers, that influence the movement of 
    //      balls on the surface of the board.
    //      the trigger maps keeps the relationships between gadgets and the absorber(s)/flippers 
    //      they trigger. keyup and keydown maps keeps the relationships between gadgets and the 
    //      absorber(s)/flippers. absorberBallNamesMap keeps the relationships between absorbers
    //      and the balls' names they hold.
    //      It may also have joined boards, their names are left,right, top, bottom. If any wall is 
    //      not joined to otherboards, the string is empty.
    //      It also have an Optional field flingball. If this board is in server client mode, this field
    //      is the flingball object that plays this board. Otherwise this field is empty.
    // Rep Invariant:
    //  - All fields not null
    // Safety from Rep Exposure:
    //  --| All fields are private and final. All gadgets are immutable.
    //  --| All getter methods that return mutable objects implement defensive copying.
    //  --| Other return types are immutable and thus safe for returning.
    // Thread Safety:
    //  --| Used monitor pattern. Used synchronized keyword in all instance methods.
    //  --| All fields are private and final. All gadgets are immutable. no rep exposure.
    //  --| Used thread safe datatype for lists and maps.
    
    
    /*
     * Checks the Representation Invariant to ensure that no representation exposure occurs
     */
    private synchronized void checkRep() {
        assert name != null;
        assert balls != null;
        assert staticBumpers != null;
        assert absorbers != null;
        assert walls != null;
        assert portals!=null;
        assert flippers!=null;
        assert friction1 >= 0;
        assert friction2 >= 0;
        assert flingball !=null;
    }
    
    /**
     * Construct a new empty Board
     * 
     * @param name the name of the Board
     */
    Board(String name) {
        this(name, GRAVITY_DEFAULT, FRICTION_DEFAULT, FRICTION_DEFAULT);
        checkRep();
    }

    /**
     * Construct a new empty board of Flingball
     * 
     * @param name String name of the board
     * @param gravity float representing L/(seconds^2) that influences Ball movement
     * @param friction1 float friction to scale collisions by (mu1 per second)
     *                  that influences Ball movement
     * @param friction2 float friction to scale collisions by (mu2 per L)
     *                  that influences Ball movement
     */
    Board(String name, float gravity, float friction1, float friction2) {
        this.name = name;
        this.balls = Collections.synchronizedList(new LinkedList<>());
        this.staticBumpers = Collections.synchronizedList(new LinkedList<>());
        this.absorbers = Collections.synchronizedList(new LinkedList<>());
        this.flippers = Collections.synchronizedList(new LinkedList<>());
        this.portals = Collections.synchronizedList(new LinkedList<>());
        this.staticGadgets= Collections.synchronizedList(new LinkedList<>());
        this.walls = constructWalls();
        this.gravity = gravity;
        this.friction1 = friction1;
        this.friction2 = friction2;
        this.flingball = Optional.empty();
        checkRep();
    }


    /*
     * Construct the four walls of the board. The dimensions are 20L x 20L
     */
    private static List<LineSegment> constructWalls() {
        final List<LineSegment> boardWalls = new LinkedList<>();
        boardWalls.add(new LineSegment(new Vect(0, 0), new Vect(L, 0)));
        boardWalls.add(new LineSegment(new Vect(0, L), new Vect(L, L)));
        boardWalls.add(new LineSegment(new Vect(0, 0), new Vect(0, L)));
        boardWalls.add(new LineSegment(new Vect(L, 0), new Vect(L, L)));
        return boardWalls;
    }
        
    /**
     * @return flippers of this board
     */
    public synchronized List<Flipper> getFlippers(){
        return Collections.unmodifiableList(flippers);
    }
    /**
     * @return portals of this board
     */
    public synchronized List<Portal> getPortals(){
        return Collections.unmodifiableList(portals);
    }

    /**
     * @param portalName the name of portal
     * @return portal with portalName in this board
     * requires such portal exist
     */
    public synchronized Portal getPortalWithName(String portalName) {
        for(Portal portal:portals) {
            if(portal.getName().equals(portalName)) return portal;
        }
        throw new RuntimeException("no such portal");
    }

    /**
     * add a portal to this board which has no target portal
     * @param portal this portal on this board
     */
    public synchronized void addPortal(Portal portal) {
        portals.add(portal);
        staticGadgets.add(portal);
    }

    /**
     * add a Flipper instance to the board
     * @param flipper flipper to be added
     */
    public synchronized void addFlipper(Flipper flipper) {
        flippers.add(flipper);
    }
    
    /**
     * do the action when keyEvent e is pressed
     * do nothing if this key can't trigger any action
     * @param keyCode key code
     */
    public synchronized void keyPressed(int keyCode) {
        if(keydownTriggerAndAbsorberMap.containsKey(keyCode)) {
            for(Absorber absorber:keydownTriggerAndAbsorberMap.get(keyCode)) {
                if(absorberBallNamesMap.get(absorber).size()>0) {
                    String ballName = absorberBallNamesMap.get(absorber).get(0);
                    absorberBallNamesMap.get(absorber).remove(0);
                    balls.add(absorber.action(ballName));
                }
            }
        }
        if(keydownTriggerAndFlipperMap.containsKey(keyCode)) {
            for(Flipper flipper:keydownTriggerAndFlipperMap.get(keyCode)) {
                Optional<Flipper> actualFlipper = findFlipper(flipper.getName());
                if (actualFlipper.isPresent()) {
                    flippers.remove(actualFlipper.get());
                    Flipper newFlipper = initiateFlipperMovement(actualFlipper.get());
                    flippers.add(newFlipper);
                }
            }
        }
    }
    
    /**
     * do the action when keyEvent e is released
     * do nothing if this key can't trigger any action
     * @param keyCode key code
     */

    public synchronized void keyReleased(int keyCode) {
        if(keyupTriggerAndAbsorberMap.containsKey(keyCode)) {
            for(Absorber absorber:keyupTriggerAndAbsorberMap.get(keyCode)) {
                if(absorberBallNamesMap.get(absorber).size()>0) {
                    String ballName = absorberBallNamesMap.get(absorber).get(0);
                    absorberBallNamesMap.get(absorber).remove(0);
                    balls.add(absorber.action(ballName));
                }
            }
        }
        if(keyupTriggerAndFlipperMap.containsKey(keyCode)) {
            for(Flipper flipper:keyupTriggerAndFlipperMap.get(keyCode)) {
                Optional<Flipper> actualFlipper = findFlipper(flipper.getName());
                if (actualFlipper.isPresent()) {
                    flippers.remove(actualFlipper.get());
                    Flipper newFlipper = initiateFlipperMovement(actualFlipper.get());
                    flippers.add(newFlipper);
                }
            }
        }
    }
    
    private Optional<Flipper> findFlipper(String flipperName) {
        for (int i=0; i<this.flippers.size(); i++) {
            Flipper currentFlipper = this.flippers.get(i);
            if (currentFlipper.getName().equals(flipperName)) {
                return Optional.of(currentFlipper);
            }
        }
        return Optional.empty();
    }

    private synchronized List<LineSegment> constructJoinedWalls() {
        List<LineSegment> newWalls=new LinkedList<>();
        if(left.equals("")) {
            newWalls.add(new LineSegment(new Vect(0, 0), new Vect(0, L)));
        }
        if(right.equals("")) {
            newWalls.add(new LineSegment(new Vect(L, 0), new Vect(L, L)));
        }
        if(top.equals("")) {
            newWalls.add(new LineSegment(new Vect(0, 0), new Vect(L, 0)));
        }
        if(bottom.equals("")) {
            newWalls.add(new LineSegment(new Vect(0, L), new Vect(L, L)));
        }
        
        return newWalls;
    }
    /**
     * join given board to this board
     * @param otherBoard the board to be joined
     * @param direction to join the board
     */
    public synchronized void joinBoard (String otherBoard, BOARD_DIRECTION direction) {
        switch (direction) {
            case LEFT: 
                ////System.out.println(name+" -left "+otherBoard);
                this.left = otherBoard;
                break;
            case RIGHT: 
                ////System.out.println(name+" -rig "+otherBoard);
                this.right = otherBoard;
                break;
            case TOP: 
                this.top = otherBoard;
                ////System.out.println(name+" -top "+otherBoard);
                break;
            case BOTTOM: 
                this.bottom = otherBoard;
                ////System.out.println(name+" -bot "+otherBoard);
                break;
        }
        this.walls.clear();
        this.walls.addAll(constructJoinedWalls());
    }
    
    /**
     * disjoin given board and this board
     * if given board was not joined with this board, do nothing
     * @param otherBoard to disjoin the board
     */
    public synchronized void disjoinBoard (String otherBoard) {
        if(left.equals(otherBoard)) {
            left="";
        }
        if(right.equals(otherBoard)) {
            right="";
        }
        if(top.equals(otherBoard)) {
            top="";
        }
        if(bottom.equals(otherBoard)) {
            bottom="";
        }
        this.walls.clear();
        this.walls.addAll(constructJoinedWalls());
    }
    
    /**
     * @param board direction of the other board
     * @return true if such board exist, false otherwise
     */
    public synchronized boolean hasJoinedBoard(String board) {
        if (this.left.equals(board)) {return true; }
        if (this.right.equals(board)) {return true; }
        if (this.top.equals(board)) {return true; }
        if (this.bottom.equals(board)) {return true; } else {return false;}
    }
    /**
     * @return the joined board's name in given direction
     * requires such board exists
     * @param direction to attach the board
     */
    public synchronized String getJoinedBoard(BOARD_DIRECTION direction) {
        switch (direction) {
            case LEFT: return this.left;
            case RIGHT: return this.right;
            case TOP: return this.top;
            case BOTTOM: return this.bottom;
        }
        return "";
    }
    /*
     * construct a string with ball pos, ball velocity and ball name, seperated by one space.
     */
    private static String ballInfo(double posX, double posY,double veloX,double veloY,String ballName) {
        return posX + " " + posY + " " + veloX + " " + veloY + " " + ballName;
    }
    /**
     * Transfer a ball to an adjacent board
     * request form:"tran" BOARDNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME
     * @param BOARD_DIRECTION the edge to transfer the ball
     * @param Ball the ball to be transfered to another board
     */
    private synchronized void transferBall(BOARD_DIRECTION direction, Ball ball) {
        String ballRequest;
        String boardName;
        Vect offset;
        if (direction.equals(BOARD_DIRECTION.LEFT)) {
            boardName=left;
            offset= new Vect(20, 0);
        } else if (direction.equals(BOARD_DIRECTION.RIGHT)) {
            boardName=right;
            offset = new Vect(-20, 0);
        } else if (direction.equals(BOARD_DIRECTION.TOP)) {
            boardName=top;
            offset = new Vect(0, 20);
        } else {
            boardName=bottom;
            offset = new Vect(0, -20);
        }
        Ball newBallLeft = new Ball(ball.getName(), ball.getLocation().plus(offset), ball.getVelocity());
        double posX = newBallLeft.getLocation().x();
        double posY = newBallLeft.getLocation().y();
        double veloX = newBallLeft.getVelocity().x();
        double veloY = newBallLeft.getVelocity().y();
        String ballName = ball.getName();
        ballRequest = "tran "+ boardName + " " + ballInfo(posX, posY, veloX, veloY, ballName);
        try {
            String reply=flingball.get().sendBallRequest(ballRequest);
            if(reply.split(" ")[0].equals("succeed:")) {
                this.balls.remove(ball);
            }else {
                System.err.println("transfer send ball failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Receive a ball
     * @param newBallInfo new ball's info
     */
    public synchronized void receiveBall(String newBallInfo) {
        String[] splitted = newBallInfo.split(" "); // posX + " " + posY + " " + veloX + " " + veloY + " " + ballName;
        // create a new ball
        double posX = Double.parseDouble(splitted[0]);
        double posY = Double.parseDouble(splitted[1]);
        double veloX = Double.parseDouble(splitted[2]);
        double veloY = Double.parseDouble(splitted[3]);
        Ball ball = new Ball(splitted[4], new Vect(posX, posY), new Vect(veloX, veloY));
        // add to the current ball
        this.balls.add(ball);
    }
    
    /**
     * sets the flingball that contains this board instance
     * @param fb
     */
    public synchronized void setFlingball(Optional<Flingball> fb) {
        this.flingball = fb;
    }

    
    /**
     * @param portalName portalName
     * @return return true if  this board has a name portalName
     */
    public synchronized boolean hasPortal(String portalName) {
        for (Portal portal: this.portals) {
            if (portal.getName().equals(portalName)) {
                return true;
            }
        }
        return false;
    }
    /**
     * get portal with given name, requires such portal exist
     * @param portalName portal name
     * @return portal with given name
     */
    public synchronized Portal getPortal(String portalName) {
        for (Portal portal: this.portals) {
            if (portal.getName().equals(portalName)) {
                return portal;
            }
        }
        throw new RuntimeException("should never get here");
    }

    /**
     * Add a new Ball to this board
     * 
     * @param newBall the singular new ball to add to the Board
     */
    public synchronized void addBall(Ball newBall) {
        balls.add(newBall);
        checkRep();
    }
    
    public synchronized void removeBall(Ball ball) {
        this.balls.remove(ball);
        checkRep();
    }

    /**
     * Add a new Bumper to this board
     * 
     * @param newBumper the singular ball to add to the Board
     */
    public synchronized void addBumper(Bumper newBumper) {
        staticBumpers.add(newBumper);
        staticGadgets.add(newBumper);
        checkRep();
    }

    /**
     * Add a new Absorber to this board
     * 
     * @param newAbsorbor the singular Absorber to add to the Board
     */
    public synchronized void addAbsorber(Absorber newAbsorbor) {
        absorbers.add(newAbsorbor);
        absorberBallNamesMap.put(newAbsorbor,new ArrayList<>());
        staticGadgets.add(newAbsorbor);
        checkRep();
    }

    /**
     * @return the list of Balls that are currently on the Board
     */
    public synchronized List<Ball> getBalls() {
        List<Ball> ballsCopy = new LinkedList<Ball>();
        for (Ball ball : balls)
            ballsCopy.add(ball);
        return ballsCopy;
    }

    /**
     * @return the list of Bumpers that are currently on the Board
     */
    public synchronized List<Bumper> getStaticBumpers() {
        List<Bumper> bumpersCopy = new LinkedList<Bumper>();
        for (Bumper bumper : staticBumpers)
            bumpersCopy.add(bumper);
        return bumpersCopy;
    }
    /**
     * @param gadgetName gadgetName
     * @return gadget with given name
     * requires such gadget exist
     */
    public synchronized Gadget getGadgetWithName(String gadgetName) {
        List<Gadget> flipper=flippers.stream().filter(g->g.getName().equals(gadgetName)).collect(Collectors.toList());
        List<Gadget> staticGadget=staticGadgets.stream().filter(g->g.getName().equals(gadgetName)).collect(Collectors.toList());
        assert flipper.size()+staticGadget.size()==1;
        if(staticGadget.size()>0)return staticGadget.get(0);
        else return flipper.get(0);
    }
    /**
     * @return the list of Absorbers that are currently on the Board
     */
    public synchronized List<Absorber> getAbsorbers() {
        List<Absorber> absorberCopy = new LinkedList<Absorber>();
        for (Absorber absorber : absorbers)
            absorberCopy.add(absorber);
        return absorberCopy;
    }

    /**
     * @return the list of all static Gadgets (Bumpers and Absorbers)
     *         that are currently on the Board
     */
    public synchronized List<Gadget> getStaticGadgets() {
        return Collections.unmodifiableList(staticGadgets);
    }
    
    /**
     * @return the Gravity of the Board
     */
    public synchronized float getGravity() {
        return gravity;
    }

    /**
     * @return the Friction (mu1 per second) of the Board
     */
    public synchronized float getFriction1() {
        return friction1;
    }

    /**
     * @return the Friction (mu2 per L) of the Board
     */
    public synchronized float getFriction2() {
        return friction2;
    }

    /**
     * @return the name of the Board
     */
    public synchronized String getName() {
        return name;
    }

    /**
    * Returns the Absorbers that will perform an action when the trigger 
    * Gadget is triggered (hit by a Ball)
    * 
    * @param trigger Gadget to find the corresponding Target Absorbers of
    * @return List of Absorbers that respond when the  corresponding trigger Gadget is hit. 
    *         If trigger has no target Absorbers, returns an empty List.
    */
    public synchronized List<Absorber> getTargetAbsorbers(Gadget trigger) {
        if (!triggerTargetAbsorberMap.containsKey(trigger)) { return new ArrayList<Absorber>();}
        return triggerTargetAbsorberMap.get(trigger);
    }
    
    /**
     * Returns the Absorbers that will perform an action when the key 
     * is pressed (or released)
     * 
     * @param triggerKeyCode the key code of the trigger
     * @param keyUp return the absorbers triggered by releasing this key if keyUp is true 
     * return the absorbers triggered by pressing this key if keyUp is false
     * @return List of Absorbers that respond when the  corresponding trigger Gadget is hit. 
     *         If trigger has no target Absorbers, returns an empty List.
     */
    public synchronized List<Absorber> getTargetAbsorbers(int triggerKeyCode, boolean keyUp) {
        if(keyUp) {
            if(keyupTriggerAndAbsorberMap.containsKey(triggerKeyCode)) {
                return keyupTriggerAndAbsorberMap.get(triggerKeyCode);
            }else {
                return new LinkedList<Absorber>();
            }
        }else {
            if(keydownTriggerAndAbsorberMap.containsKey(triggerKeyCode)) {
                return keydownTriggerAndAbsorberMap.get(triggerKeyCode);
            }else {
                return new LinkedList<Absorber>();
            }
        }
    }
    
    /**
     * Returns the flippers that will perform an action when the key 
     * is pressed (or released)
     * 
     * @param triggerKeyCode the key code of the trigger
     * @param keyUp return the flippers triggered by releasing this key if keyUp is true 
     * return the flippers triggered by pressing this key if keyUp is false
     * @return List of flippers that respond when the  corresponding trigger Gadget is hit. 
     *         If trigger has no target flippers, returns an empty List.
     */
     public synchronized List<Flipper> getTargetFlippers(int triggerKeyCode, boolean keyUp) {
         if(keyUp) {
             if(keyupTriggerAndFlipperMap.containsKey(triggerKeyCode)) {
                 return keyupTriggerAndFlipperMap.get(triggerKeyCode);
             }else {
                 return new LinkedList<Flipper>();
             }
         }else {
             if(keydownTriggerAndFlipperMap.containsKey(triggerKeyCode)) {
                 return keydownTriggerAndFlipperMap.get(triggerKeyCode);
             }else {
                 return new LinkedList<Flipper>();
             }
         }
     }
     
    /**
     * Returns the flippers that will perform an action when the trigger 
     * Gadget is triggered (hit by a Ball)
     * 
     * @param trigger Gadget to find the corresponding Target Absorbers of
     * @return List of flippers that respond when the  corresponding trigger Gadget is hit. 
     *         If trigger has no target flippers, returns an empty List.
     */
     public synchronized List<Flipper> getTargetFlippers(Gadget trigger) {
         if (!triggerTargetFlipperMap.containsKey(trigger)) { return new ArrayList<Flipper>();}
         return triggerTargetFlipperMap.get(trigger);
     }
    
    /**
    * Sets the relationship of triggers and actions between gadgets. When a gadget
    * is triggered, an action will occur in all of its target Absorbers
    * 
    * @param target Absorber that will performs an action when trigger is triggered
    * @param trigger Gadget that when triggered causes a response in target
    * @return boolean representing if the trigger-target pair were added.
    */
    public synchronized boolean setTarget(Absorber target, Gadget trigger) {
        if (triggerTargetAbsorberMap.containsKey(trigger) && triggerTargetAbsorberMap.get(trigger).contains(target)) {
            return false;
        }
        if (!triggerTargetAbsorberMap.containsKey(trigger)) {
            triggerTargetAbsorberMap.put(trigger, new LinkedList<>());
        }
        triggerTargetAbsorberMap.get(trigger).add(target);
        checkRep();
        return true;
    }
    /**
    * Sets the relationship of triggers and actions between keys and gadgets. 
    * when this key is pressed (or released), an action will occur in all of its target Absorbers
    * If keyUp is true, this action is triggered by releasing this key,
    * and if keyUp is true, this action is triggered by pressing this key
    * @param target Absorber that will performs an action when trigger is triggered
    * @param triggerKeyCode the key code of the trigger
    * @param keyUp if this action is triggered by releasing this key
    * @return boolean representing if the trigger-target pair were added.
    */
    public synchronized boolean setTarget(Absorber target, int triggerKeyCode, boolean keyUp) {
        if (keyUp) {
            if (!keyupTriggerAndAbsorberMap.containsKey(triggerKeyCode)) { 
                keyupTriggerAndAbsorberMap.put(triggerKeyCode, new LinkedList<>());
            }
            keyupTriggerAndAbsorberMap.get(triggerKeyCode).add(target);
            checkRep();
            return true;
        } else {
            if (!keydownTriggerAndAbsorberMap.containsKey(triggerKeyCode) ) {
                keydownTriggerAndAbsorberMap.put(triggerKeyCode, new LinkedList<>());
            }
            keydownTriggerAndAbsorberMap.get(triggerKeyCode).add(target);
            checkRep();
            return true;
        }
    }
    /**
     * Sets the relationship of triggers and actions between gadgets. When a gadget
     * is triggered, an action will occur in all of its target Flippers
     * 
     * @param target Flipper that will performs an action when trigger is triggered
     * @param trigger Gadget that when triggered causes a response in target
     * @return boolean representing if the trigger-target pair were added.
     */
     public synchronized boolean setTarget(Flipper target, Gadget trigger) {
         if (triggerTargetFlipperMap.containsKey(trigger) && triggerTargetFlipperMap.get(trigger).contains(target)) {
             return false;
         }
         if (!triggerTargetFlipperMap.containsKey(trigger)) {
             triggerTargetFlipperMap.put(trigger, new LinkedList<>());
         }
         triggerTargetFlipperMap.get(trigger).add(target);
         checkRep();
         return true;
     }
     /**
      * Sets the relationship of triggers and actions between keys and gadgets. 
      * when this key is pressed (or released), an action will occur in all of its target flippers
      * If keyUp is true, this action is triggered by releasing this key,
      * and if keyUp is true, this action is triggered by pressing this key
      * @param target flipper that will performs an action when trigger is triggered
      * @param triggerKeyCode the key code of the trigger
      * @param keyUp if this action is triggered by releasing this key
      * @return boolean representing if the trigger-target pair were added.
      */
      public synchronized boolean setTarget(Flipper target, int triggerKeyCode, boolean keyUp) {
          if (keyUp) {
              if (!keyupTriggerAndFlipperMap.containsKey(triggerKeyCode)) { 
                  keyupTriggerAndFlipperMap.put(triggerKeyCode, new LinkedList<>());
              }
              keyupTriggerAndFlipperMap.get(triggerKeyCode).add(target);
              checkRep();
              return true;
          } else {
              if (!keydownTriggerAndFlipperMap.containsKey(triggerKeyCode) ) {
                  keydownTriggerAndFlipperMap.put(triggerKeyCode, new LinkedList<>());
              }
              keydownTriggerAndFlipperMap.get(triggerKeyCode).add(target);
              checkRep();
              return true;
          }
      }
    /**
     * Update this board to the next frame
     */
    public synchronized void updateBoard() {
        List<Ball> newBalls = new ArrayList<>();
        
        // update collisions between balls
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++ ) {
                Ball currentBall = this.balls.get(i);
                Ball compareBall = this.balls.get(j);
                if (currentBall.triggered(compareBall)) {
                    Ball newCurrent = currentBall.getCollisionRedirection(compareBall);
                    Ball newCompare = compareBall.getCollisionRedirection(currentBall);
                    balls.set(i, newCurrent);
                    balls.set(j, newCompare);
                }
            }
        }
        for (Ball ball: balls) {
            ball=updateBallWithGravityAndFrictionInHalfTime(ball);
            boolean getAbsorbedorTeleported=false;
            for (Absorber absorber: absorbers) {
                //check if this ball gets caught by an absorber
                if (absorber.triggered(ball)) {
                    List<String> ballNames = absorberBallNamesMap.get(absorber);
                    ballNames.add(ball.getName());
                    getAbsorbedorTeleported=true;
                    updateActionedAbsorbersAndFlippers(absorber, newBalls);
                }
            }
            if(!getAbsorbedorTeleported) {
                for (Portal portal:portals) {
                    //check if this ball gets caught by an portal
                    if (portal.triggered(ball)) {
                        // update gadgets if any gadgets are triggered
                        updateActionedAbsorbersAndFlippers(portal, newBalls);
                        //try teleport ball to other portal
                        // if other board is this board
                        if(portal.getOtherBoardName().equals(name) ) {
                            // and other portal exists in this board, create new ball
                            if(hasPortal(portal.getOtherPortalName())) {
                                Portal otherPortal = getPortal(portal.getOtherPortalName());
                                ball=new Ball(ball.getName(), otherPortal.getCenter(),ball.getVelocity());
                            }
                        // if other portal in other board, try to teleport it
                        }else{
                            if(flingball.isPresent()) {
                                Flingball client=flingball.get();
                                try {
                                    // "port" BOARDNAME PORTALNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME "from" BOARDNAME
                                    String ballInfo=ballInfo(ball.getLocation().x(), ball.getLocation().y(), 
                                            ball.getVelocity().x(), ball.getVelocity().y(), ball.getName());
                                    String request = "port "+ portal.getOtherBoardName()+" "+portal.getOtherPortalName()
                                            +" "+ ballInfo +" from "+name;
                                    String reply = client.sendBallRequest(request);
                                    if(reply.split(" ")[0].equals("succeed:")) {
                                        getAbsorbedorTeleported=true;
                                    }else {
                                        System.err.println("portal send ball failed");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                if(!getAbsorbedorTeleported) {
                    for(Bumper bumper: staticBumpers) {
                        // find out all collisions
                        if(bumper.triggered(ball)) {
                            // update gadgets if any gadgets are triggered
                            updateActionedAbsorbersAndFlippers(bumper, newBalls);
                            // create a new Ball with new velocity
                            ball = bumper.getCollisionRedirection(ball);
                        }
                    }
                    // update the flipper movement is flipper is triggered by ball
                    List<Flipper> flippersCopy=new LinkedList<>();
                    flippersCopy.addAll(flippers);
                    for (Flipper flipper: flippersCopy) {
                        // TO-DO: need to think about this case more!!!!!
                        if(flipper.triggered(ball)) {
                            // TO-DO: Update action flipper as well, not only absorber
                            updateActionedAbsorbersAndFlippers(flipper, newBalls);
                            ball = flipper.getCollisionRedirection(ball);
                        }
                    }
                    
                    ball=getCollisionRedirectionWithWalls(ball);
                    if(!getAbsorbedorTeleported) {
                        ball=updateBallWithGravityAndFrictionInHalfTime(ball);
                        newBalls.add(ball);
                    }
                }
                
            }
            
        }
        balls.clear();
        for(int i=newBalls.size()-1;i>=0; i--) {
            Ball ball=newBalls.get(i);
            checkOutBoundary(ball,newBalls);
        }
        for(Ball ball:newBalls) {
            balls.add(updateBallLocation(ball));
        }

        // update the flipper movement
        List<Flipper> newFlipperList = new ArrayList<>();
        for (Flipper flipper: flippers) {
            if (flipper.stillNeedToMove()) {
                // if any flipper still need to move around, need to move those flipper slightly for drawing purpose
                Flipper newFlipperAfterMoving = updateFlipperMovement(flipper);
                newFlipperList.add(newFlipperAfterMoving);
            } else {
                newFlipperList.add(flipper);
            }
        }
        flippers.clear();
        flippers.addAll(newFlipperList);
        
        checkRep();
    }

    /**
     * checks if a ball is about to be transferred
     */
    public synchronized void checkOutBoundary(Ball ball,List<Ball> newBalls) {
        double x = ball.getLocation().x();
        double y = ball.getLocation().y();
        double v_x=ball.getVelocity().x();
        double v_y=ball.getVelocity().y();
        if(v_x==0 && v_y==0) return;
        
        final double delta=0.1;// double point imprecision
        final double minLocX = Ball.RADIUS + Math.abs(v_x)*TIME + delta;
        final double maxLocX = L - minLocX; 
        final double minLocY = Ball.RADIUS + Math.abs(v_y)*TIME + delta;
        final double maxLocY = L - minLocY; 

        boolean outLeftEdge=  (x <= minLocX && v_x < 0);
        boolean outRightEdge= (x >= maxLocX && v_x > 0);
        boolean outTopEdge=   (y <= minLocY && v_y < 0);
        boolean outBottomEdge=(y >= maxLocY && v_y > 0);
        if  (outLeftEdge && outTopEdge) {
            if(!left.equals("") && !top.equals("")) v_x=-v_x;
            else if (left.equals("")) v_x=-v_x;
            else if (top.equals("")) v_y=-v_y;
            newBalls.remove(ball);
            newBalls.add(new Ball(ball.getName(), new Vect(x,y), new Vect(v_x,v_y)));
        }
        else if(outLeftEdge && outBottomEdge){
            if(!left.equals("") && !bottom.equals("")) v_x=-v_x;
            else if (left.equals("")) v_x=-v_x;
            else if (bottom.equals("")) v_y=-v_y;
            newBalls.remove(ball);
            newBalls.add(new Ball(ball.getName(), new Vect(x,y), new Vect(v_x,v_y)));
        }
        else if(outRightEdge && outTopEdge) {
            if(!right.equals("") && !top.equals("")) v_x=-v_x;
            else if (right.equals("")) v_x=-v_x;
            else if (top.equals("")) v_y=-v_y;
            newBalls.remove(ball);
            newBalls.add(new Ball(ball.getName(), new Vect(x,y), new Vect(v_x,v_y)));
        }
        else if(outRightEdge && outBottomEdge) {
            if(!right.equals("") && !bottom.equals("")) v_x=-v_x;
            else if (right.equals("")) v_x=-v_x;
            else if (bottom.equals("")) v_y=-v_y;
            newBalls.remove(ball);
            newBalls.add(new Ball(ball.getName(), new Vect(x,y), new Vect(v_x,v_y)));
        }
        else if (outLeftEdge && !left.equals("")) {
            assert flingball.isPresent();
            this.transferBall(BOARD_DIRECTION.LEFT, ball);
            newBalls.remove(ball);
        }else if (outRightEdge&& !right.equals("")) {
            assert flingball.isPresent();
            this.transferBall(BOARD_DIRECTION.RIGHT, ball);
            newBalls.remove(ball);
        } else if (outBottomEdge && !bottom.equals("")) {
            assert flingball.isPresent();
            this.transferBall(BOARD_DIRECTION.BOTTOM, ball);
            newBalls.remove(ball);
        } else if (outTopEdge  && !top.equals(""))  {
            assert flingball.isPresent();
            this.transferBall(BOARD_DIRECTION.TOP, ball);
            newBalls.remove(ball);
        }
    }
    
    /* 
     * For the given Gadgets, update all of their target Absorbers (if any).
     * Add the balls shot by the Absorbers to newBalls.
     * Create new Absorbers with 1 less balls and add them to Absorbers List. 
     */
    private synchronized void updateActionedAbsorbersAndFlippers(Gadget gadget, List<Ball> newBalls) {
        // a gadget can trigger a absorber or a flipper, need to check if it triggers either
        List<Absorber> absorberTargets = getTargetAbsorbers(gadget);
        List<Flipper> flipperTargets = getTargetFlippers(gadget);
        for(Absorber target: absorberTargets) {
            if(absorberBallNamesMap.get(target).size()>0) {
                String ballName = absorberBallNamesMap.get(target).get(0);
                absorberBallNamesMap.get(target).remove(0);
                newBalls.add(target.action(ballName));
            }
        }
        for(Flipper target: flipperTargets) {
            Optional<Flipper> actualFlipper = this.findFlipper(target.getName());
            if (actualFlipper.isPresent()) {
                flippers.remove(actualFlipper.get());
                Flipper initializeMovement = initiateFlipperMovement(actualFlipper.get());
                flippers.add(initializeMovement);
            }
        }
        checkRep();
    }
    /*
     * For a given Ball, checks if it collided with walls. If so, returns a Ball with new velocity.
     * If there was no collision, returns a new ball with previous velocity.
     */
    private synchronized Ball getCollisionRedirectionWithWalls(Ball ball) {
        Vect velocity = ball.getVelocity();
        Vect curVelocity = velocity;
        for (LineSegment lineSegment: walls) {
            if (Physics.timeUntilWallCollision(lineSegment, ball.getCircle(), curVelocity) < TIME) {
                curVelocity = Physics.reflectWall(lineSegment, curVelocity);
            }
        }
        return new Ball(ball.getName(), ball.getLocation(), curVelocity);
    }
    
    /*
     * Updates a Ball's location and velocity to next half unit of time, taking into account 
     * gravity and frictions. Return the new ball.
     */
    private synchronized Ball updateBallWithGravityAndFrictionInHalfTime(Ball ball) {
        Vect location = ball.getLocation();
        Vect velocity = ball.getVelocity();
        // Vnew = Vold × ( 1 - mu × deltat - mu2 × |Vold| × deltat )
        final double deltaT=TIME/2;
        velocity = velocity.times(1 - friction1 * deltaT - friction2 * velocity.length() * deltaT);
        velocity = velocity.plus(new Vect(0, gravity*deltaT));
        checkRep();
        return new Ball(ball.getName(), location, velocity);
    }
    
    /*
     * Updates the Ball's location according to its current velocity.
     */
    private static Ball updateBallLocation(Ball ball) {
        Vect location=ball.getLocation();
        Vect newLocation = location.plus(ball.getVelocity().times(TIME));
        return new Ball(ball.getName(), newLocation, ball.getVelocity());
    }
    
    private static Flipper initiateFlipperMovement(Flipper flipper) {
        return new Flipper(flipper.getName(), flipper.getLocation(), flipper.getRotation(), flipper.getFlipperVariation(), flipper.getAngle(), flipper.isInitialPostion(), true);
    }
    
    private static Flipper updateFlipperMovement(Flipper flipper) {
        double angularVelocityPerUpdate = flipper.getAngularVelocityPerSecond() * Board.TIME;
        double radian = Math.toRadians(angularVelocityPerUpdate);
        Angle updateAngle = new Angle(radian);
        if (flipper.getFlipperVariation().equals(FlipperLeftOrRight.LEFT_FLIPPER)) {
            if (flipper.isInitialPostion()) {
                double currentAngle = flipper.getAngle().plus(Angle.DEG_270).radians();
                double finishedAngle = Angle.DEG_270.minus(Angle.DEG_90).radians();
                double radianDiff = currentAngle-finishedAngle;
                if (radianDiff < radian) {
                    // flipper need to stop moving 
                    return new Flipper(flipper.getName(), flipper.getLocation(), flipper.getRotation(), flipper.getFlipperVariation(), new Angle(0, -1), false, false);
                } else {
                    // flipper still needs to move 
                    return new Flipper(flipper.getName(), flipper.getLocation(), flipper.getRotation(), flipper.getFlipperVariation(), flipper.getAngle().minus(updateAngle), true, true);
                }
            } else {
                double currentAngle = flipper.getAngle().plus(Angle.DEG_180).radians();
                double finishedAngle = Angle.DEG_180.radians();
                double radianDiff = finishedAngle-currentAngle;
                if (radianDiff < radian) {
                    // flipper need to stop moving 
                    return new Flipper(flipper.getName(), flipper.getLocation(), flipper.getRotation(), flipper.getFlipperVariation(), Angle.ZERO, true, false);
                } else { 
                    // flipper still needs to move 
                    return new Flipper(flipper.getName(), flipper.getLocation(), flipper.getRotation(), flipper.getFlipperVariation(), flipper.getAngle().plus(updateAngle), false, true);
                } 
            }
        } else {
            if (flipper.isInitialPostion()) {
                double currentAngle = flipper.getAngle().radians();
                double finishedAngle = Angle.DEG_90.radians();
                double radianDiff = finishedAngle-currentAngle;
                if (radianDiff < radian) {
                    // flipper need to stop moving 
                    return new Flipper(flipper.getName(), flipper.getLocation(), flipper.getRotation(), flipper.getFlipperVariation(), Angle.DEG_90, false, false);
                } else {
                    // flipper still needs to move 
                    return new Flipper(flipper.getName(), flipper.getLocation(), flipper.getRotation(), flipper.getFlipperVariation(), flipper.getAngle().plus(updateAngle), true, true);
                }
            } else {
                double currentAngle = flipper.getAngle().radians();
                double finishedAngle = Angle.ZERO.radians();
                double radianDiff = currentAngle-finishedAngle;
                if (radianDiff < radian) {
                    // flipper need to stop moving 
                    return new Flipper(flipper.getName(), flipper.getLocation(), flipper.getRotation(), flipper.getFlipperVariation(), Angle.ZERO, true, false);
                } else {
                    // flipper still needs to move 
                    return new Flipper(flipper.getName(), flipper.getLocation(), flipper.getRotation(), flipper.getFlipperVariation(), flipper.getAngle().minus(updateAngle), false, true);
                } 
            }
            
        }
    }
    
    /**
     * Draws an image of all the parts of a Flingball game that do not
     * move. Since these are static, they do not need to be redrawn with
     * each frame, so they will be drawn once and set as a background.
     * 
     * @return the Board's background image where the background image is
     *         a BufferedImage with all Gadgets drawn and has a background
     *         color of white.
     */
    public synchronized BufferedImage drawBackground() {
        final BufferedImage background = 
                new BufferedImage(PIXEL_PER_L * L,PIXEL_PER_L * L, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = background.getGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, PIXEL_PER_L * L, PIXEL_PER_L * L);
        
        for (Gadget gadget : getStaticGadgets())         
            gadget.draw(graphics); 
        
        return background;
    }
    
    /**
     * Draws balls on a board.
     * 
     * @param graphics a Graphics object that represents the board to draw balls on
     */
    public synchronized void drawBalls(Graphics graphics) {
        for (Ball ball : balls)
            ball.draw(graphics);
    }
    
    /**
     * Draws joined boards' names.
     * @param graphics a Graphics object that represents the board to draw balls on
     */
    public synchronized void drawJoinedBoardName(Graphics graphics) {
        graphics.setColor(Color.gray);
        final double start=L/(double)4;
        final double range=L/2;
        if(!left.equals("")) {
            double y=start;
            double x=0.2;
            for(char c: left.toCharArray()) {
                graphics.drawString(c+"", (int)(x*PIXEL_PER_L), (int)(y*PIXEL_PER_L));
                if(left.length()!=1) y +=range/(left.length()-1);
            }
        }
        if(!right.equals("")) {
            double y=start;
            double x=L-0.6;
            for(char c: right.toCharArray()) {
                graphics.drawString(c+"", (int)(x*PIXEL_PER_L), (int)(y*PIXEL_PER_L));
                if(right.length()!=1) y += range/(right.length()-1);
            }
        }
        if(!top.equals("")) {
            double x=start;
            double y=0.6;
            for(char c: top.toCharArray()) {
                graphics.drawString(c+"", (int)(x*PIXEL_PER_L), (int)(y*PIXEL_PER_L));
                if(top.length()!=1) x += range/(top.length()-1);
            }
        }
        if(!bottom.equals("")) {
            double x=start;
            double y=L-0.2;
            for(char c: bottom.toCharArray()) {
                graphics.drawString(c+"", (int)(x*PIXEL_PER_L), (int)(y*PIXEL_PER_L));
                if(bottom.length()!=1) x += range/(bottom.length()-1);
            }
        }
    }
    
    public synchronized void drawFlippers(Graphics graphics) {
        for (Flipper flipper: flippers)
            flipper.draw(graphics);
    }

    @Override 
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Board: " + name + "\n");
        sb.append("--| Gravity: " + gravity + "\n");
        sb.append("--| Friction1: " + friction1 + "\n");
        sb.append("--| Friction2: " + friction2 + "\n");
        sb.append("--| Balls:\n");
        for (Ball ball : balls) { sb.append("-----| " + ball.toString()); }
        sb.append("--| staticBumpers:\n");
        for (Bumper bumper : staticBumpers) { sb.append("-----| " + bumper.toString()); }
        sb.append("--| Absorbers:\n");
        for (Absorber absorber : absorbers) { sb.append("-----| " + absorber.toString()); }
        sb.append("--| Flippers:\n");
        for (Flipper flipper:flippers) { sb.append("-----| " + flipper.toString()); }
        sb.append("--| Portals:\n");
        for (Portal portal:portals) { sb.append("-----| " + portal.toString()); }
        sb.append("\n--| fire:\n");
        for (Gadget g:triggerTargetAbsorberMap.keySet()) { sb.append("-----| " + g.getName()+" triggers "+ triggerTargetAbsorberMap.get(g).stream().map(Gadget::getName).collect(Collectors.toList()).toString()); }
        for (Gadget g:triggerTargetFlipperMap.keySet()) { sb.append("-----| " + g.getName()+" triggers "+triggerTargetFlipperMap.get(g).stream().map(Gadget::getName).collect(Collectors.toList()).toString()); }
        sb.append("\n--| keyup:\n");
        for (int key:keyupTriggerAndAbsorberMap.keySet()) { sb.append("-----| " + KeyEvent.getKeyText(key)+" triggers "+ keyupTriggerAndAbsorberMap.get(key).stream().map(Gadget::getName).collect(Collectors.toList()).toString()); }
        for (int key:keyupTriggerAndFlipperMap.keySet()) { sb.append("-----| " + KeyEvent.getKeyText(key)+" triggers "+  keyupTriggerAndFlipperMap.get(key).stream().map(Gadget::getName).collect(Collectors.toList()).toString()); }
        sb.append("\n--| keydown:\n");
        for (int key:keydownTriggerAndAbsorberMap.keySet()) { sb.append("-----| "+ KeyEvent.getKeyText(key)+" triggers "+  keydownTriggerAndAbsorberMap.get(key).stream().map(Gadget::getName).collect(Collectors.toList()).toString()); }
        for (int key:keydownTriggerAndFlipperMap.keySet()) { sb.append("-----| "+ KeyEvent.getKeyText(key)+" triggers "+  keydownTriggerAndFlipperMap.get(key).stream().map(Gadget::getName).collect(Collectors.toList()).toString()); }
        return sb.toString();
    }

}
