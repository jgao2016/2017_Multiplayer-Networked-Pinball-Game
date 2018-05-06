package flingball;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;
import physics.Angle;
import physics.Vect;

/** Handles parsing of expressions
 * 
 *  @author esegler: implementation
 */
public class BoardParser {

    // The nonterminals of the Board grammar
    private enum BoardGrammar {
        BOARD, 
        BALL, 
        SQUAREBUMPER, 
        CIRCLEBUMPER, 
        TRIANGLEBUMPER, 
        ABSORBER, 
        FIRE, 
        GADGET, 
        ORIENTATION, 
        INTEGER, 
        FLOAT, 
        NAME, 
        WHITESPACE, 
        COMMENTS, 
        WHITESPACEANDCOMMENTS, 
        GRAVITY, 
        FRICTION1, 
        FRICTION2, 
        TRIGGER, 
        ACTION,
        RIGHTFLIPPER,
        LEFTFLIPPER,
        PORTAL,
        BOARDNAME,
        KEYUP,
        KEYDOWN,
        KEY,
    }

    private static Parser<BoardGrammar> parser = makeParser();
    private final static Map<String,Integer> keyName;
    static {
        Map<String,Integer> map = new HashMap<>();
        map.put("a",KeyEvent.VK_A);
        map.put("b",KeyEvent.VK_B);
        map.put("c",KeyEvent.VK_C);
        map.put("d",KeyEvent.VK_D);
        map.put("e",KeyEvent.VK_E);
        map.put("f",KeyEvent.VK_F);
        map.put("g",KeyEvent.VK_G);
        map.put("h",KeyEvent.VK_H);
        map.put("i",KeyEvent.VK_I);
        map.put("j",KeyEvent.VK_J);
        map.put("k",KeyEvent.VK_K);
        map.put("l",KeyEvent.VK_L);
        map.put("m",KeyEvent.VK_M);
        map.put("n",KeyEvent.VK_N);
        map.put("o",KeyEvent.VK_O);
        map.put("p",KeyEvent.VK_P);
        map.put("q",KeyEvent.VK_Q);
        map.put("r",KeyEvent.VK_R);
        map.put("s",KeyEvent.VK_S);
        map.put("t",KeyEvent.VK_T);
        map.put("u",KeyEvent.VK_U);
        map.put("v",KeyEvent.VK_V);
        map.put("w",KeyEvent.VK_W);
        map.put("x",KeyEvent.VK_X);
        map.put("y",KeyEvent.VK_Y);
        map.put("z",KeyEvent.VK_Z);
        map.put("0",KeyEvent.VK_0);
        map.put("1",KeyEvent.VK_1);
        map.put("2",KeyEvent.VK_2);
        map.put("3",KeyEvent.VK_3);
        map.put("4",KeyEvent.VK_4);
        map.put("5",KeyEvent.VK_5);
        map.put("6",KeyEvent.VK_6);
        map.put("7",KeyEvent.VK_7);
        map.put("8",KeyEvent.VK_8);
        map.put("9",KeyEvent.VK_9);
        map.put("shift",KeyEvent.VK_SHIFT);
        map.put("ctrl",KeyEvent.VK_CONTROL);
        map.put("alt",KeyEvent.VK_ALT);
        map.put("meta",KeyEvent.VK_META);
        map.put("space",KeyEvent.VK_SPACE);
        map.put("left",KeyEvent.VK_LEFT);
        map.put("right",KeyEvent.VK_RIGHT);
        map.put("up",KeyEvent.VK_UP);
        map.put("down",KeyEvent.VK_DOWN);
        map.put("minus",KeyEvent.VK_MINUS);
        map.put("equals",KeyEvent.VK_EQUALS);
        map.put("backspace",KeyEvent.VK_BACK_SPACE);
        map.put("openbracket",KeyEvent.VK_OPEN_BRACKET);
        map.put("closebracket",KeyEvent.VK_CLOSE_BRACKET);
        map.put("backslash",KeyEvent.VK_BACK_SLASH);
        map.put("semicolon",KeyEvent.VK_SEMICOLON);
        map.put("quote",KeyEvent.VK_QUOTE);
        map.put("enter",KeyEvent.VK_ENTER);
        map.put("comma",KeyEvent.VK_COMMA );
        map.put("period",KeyEvent.VK_PERIOD);
        map.put("slash",KeyEvent.VK_SLASH);
        keyName = Collections.unmodifiableMap(map);
    }
    /**
     * Compile the grammar into a parser.
     * 
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has syntax errors
     */
    private static Parser<BoardGrammar> makeParser() throws RuntimeException {
        try {
            InputStream grammarStream = Flingball.class.getResourceAsStream("Board.g");
            return Parser.compile(grammarStream, BoardGrammar.BOARD);
            
        } catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("the grammar has a syntax error", e);
        }
    }

    /**
     * Parse a string into a Board.
     * 
     * @param string string to parse
     * @param otherBoards the other boards currently connecting to server
     * @return Board parsed from the string
     * @throws UnableToParseException if the string doesn't match the Board grammar
     */
    public static Board parse(final String string, List<Board> otherBoards) throws UnableToParseException {
        final ParseTree<BoardGrammar> parseTree = parser.parse(string);
        // make an AST from the parse tree
        final Board board = makeAbstractSyntaxTree(parseTree);
        return board;
    }
    /**
     * Parse a string into a Board.
     * 
     * @param string string to parse
     * @return Board parsed from the string
     * @throws UnableToParseException if the string doesn't match the Board grammar
     */
    public static Board parse(final String string) throws UnableToParseException {
        final ParseTree<BoardGrammar> parseTree = parser.parse(string);
        // make an AST from the parse tree
        final Board board = makeAbstractSyntaxTree(parseTree);
        return board;
    }

    /**
     * Convert a parse tree into an abstract syntax tree.
     * 
     * @param parseTree constructed according to the grammar in Board.g
     * @param otherBoards the other boards currently connecting to server
     * @return abstract syntax tree corresponding to parseTree
     */
    private static Board makeAbstractSyntaxTree(final ParseTree<BoardGrammar> parseTree) {
        List<Gadget> gadgets = new ArrayList<>();
        // get all the parsings of the different objects
        String name = parseTree.childrenByName(BoardGrammar.NAME).get(0).text();

        // should just be empty or have one element
        List<ParseTree<BoardGrammar>> gravityList = parseTree.childrenByName(BoardGrammar.GRAVITY); 
        List<ParseTree<BoardGrammar>> friction1List = parseTree.childrenByName(BoardGrammar.FRICTION1);
        List<ParseTree<BoardGrammar>> friction2List = parseTree.childrenByName(BoardGrammar.FRICTION2);

        Float gravity = 25F; // default value of gravity
        Float friction1 = .025F; // default value of friction
        Float friction2 = .025F;

        if (!gravityList.isEmpty()) {
            gravity = Float.parseFloat(gravityList.get(0).text());
        }
        if (!friction1List.isEmpty()) {
            friction1 = Float.parseFloat(friction1List.get(0).text());
        }
        if (!friction2List.isEmpty()) {
            friction2 = Float.parseFloat(friction2List.get(0).text());
        }
        // make board
        Board board = new Board(name, gravity, friction1, friction2);
        // all the balls defined
        List<ParseTree<BoardGrammar>> gadgetList = parseTree.childrenByName(BoardGrammar.GADGET);
        // source portals and other portal's names
//        Map<Portal, String> sourcePortalAndOtherPortals=new HashMap<>();
//        Map<Portal, Board> sourcePortalAndOtherBoards=new HashMap<>();
        // in the file
        for (ParseTree<BoardGrammar> parseGadget : gadgetList) {
            // parse all Balls
            for (ParseTree<BoardGrammar> parse : parseGadget.childrenByName(BoardGrammar.BALL)) {
                Ball ball = makeBall(parse);
                board.addBall(ball);
            }
            // parse all SquareBumper
            for (ParseTree<BoardGrammar> parse : parseGadget.childrenByName(BoardGrammar.SQUAREBUMPER)) {
                SquareBumper bumper = makeSquareBumper(parse);
                gadgets.add(bumper);
                board.addBumper(bumper);
            }
            // parse all CircleBumper
            for (ParseTree<BoardGrammar> parse : parseGadget.childrenByName(BoardGrammar.CIRCLEBUMPER)) {
                CircleBumper bumper = makeCircleBumper(parse);
                gadgets.add(bumper);
                board.addBumper(bumper);
            }
            // parse all TriangleBumper
            for (ParseTree<BoardGrammar> parse : parseGadget.childrenByName(BoardGrammar.TRIANGLEBUMPER)) {
                TriangleBumper bumper = makeTriangleBumper(parse);
                gadgets.add(bumper);
                board.addBumper(bumper);
            }
            // parse all Absorber
            for (ParseTree<BoardGrammar> parse : parseGadget.childrenByName(BoardGrammar.ABSORBER)) {
                Absorber abs = makeAbsorber(parse);
                gadgets.add(abs);
                board.addAbsorber(abs);
            }
            // parse all leftFlippers
            for (ParseTree<BoardGrammar> parse : parseGadget.childrenByName(BoardGrammar.LEFTFLIPPER)) {
                Flipper flipper = makeFlipper(parse, true);
                gadgets.add(flipper);
                board.addFlipper(flipper);
            }
            // parse all rightFlippers
            for (ParseTree<BoardGrammar> parse : parseGadget.childrenByName(BoardGrammar.RIGHTFLIPPER)) {
                Flipper flipper = makeFlipper(parse, false);
                gadgets.add(flipper);
                board.addFlipper(flipper);
            }
            // parse all portals
            for (ParseTree<BoardGrammar> parse : parseGadget.childrenByName(BoardGrammar.PORTAL)) {
                Portal portal = makePortal(parse,name,board);
                gadgets.add(portal);
                board.addPortal(portal);
            }
            // parse all Fire
            for (ParseTree<BoardGrammar> fire : parseGadget.childrenByName(BoardGrammar.FIRE)) {
                // get the name of gadget that serves as the trigger
                List<ParseTree<BoardGrammar>> triggerGadget = fire.childrenByName(BoardGrammar.TRIGGER);
                // get name of absorber that is triggered
                List<ParseTree<BoardGrammar>> actionGadget = fire.childrenByName(BoardGrammar.ACTION);
                System.err.println("fire= "+fire);
                String triggerName = triggerGadget.get(0).text();
                String actionName = actionGadget.get(0).text();
                System.err.println("actionName= "+actionName);
                Gadget trigger = null; // empty value to compare against to make sure they were set
                for (Gadget g : gadgets) {
                    // match the trigger to the right gadget
                    if (g.getName().equals(triggerName)) {
                        trigger = g;
                    }
                }
                if(trigger==null) {
                    throw new RuntimeException("no gadget with name "+triggerName);
                }
                for(Absorber abs: board.getAbsorbers()) {
                    // match the action to the right gadget
                    if (abs.getName().equals(actionName)) {
                        System.err.println("found action target-absorber");
                        board.setTarget(abs, trigger);
                    }
                }
                for(Flipper flipper: board.getFlippers()) {
                    // match the action to the right gadget
                    if (flipper.getName().equals(actionName)) {
                        System.err.println("found action target-flipper");
                        board.setTarget(flipper, trigger);
                    }
                }
            }
            // parse all keyups
            for (ParseTree<BoardGrammar> keyup : parseGadget.childrenByName(BoardGrammar.KEYUP)) {
                // get the key that serves as trigger
                List<ParseTree<BoardGrammar>> triggerGadget = keyup.childrenByName(BoardGrammar.KEY);
                // get name of absorber that is triggered
                List<ParseTree<BoardGrammar>> actionGadget = keyup.childrenByName(BoardGrammar.ACTION);
                System.err.println("keyup= "+keyup);
                String keyString = triggerGadget.get(0).text();
                String actionName = actionGadget.get(0).text();
                System.err.println("actionName= "+actionName);
                if(!keyName.containsKey(keyString)) {
                    throw new RuntimeException("illegal key");
                }
                int keyCode = keyName.get(keyString);
                for(Absorber abs: board.getAbsorbers()) {
                    // match the action to the right gadget
                    if (abs.getName().equals(actionName)) {
                        System.err.println("found action target-absorber");
                        board.setTarget(abs, keyCode, true);
                    }
                }
                for(Flipper flipper: board.getFlippers()) {
                    // match the action to the right gadget
                    if (flipper.getName().equals(actionName)) {
                        System.err.println("found action target-flipper");
                        board.setTarget(flipper, keyCode,true);
                    }
                }
            }
            // parse all keydowns
            for (ParseTree<BoardGrammar> keydown : parseGadget.childrenByName(BoardGrammar.KEYDOWN)) {
                // get the key that serves as trigger
                List<ParseTree<BoardGrammar>> triggerGadget = keydown.childrenByName(BoardGrammar.KEY);
                // get name of absorber that is triggered
                List<ParseTree<BoardGrammar>> actionGadget = keydown.childrenByName(BoardGrammar.ACTION);
                System.err.println("keydown= "+keydown);
                String keyString = triggerGadget.get(0).text();
                String actionName = actionGadget.get(0).text();
                if(!keyName.containsKey(keyString)) {
                    throw new RuntimeException("illegal key");
                }
                int keyCode = keyName.get(keyString);
                for(Absorber abs: board.getAbsorbers()) {
                    // match the action to the right gadget
                    if (abs.getName().equals(actionName)) {
                        System.err.println("found action target-absorber");
                        board.setTarget(abs, keyCode, false);
                    }
                }
                for(Flipper flipper: board.getFlippers()) {
                    // match the action to the right gadget
                    if (flipper.getName().equals(actionName)) {
                        System.err.println("found action target-flipper");
                        board.setTarget(flipper, keyCode,false);
                    }
                }
            }
        }
        //System.err.println(board);
        return board;
    }

    /*
     * Helper method to make Balls from a ball ParseTree
     */
    private static Ball makeBall(ParseTree<BoardGrammar> parsedBall) {
        String name = parsedBall.childrenByName(BoardGrammar.NAME).get(0).text(); // should only have one name, so index 0
        // will have in order xCoord, yCoord, xVelocity, yVelocity
        List<ParseTree<BoardGrammar>> floatList = parsedBall.childrenByName(BoardGrammar.FLOAT);
        //index positions of the variables
        final int xPos = 0;
        final int yPos = 1;
        final int xVelPos =2;
        final int yVelPos = 3;
        
        Float xCoord = Float.parseFloat(floatList.get(xPos).text());
        Float yCoord = Float.parseFloat(floatList.get(yPos).text());
        Float xVel = Float.parseFloat(floatList.get(xVelPos).text());
        Float yVel = Float.parseFloat(floatList.get(yVelPos).text());
        Vect position = new Vect(xCoord, yCoord);
        Vect velocity = new Vect(xVel, yVel);
        Ball ball = new Ball(name, position, velocity);
        return ball;
    }

    /*
     * Helper method to make SquareBumpers from a bumper ParseTree
     */
    private static SquareBumper makeSquareBumper(ParseTree<BoardGrammar> parsedBumper) {
        String name = parsedBumper.childrenByName(BoardGrammar.NAME).get(0).text();
        List<ParseTree<BoardGrammar>> integerList = parsedBumper.childrenByName(BoardGrammar.INTEGER);

        int x = Integer.parseInt(integerList.get(0).text()); // 2 elt list, x appears first
        int y = Integer.parseInt(integerList.get(1).text()); // 2 elt list, y appears second
        Vect position = new Vect(x, y);
        SquareBumper bumper = new SquareBumper(name, position);
        return bumper;
    }

    /*
     * Helper method to make CircleBumpers from a bumper ParseTree
     */
    private static CircleBumper makeCircleBumper(ParseTree<BoardGrammar> parsedBumper) {
        String name = parsedBumper.childrenByName(BoardGrammar.NAME).get(0).text();
        List<ParseTree<BoardGrammar>> integerList = parsedBumper.childrenByName(BoardGrammar.INTEGER);
        
        int x = Integer.parseInt(integerList.get(0).text()); // 2 elt list, x appears first
        int y = Integer.parseInt(integerList.get(1).text()); // 2 elt list, y appears second
        Vect position = new Vect(x, y);
        CircleBumper bumper = new CircleBumper(name, position);
        return bumper;
    }
    
    /*
     * Helper method to make CircleBumpers from a bumper ParseTree
     */
    private static Portal makePortal(ParseTree<BoardGrammar> parsedPortal,String thisBoardName,Board thisBoard) {
        String portalName = parsedPortal.childrenByName(BoardGrammar.NAME).get(0).text();
        String otherPortalName = parsedPortal.childrenByName(BoardGrammar.NAME).get(1).text();
        List<ParseTree<BoardGrammar>> integerList = parsedPortal.childrenByName(BoardGrammar.INTEGER);
        int x = Integer.parseInt(integerList.get(0).text()); // 2 elt list, x appears first
        int y = Integer.parseInt(integerList.get(1).text()); // 2 elt list, y appears second
        String toBoardName=thisBoardName;
        if(parsedPortal.childrenByName(BoardGrammar.BOARDNAME).size()!=0) {
            toBoardName=parsedPortal.childrenByName(BoardGrammar.BOARDNAME).get(0).text();
        }
        Portal portal = new Portal(portalName, otherPortalName, toBoardName, new Vect(x,y));
        return portal;
    }
    /*
     * Helper method to make TriangleBumpers from a bumper ParseTree
     */
    private static TriangleBumper makeTriangleBumper(ParseTree<BoardGrammar> parsedBumper) {
        String name = parsedBumper.childrenByName(BoardGrammar.NAME).get(0).text();
        List<ParseTree<BoardGrammar>> integerList = parsedBumper.childrenByName(BoardGrammar.INTEGER);

        int x = Integer.parseInt(integerList.get(0).text()); // 2 elt list, x appears first
        int y = Integer.parseInt(integerList.get(1).text()); // 2 elt list, y appears second
        Vect position = new Vect(x, y);

        // orientation
        Angle orientation = new Angle(0); // default orientation
        List<ParseTree<BoardGrammar>> orientationList = parsedBumper.childrenByName(BoardGrammar.ORIENTATION);
        if (!orientationList.isEmpty()) {
            int angle = Integer.parseInt(orientationList.get(0).text());
            if (angle == 90) {  // can't use compare function because it is node
                orientation = Angle.DEG_90;
            }
            else if (angle == 180) {
                orientation = Angle.DEG_180;
            }
            else if (angle == 270) {
                orientation = Angle.DEG_270;
            }
        }
        TriangleBumper bumper = new TriangleBumper(name, position, orientation);
        return bumper;
    }
    
    /*
     * Helper method to make Flippers from a flipper ParseTree
     */
    private static Flipper makeFlipper(ParseTree<BoardGrammar> parsedFlipper, boolean isLeftFlipper) {
        String name = parsedFlipper.childrenByName(BoardGrammar.NAME).get(0).text();
        List<ParseTree<BoardGrammar>> integerList = parsedFlipper.childrenByName(BoardGrammar.INTEGER);

        int x = Integer.parseInt(integerList.get(0).text()); // 2 elt list, x appears first
        int y = Integer.parseInt(integerList.get(1).text()); // 2 elt list, y appears second
        Vect position = new Vect(x, y);

        // orientation
        Angle orientation = new Angle(0); // default orientation
        List<ParseTree<BoardGrammar>> orientationList = parsedFlipper.childrenByName(BoardGrammar.ORIENTATION);
        if (!orientationList.isEmpty()) {
            int angle = Integer.parseInt(orientationList.get(0).text());
            if (angle == 90) {  // can't use compare function because it is node
                orientation = Angle.DEG_90;
            }
            else if (angle == 180) {
                orientation = Angle.DEG_180;
            }
            else if (angle == 270) {
                orientation = Angle.DEG_270;
            }
        }
        Flipper flipper;
        if(isLeftFlipper)  flipper = new Flipper(name,position, orientation,FlipperLeftOrRight.LEFT_FLIPPER);
        else  flipper = new Flipper(name,position, orientation, FlipperLeftOrRight.RIGHT_FLIPPER);
        return flipper;
    }


    /*
     * Helper method to make Absorbers from an absorber ParseTree
     */
    private static Absorber makeAbsorber(ParseTree<BoardGrammar> parsedAbs) {
        String name = parsedAbs.childrenByName(BoardGrammar.NAME).get(0).text();
        // order: x, y, width, height
        List<ParseTree<BoardGrammar>> integerList = parsedAbs.childrenByName(BoardGrammar.INTEGER);
        
        final int widthPos = 2;
        final int heightPos=3;
        int x = Integer.parseInt(integerList.get(0).text());
        int y = Integer.parseInt(integerList.get(1).text());
        int width = Integer.parseInt(integerList.get(widthPos).text());
        int height = Integer.parseInt(integerList.get(heightPos).text());
        
        Vect size = new Vect(width, height);
        Vect position = new Vect(x, y);
        Absorber abs = new Absorber(name, position, size);

        return abs;
    }

}
