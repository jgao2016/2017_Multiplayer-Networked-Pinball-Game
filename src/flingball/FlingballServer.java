package flingball;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FlingballServer is a server that deals with the requests of the 
 * clients of the flingball game.
 * Multiple clients (Flingball object) can play together with a FlingballServer.
 * FlingballServer itself is not a thread-safe data type, but its use of multiply 
 * inner threads is safe, and it can serve multiple clients at the same time.
 * ***************************
 * It accepts requests of the form:
 * 
 * REQUEST ::= CONNECT_REQUEST | QUIT_REQUEST | SERVER_REQUEST | TRANSFER_BALL_REQUEST | PORTAL_BALL_REQUEST
 * CONNECT_REQUEST ::= "connect" BOARDNAME PORTALNAME* NEWLINE
 * QUIT_REQUEST ::= "quit" NEWLINE
 * SERVER_REQUEST::= "server created"
 * TRANSFER_BALL_REQUEST ::= "tran" BOARDNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME NEWLINE
 * PORTAL_BALL_REQUEST::= "port" BOARDNAME PORTALNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME "from" BOARDNAME NEWLINE
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
 * The first time a client connects to the server, it must begin with a CONNECT_REQUEST
 * 
 * If request is a CONNECT_REQUEST, the FlingballServer will include this client into the flingball game
 * If succeed, the FlingballServer will send a reply containing the userID of this client, 
 * (reply = "succeed:" userID "connected the board to server")
 * and the client should create a serverSocket on port userID + Flingball.PORT_START_NUMBER, start serving and send a 
 * SERVER_REQUEST to FlingballServer.
 * 
 * If request is a SERVER_REQUEST, the FlingballServer will create a socket for this client in order
 * to send requests to it in the future.
 * 
 * If request is a QUIT_REQUEST, the FlingballServer will end the connection with this client.
 * and all other boards that are joined with this client's board will disjoin with it.
 * 
 * If request is a TRANSFER_BALL_REQUEST, FlingballServer will send message to the client
 * with BOARDNAME to create a new ball with given info in that Flingball's game. If 
 * BOARDNAME doesn't exist, it will return an error message.
 * 
 * If request is a PORTAL_BALL_REQUEST, FlingballServer will send message to the client
 * with BOARDNAME to create a new ball with given info in this Flingball's game, but if PORTALNAME
 * or BOARDNAME doesn't exist, it will return an error message.
 * 
 * Besides the format, also requires that the location and velocity are valid for balls.
 * 
 * If the request doesn't follow the protocol, server will return an error message and
 * do nothing else.
 * ***************************
 * For each client, FlingballServer can also act as it's client and send requests to each of them.
 * ***************************
 * User can use commands to join two boards together.
 * Two boards can be joined side-by-side using the following command:
 * h NAME_left NAME_right
 * Two boards can be joined top-and-bottom using the following command:
 * v NAME_top NAME_bottom
 * where NAME_direction is the name of the board the user wants to join 
 */
public class FlingballServer {
    
    private final ServerSocket serverSocket;
    private int userNumber;
    private final Map<String, Integer> boardUserMap;
    private final Map<String, List<String>> boardPortalMap;
    private final Map<Integer, FBServerSocket> userSockets;
    private static final String CLOSE= "close";
    public static final int PORT = 10987;
    
    // Abstraction function:
    //   AF(serverSocket,userNumber,boardUserMap,boardPortalMap,userSockets)=
    //               The flingball server with serverSocket serverSocket, 
    //               with total userNumber users, (including disconnected users).
    //               Each client has a unique userID.
    //               boardUserMap is the map that map board names to their userIds,
    //               boardPortalMap is the map that map board names to their portal lists,
    //               and userSockets is the map that map userIDs to their FBServerSockets.
    // Representation invariant:
    //    1.All fields not null.  
    //    2.boardUserMap,boardPortalMap,userSockets are of same size.
    // Safety from rep exposure:
    //    1. All fields except userNumber are private and final. userNumber is private.
    //    2. Fields are never returned except userNumber, and userNumber is immutable.
    //    3. boardUserMap.keySet() and boardPortalMap.keySet() are the same set.    
    // Thread safety argument:
    //    FlingballServer itself is not a thread-safe data type, but its use of multiply 
    //     inner threads is safe, and it can serve multiple clients at the same time.
    //    1. All instance methods except serve, handleConnection, getCommandAndAct requires the lock
    //       of "this".
    //    2. getCommandAndAct is only called inside main method in one thread which only
    //       deals with command line input. So it is safe not to use synchronized keyword on it.
    //    3. serve is called on one thread. And inside serve, it creates one thread
    //       to deal with each client, and each client is limited to one thread. In the later 
    //       part of serve where it doesn't use synchronized keyword, it only calls 
    //       handleConnection method.
    //    4. handleConnection doesn't touch the rep of this, and it calls handleConnection 
    //       which is synchronized on "this".
    
    /**
     * Usage:
     * FlingballServer [--port PORT]
     * Square brackets mean that the arguments are optional. PORT is an integer in the 
     * range 0 to 65535 inclusive, specifying the port where the server should listen for 
     * incoming connections. If this argument is not given, then the default port is 10987.
     * @throws IOException if exception
     */
    public static void main(String[] args) throws IOException {
        int port=PORT;
        if(args[0].equals("FlingballServer")) {
            if(args.length==3 && args[1].equals("--port")) {
                port=Integer.parseInt(args[2]);
            }
            FlingballServer server = new FlingballServer(port);
            new Thread(new Runnable() {
                public void run() {
                    while(true) {
                        try {
                            server.getCommandAndAct();
                        } catch (IOException ioe) {
                            ioe.printStackTrace(); 
                        } 
                    }
                }
            }).start();
            new Thread(() ->  {
                try {
                    server.serve();
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }).start();

        }
    }
    
    // checkRep
    private synchronized void checkRep() {
        assert serverSocket!=null;
        assert boardUserMap!=null;
        assert boardPortalMap!=null;
        assert userSockets!=null;
    }
    
    /**
     * Make a new text game server using board that listens for connections on port.
     * 
     * @param port server port number
     * @throws IOException if an error occurs opening the server socket
     */
    public FlingballServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.userNumber = 0;
        boardUserMap=Collections.synchronizedMap(new HashMap<>());
        userSockets=Collections.synchronizedMap(new HashMap<>());
        boardPortalMap=Collections.synchronizedMap(new HashMap<>());
        checkRep();
    }

    /**
     * @return current user number
     */
    public synchronized int getUserNumber() {
        return boardUserMap.size();
    }
    /**
     * get commands and act 
     */
    public void getCommandAndAct() throws IOException{
        //read commands from user
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        final String input = in.readLine();
        //command: "h NAME_left NAME_right" or "v NAME_top NAME_bottom"
        String[] tokens = input.split(" ");
        if(tokens.length!=3 || !(tokens[0].equals("h") || (tokens[0].equals("v"))) ){
            System.err.println("SERVER illegal command");
        }else {
            String board1=tokens[1];
            String board2=tokens[2];
            System.out.println("SERVER join b1: "+board1+" b2: "+board2);
            if(!hasBoardName(board1) || !hasBoardName(board2)){
                System.err.println("SERVER illegal board names");
            }else if(tokens[0].equals("h")){
                leftJoin(board1, board2);
            }else {
                topJoin(board1, board2);
            }
        }
    }
    
    /**
     * @return the port on which this server is listening for connections
     */
    public synchronized int port() {
        return serverSocket.getLocalPort();
    }
    
    /**
     * Run the server, listening for and handling client connections.
     * Never returns normally.
     * 
     * @throws IOException if an error occurs waiting for a connection
     */
    public void serve() throws IOException {
        //handle multiple clients
        while (true) {
            // block until a client connects
            Socket socket = serverSocket.accept();
            userNumber++;
            int userID;
            userID = userNumber;
            checkRep();
            Thread handler = new Thread(new Runnable() {
                public void run() {
                    // handle the client
                    try {
                        while(!socket.isClosed()) {
                            try {
                                handleConnection(socket,userID);
                                checkRep();
                            } catch (IOException ioe) {
                                ioe.printStackTrace(); // but do not stop serving
                            } 
                        }
                    }finally {
                        try {
                            synchronized (this) {
                                checkRep();
                                socket.close();
                                String board=getBoardWithUser(userID);
                                boardUserMap.remove(board);
                                userSockets.remove(userID);
                                disjoinBoard(board);
                                checkRep();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            handler.start();
        }
    }
    
    /**
     * Handle a single client connection.
     * Returns when the client disconnects.
     * 
     * @param socket socket connected to client
     * @throws IOException if the connection encounters an error or closes unexpectedly
     */
    private void handleConnection(Socket socket, int userID) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try{
            for (String input = in.readLine(); input != null; input = in.readLine()) {
                synchronized(this) {
                    String output = handleRequest(input,userID);
                    if(output.equals(CLOSE)) {
                        break;
                    }
                    out.println(output);
                }
            }
        }finally {
            out.close();
            in.close();
        }
    }
    /*
     * get the board name with userID userID
     */
    private synchronized String getBoardWithUser(int userID) {
        for(String board:boardUserMap.keySet()) {
            if(boardUserMap.get(board).equals(userID)) {
                return board;
            }
        }
        return "";
    }
    /**
     * Handle a single client request and return the server response.
     * @param input message from client
     * @return output message to client
     * @throws IOException if network or server failure
     */
    private synchronized String handleRequest(String input, int userID) throws IOException  {
        String[] tokens = input.split(" ");
        // handles start requests CONNECT_REQUEST ::= "connect" BOARDNAME PORTALNAME* NEWLINE
        if (tokens[0].equals("connect") && tokens.length >=2) {
            String boardName=tokens[1];
            if(hasBoardName(boardName)) {
                return "fail: board was connected already";
            }else {
                boardUserMap.put(boardName,userID);
                List<String> portals=new ArrayList<>();
                for(int i=2; i<tokens.length; i++) {
                    portals.add(tokens[i]);
                }
                boardPortalMap.put(boardName, portals);
                return "succeed: "+userID+" connected the board to server";
            }
        // handles quit requests QUIT_REQUEST ::= "quit" NEWLINE
        }else if (tokens[0].equals("quit") && tokens.length == 1) {
            return CLOSE;
        }else if(tokens[0].equals("tran")||tokens[0].equals("port")) {
            String otherBoard=tokens[1];
            if(!boardUserMap.containsKey(otherBoard)) {
                return "fail: no such board";
            }
            //handles TRANSFER_BALL_REQUEST ::= "tran" BOARDNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME NEWLINE
            else if (tokens[0].equals("tran")) {
                int otherUserID = boardUserMap.get(otherBoard);
                FBServerSocket socket=userSockets.get(otherUserID);
                //"tran" LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME NEWLINE
                String ballRequest = "tran "+input.substring(input.indexOf(tokens[1])+tokens[1].length()+1);
                sendRequest(ballRequest, socket);
                return "succeed: tran ball received and sent";
            //handles PORTAL_BALL_REQUEST::= "port" BOARDNAME PORTALNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME "from" BOARDNAME NEWLINE
            }else{
                String portalName=tokens[2];
                if(!boardPortalMap.get(otherBoard).contains(portalName)) {
                    return "fail: no such portal";
                }
                int otherUserID = boardUserMap.get(otherBoard);
                FBServerSocket socket=userSockets.get(otherUserID);
                //"port" PORTALNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME "from" BOARDNAME NEWLINE
                String ballRequest = "port "+input.substring(input.indexOf(tokens[1])+tokens[1].length()+1);
                // try to send ball to PORTALNAME in otherboard, if no such portal, the client
                // of otherboard will send it back by sending a request to this server
                sendRequest(ballRequest, socket);
                return "succeed: portal ball received and sent";
            }
        //handles server created request
        }else if (input.equals("server created")) {
            try {
                FBServerSocket newSocket = new FBServerSocket(userID);
                userSockets.put(userID, newSocket);
                return "succeed: created socket for this server";
            } catch (IOException e) {
                e.printStackTrace();
                return "fail: fail to create socket";
            }
        }else {
            return "fail: illegal command!!";
        }
    }
    /*
     * return true if a board with name exist, otherwise false
     */
    private synchronized boolean hasBoardName(String name) {
        return boardUserMap.containsKey(name);
    } 
    /**
     * Send a ball request to the server. Requires this is "open".
     * @param request ballRequest
     * @throws IOException if network or server failure
     */
    private synchronized void sendRequest(String request, FBServerSocket fbSocket) throws IOException {
        fbSocket.out().print(request + "\n");
        fbSocket.out().flush(); // important! 
    }
    /**
     * Joins the first board to the left of the second board
     * if any of these boards don't exist, do nothing.
     * -------------------
     * | board1 | board2 |
     * -------------------
     * @param board1 the board on the left
     * @param board2 the board on the right
     * @throws IOException if error
     */
    private synchronized void leftJoin(String board1, String board2) throws IOException {
        if(hasBoardName(board1) && hasBoardName(board2)) {
            int userID1=boardUserMap.get(board1);
            int userID2=boardUserMap.get(board2);
            //"join" DIRECTION BOARDNAME
            String request1="join right "+board2;
            sendRequest(request1, userSockets.get(userID1));
            String request2="join left "+board1;
            sendRequest(request2, userSockets.get(userID2));
        }
    }
    
    /**
     * Joins the first board to the top of the second board
     * if any of these boards don't exist, do nothing.
     * --------
     * |board1|
     * --------
     * |board2|
     * --------
     * @param board1 the board on the top
     * @param board2 the board on the bottom
     * @throws IOException if error
     */
    private synchronized void topJoin(String board1, String board2) throws IOException {
        if(hasBoardName(board1) && hasBoardName(board2)) {
            int userID1=boardUserMap.get(board1);
            int userID2=boardUserMap.get(board2);
            //"join" DIRECTION BOARDNAME
            String request1="join bottom "+board2;
            sendRequest(request1, userSockets.get(userID1));
            String request2="join top "+board1;
            sendRequest(request2, userSockets.get(userID2));
        }
    }
    /*
     * disjoin given board with board
     */
    private synchronized void disjoinBoard(String board) throws IOException {
        String request="disjoin "+board;
        for(FBServerSocket socket:userSockets.values()) {
            sendRequest(request, socket);
        }
    }
    @Override 
    public synchronized String toString() {
        return "FlingballServer with "+getUserNumber()+" clients";
    }
    
    /*
     * A private class to FlingballServer class.
     * A FBServerSocket is a Socket and its BufferedReader in, its PrintWriter out.
     * It can only be accessed by one thread at a time.
     */
    private class FBServerSocket{
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        // Abstraction function:
        //   AF(socket,in,out)=The FBServerSocket is a Socket 
        //              and its BufferedReader in, its PrintWriter out.
        // Representation invariant:
        //     All fields not null.
        // Safety from rep exposure:
        //     Class is private
        
        // constructor
        private FBServerSocket(int userID) throws UnknownHostException, IOException {
            final String host="localhost";
            socket = new Socket(host, userID + Flingball.PORT_START_NUMBER);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        // in
        private BufferedReader in() {
            return in;
        }
        // out
        private PrintWriter out() {
            return out;
        }
    }
    
}

