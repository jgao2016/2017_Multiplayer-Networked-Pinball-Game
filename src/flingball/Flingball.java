package flingball;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import edu.mit.eecs.parserlib.UnableToParseException;

/**
 * Flingball is class that can play flingball.
 * It has a client mode in which it serves as a client that sends requests to
 * the FlingballServer to play, and it can play together with other clients.
 * Flingball is not a thread-safe data type, but its use of multiply 
 * inner threads is safe.
 * ***************************
 * After connecting to a FlingballServer, Flingball can also serve as a "server"
 * to receive requests from the FlingballServer. However, it can only have one
 * "client", which must be the FlingballServer that it is connecting to.
 * 
 * It accepts requests of the form:
 * REQUEST ::= JOIN_REQUEST |DISJOIN_REQUEST | TRANSFER_BALL_REQUEST | PORTAL_BALL_REQUEST
 * JOIN_REQUEST ::= "join" DIRECTION BOARDNAME NEWLINE
 * DISJOIN_REQUEST ::= "disjoin" BOARDNAME NEWLINE
 * TRANSFER_BALL_REQUEST ::= "tran" LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME NEWLINE
 * PORTAL_BALL_REQUEST::= "port" PORTALNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME "from" BOARDNAME NEWLINE
 * 
 * RESPONSE ::= "succeed:"|"fail:" MESSAGE NEWLINE
 * 
 * BOARDNAME::= NAME
 * PORTALNAME::= NAME
 * BALLNAME::= [A-Za-z_][A-Za-z_0-9]*
 * LOCA_X::=FLOAT
 * LOCA_Y::=FLOAT
 * VELO_X::=FLOAT
 * VELO_Y::=FLOAT
 * NAME::=[A-Za-z_][A-Za-z_0-9]*
 * FLOAT::= '-'?([0-9]+'.'?[0-9]*|'.'[0-9]+);
 * DIRECTION::='left'|'right'|'top'|'bottom'
 * MESSAGE:: [^\n\r]*
 * NEWLINE ::= "\n" | "\r" "\n"?
 * 
 * After getting a TRANSFER_BALL_REQUEST, Flingball will create a new ball with 
 * given info in this Flingball's game.
 * 
 * After getting a PORTAL_BALL_REQUEST, Flingball will create a new ball with given 
 * info in this Flingball's game. requires that PORTALNAME exists in this board.
 * 
 * After getting a JOIN_REQUEST, Flingball will join its board with given board.
 * 
 * After getting a DISJOIN_REQUEST, Flingball will try to disjoin this board with given board,
 * and if this board was not joined with given board, it will have no influence.
 * 
 * Besides the format, also requires that the location and velocity are valid for balls.
 * 
 * If the request doesn't follow the protocol, Flingball will return an error message and
 * do nothing else.
 */
public class Flingball {
    private final Socket socket;
    private final Board board;
    private ServerSocket serverSocket;
    private int serverPort;
    private final BufferedReader in;
    private final PrintWriter out;
    public final static int PORT_START_NUMBER = 2000;
    private static final int PORT = 10987;
    private static String defaultGame="boards/default.fb";
    
    // Abstraction function:
    //  AF(socket, serverSocket,serverPort,in,out,board)=
    //     the server with socket socket to send request to FlingballServer,
    //     serverSocket serverSocket to receive request from FlingballServer,
    //     BufferedReader in and PrintWriter out, and board board.
    // Representation invariant:
    //    1. socket, in, out, board not null 
    //    2. if it succeeded to connect with FlingballServer, then 
    //       serverSocket,serverPort,in,out are not null;
    // Safety from rep exposure:
    //    1. all fields except ServerSocket and serverPort are private and final.
    //    2. ServerSocket and serverPort are private, ServerSocket is never changed 
    //       after initialization and serverPort's type is immutable.
    //    3. fields are never returned.
    // Thread safety argument:
    //   Flingball is not a thread-safe data type, but its use of multiply 
    //   inner threads is safe.
    //    1. all fields private, most of them final. No rep exposure.
    //    2. all instance methods used synchronized keyword except serverServe and handleConnection.
    //    3. serverServe only touches the field serverSocket, and serverSocket is confined 
    //       in this one thread only.
    //    4. handleConnection doesn't touch the rep of this, and it calls handleConnection 
    //       which is synchronized on "this".
    /**
     * Usage:
     * Flingball [--host HOST] [--port PORT] [FILE].
     * HOST is an optional hostname or IP address of the server to connect to. 
     * If no HOST is provided, then the client runs in single-machine play mode,
     * running a board and allowing the user to play it without any network 
     * connection to any other board.
     * PORT is an optional integer in the range 0 to 65535 inclusive, specifying
     * the port where the server is listening for incoming connections. 
     * The default port is 10987.
     * FILE is an optional argument specifying a file pathname of the Flingball 
     * board that this client should run. If FILE is not provided, then the 
     * Flingball client should run the default benchmark board as described in
     * the phase 1 specification.
     * e.g. Flingball --host localhost --port 10987 boards/default.fb
     */
    public static void main(String[] args){
        
        String file=defaultGame;
        try {
            if(args.length % 2 == 0){
                file=args[args.length-1];
            }
            if(args.length <= 2 || !args[1].equals("--host")) {
                playBoardFile(file);
            }else {
                try {
                    String host=args[2];
                    int port=PORT;
                    if(args.length >= 4) {
                        port=Integer.parseInt(args[4]);
                    }
                    Board board = playBoardFile(file);
                    Flingball client = new Flingball(host, port, board);
                    client.sendConnectRequest(client.board.getName());

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }catch (UnableToParseException e) {
            System.err.println("error: unable to parse, invalid file");
        }catch (IOException e) {
            System.err.println("error: unable to read file");
        }
    }
    /**
     * Make a Flingball and connect it to a server running on
     * hostname at the specified port.
     * @param hostname host name
     * @param port port
     * @param board board
     * @throws IOException if can't connect
     */
    public Flingball(String hostname, int port, Board board) throws IOException {
        socket = new Socket(hostname, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.board=board;
        board.setFlingball(Optional.of(this));
        checkRep();
    }
    //checkrep
    private synchronized void checkRep() {
        assert board!=null;
        assert socket!=null;
        assert in!=null;
        assert out!=null;
    }
    /*
     * start the serverSocket
     */
    private void serverServe() throws IOException {
        Thread thread = new Thread(() ->  {
            try {
                while (true) {
                    // block until a client connects
                    while(!serverSocket.isClosed()) {
                        Socket flingballServerSocket = serverSocket.accept();
                        while(!flingballServerSocket.isClosed()) {
                            try {
                                handleConnection(flingballServerSocket);
                                checkRep();
                            } catch (IOException ioe) {
                                ioe.printStackTrace(); 
                            } 
                        }
                    }
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
        thread.start();
        
    }
    /**
     * Handle a single client connection.
     * Returns when the client disconnects.
     * 
     * @param socket socket connected to client
     * @throws IOException if the connection encounters an error or closes unexpectedly
     */
    private void handleConnection(Socket flingballServerSocket) throws IOException {
        BufferedReader flingballServerIn = new BufferedReader(new InputStreamReader(flingballServerSocket.getInputStream()));
        PrintWriter flingballServerOut = new PrintWriter(flingballServerSocket.getOutputStream(), true);
        try{
            for (String input = flingballServerIn.readLine(); input != null; input = flingballServerIn.readLine()) {
                String output = handleRequest(input);
                flingballServerOut.println(output);
            }
        }finally {
            flingballServerOut.close();
            flingballServerIn.close();
        }
    }
    /**
     * Handle a single client request and return the server response.
     * @param input message from client
     * @return output message to client
     * @throws IOException if network or server failure
     */
    private synchronized String handleRequest(String input) throws IOException  {
        String[] tokens = input.split(" ");
        // handles TRANSFER_BALL_REQUEST ::= "tran" LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME
        if (tokens[0].equals("tran") && tokens.length == 6) {
            final int beginIndex=5;
            board.receiveBall(input.substring(beginIndex));
            return "succeed: ball received";
        // handles PORTAL_BALL_REQUEST::= "port" PORTALNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME "from" BOARDNAME 
        }else if (tokens[0].equals("port") && tokens.length == 9) {
            String portalName=tokens[1];
            assert board.hasPortal(portalName);
            Portal portal=board.getPortal(portalName);
            String ballInfo=portal.getCenter().x()+" " + portal.getCenter().y()
                    +" "+tokens[4]+" "+tokens[5]+" " +tokens[6];
            board.receiveBall(ballInfo);
            return "succeed: portal ball received";
        // handles JOIN_REQUEST ::= "join" DIRECTION
        }else if (tokens[0].equals("join") && tokens.length == 3) {
            String otherBoard=tokens[2];
            switch (tokens[1]) {
            case "left":
                board.joinBoard(otherBoard,BOARD_DIRECTION.LEFT);
                break;
            case "right":
                board.joinBoard(otherBoard,BOARD_DIRECTION.RIGHT);
                break;
            case "top":
                board.joinBoard(otherBoard,BOARD_DIRECTION.TOP);
                break;
            case "bottom":
                board.joinBoard(otherBoard,BOARD_DIRECTION.BOTTOM);
                break;
            default:
                System.err.println("CLIENT "+board.getName()+" should never reach here");
                break;
            }
            return "succeed: boards joined";
        // handles DISJOIN_REQUEST ::= "disjoin" BOARDNAME
        }else if (tokens[0].equals("disjoin")) {
            String otherBoard=tokens[1];
            board.disjoinBoard(otherBoard);
            return "succeed: disjoined board";
        }else {
            return "fail: illegal command";
        }
    }
    /*
     * create a port number according to userID
     */
    private static int createPortWithUserID(int userID) {
        return userID + PORT_START_NUMBER;
    }
    
    /**
     * Send a connect request to the server. Requires this is "open".
     * @param boardName boardName
     * @throws IOException if network or server failure
     * @returns reply
     */
    public synchronized String sendConnectRequest(String boardName)throws IOException {
        StringBuffer portals=new StringBuffer();
        for(Portal portal:board.getPortals()) {
            portals.append(" "+portal.getName());
        }
        out.print("connect "+ boardName + portals+ "\n");
        out.flush(); // important! 
        String reply=getReply();
        if (reply.split(" ")[0].equals("succeed:")) {
            int userID=Integer.parseInt(reply.split(" ")[1]);
            serverPort = createPortWithUserID(userID);
            serverSocket=new ServerSocket(serverPort);
            serverServe();
            sendServerCreatedRequest();
        }else {
            System.err.println("connect request failed");
            socket.close();
        }
        return reply;
    }
    /**
     * Send a "server created" request to the server. Requires this is "open".
     * @throws IOException if network or server failure
     * @returns reply
     */
    public synchronized String sendServerCreatedRequest() throws IOException {
        out.print("server created" + "\n");
        out.flush(); // important! 
        return getReply();
    }
    /**
     * Send a ball request to the server. Requires this is "open".
     * @param ballRequest ballRequest
     * @throws IOException if network or server failure
     * @returns reply
     */
    public synchronized String sendBallRequest(String ballRequest) throws IOException {
        out.print(ballRequest + "\n");
        out.flush(); // important! 
        return getReply();
    }
    /**
     * Send a quit request to the server and close socket and serverSocket.
     * Requires this socket is "open".
     * @throws IOException if network or server failure
     */
    public synchronized void sendQuitRequest() throws IOException {
        out.print("quit" + "\n");
        out.flush(); // important! 
        serverSocket.close();
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     * @return square of requested number
     * @throws IOException if network or server failure
     */
    public synchronized String getReply() throws IOException {
        String reply = in.readLine();
        if (reply == null) {
            System.err.println("reply is null!");
            throw new IOException("connection terminated unexpectedly");
        }
        return reply;
    }

    /**
     * Closes the client's connection to the server.
     * This client is now "closed". Requires this is "open".
     * @throws IOException if close fails
     */
    public synchronized void close() throws IOException {
        in.close();
        out.close();
        socket.close();
        serverSocket.close();
    }
    /**
     * Read a file and return the string of this file
     * @param boardFile file 
     * @return the string of this file
     * @throws UnableToParseException exception
     * @throws IOException exception
     */
    public static String boardFileToString(File boardFile) throws UnableToParseException, IOException {
        List<String> sampleBoard = new ArrayList<>();
        sampleBoard = Files.readAllLines(boardFile.toPath());
        StringBuilder builder = new StringBuilder();
        for (String line : sampleBoard)
            builder.append(line + "\n");
        return builder.toString();
    }
    /*
     * play board file with given file
     */
    private static Board playBoardFile(String boardFile) throws UnableToParseException, IOException {
        //Board flingBall = BoardParser.parse(boardFileToString(new File("boards/multiplayer_left.fb")));
        Board flingBall = BoardParser.parse(boardFileToString(new File(boardFile)));
        Simulator simulator = new Simulator(flingBall);
        simulator.playFlingball();
        return flingBall;
    }
    @Override 
    public synchronized String toString() {
        return "Flingball with board "+board.getName();
    }
}
