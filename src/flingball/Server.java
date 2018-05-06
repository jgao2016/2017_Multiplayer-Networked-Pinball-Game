package flingball;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.mit.eecs.parserlib.UnableToParseException;

import java.io.File;



public class Server {
    
//    
//    private final ServerSocket serverSocket;
//    private int userNumber;
////    private List<Board> boards;
//    private Map<Integer, Board> userBoards;
////    private Map<Board, Map<BOARD_DIRECTION, Board>> joinedBoards;
//    private static final String CLOSE= "close";
//    // Abstraction function:
//    //   TODO
//    // Representation invariant:
//    //   TODO
//    // Safety from rep exposure:
//    //   TODO
//    // Thread safety argument:
//    //   TODO
//    
//    /**
//     * Make a new text game server using board that listens for connections on port.
//     * 
//     * @param port server port number
//     * @throws IOException if an error occurs opening the server socket
//     */
//    public Server(int port) throws IOException {
//        this.serverSocket = new ServerSocket(port);
//        this.userNumber = 0;
//        userBoards=new HashMap<>();
////        joinedBoards=new HashMap<>();
////        boards=
//    }
//    
//    // checkRep
//    private void checkRep() {
//        
//    }
//    /**
//     * Check if the current board is connect to any other board
//     * requires such board exist
//     * @param name the board's name to check for connection
//     * @return boolean indicating if the board is still connected to any other board
//     */
//    
//    public boolean isConnectedToAnyBoard(String name,BOARD_DIRECTION direction) {
//        Board board = getBoardWithName(name);
//        return board.hasJoinedBoard(direction);
//    }
//    //////////////////////////////
//    
//    /**
//     * @return the port on which this server is listening for connections
//     */
//    public int port() {
//        return serverSocket.getLocalPort();
//    }
//    
//    /**
//     * get total clients connected to the sever
//     * @return total client connected to the server
//     */
//    public int getTotalClients() {
//        return userNumber;
//    }
//    
//    public Set<String> getAllConnectedBoardNames(){
//        return userBoards.values().stream().map(Board::getName).collect(Collectors.toSet());
//    }
//    /**
//     * Run the server, listening for and handling client connections.
//     * Never returns normally.
//     * 
//     * @throws IOException if an error occurs waiting for a connection
//     */
//    public void serve() throws IOException {
//      //handle multiple clients
//        while (true) {
//            // block until a client connects
//            Socket socket = serverSocket.accept();
//            userNumber++;
//            int userID = userNumber;
//            
//            Thread handler = new Thread(new Runnable() {
//                public void run() {
//                 // handle the client
//                    try {
//                        try {
//                            handleConnection(socket,userID);
//                            checkRep();
//                        } finally {
//                            Board board = userBoards.get(userID);
//                            userBoards.remove(userID);
////                            disjoinBoard(board);
//                            socket.close();
//                        }
//                    } catch (IOException ioe) {
//                        ioe.printStackTrace(); // but do not stop serving
//                    } 
//                }
//            });
//            handler.start();
//        }
//    }
//    
//    /**
//     * Handle a single client connection.
//     * Returns when the client disconnects.
//     * 
//     * @param socket socket connected to client
//     * @throws IOException if the connection encounters an error or closes unexpectedly
//     */
//    private void handleConnection(Socket socket, int userID) throws IOException {
//        //System.out.println("user "+user.getId()+"'s thread is handling");
//        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//        
//        try {
//            for (String input = in.readLine(); input != null; input = in.readLine()) {
//                String output = handleRequest(input,socket,userID);
//                if(output.equals(CLOSE)) {
//                    //System.out.println("closed");
//                    break;
//                }
//                out.println(output);
//            }
//        } finally {
//            out.close();
//            in.close();
//        }
//    }
//    
//    /**
//     * Handle a single client request and return the server response.
//     * if request is a start request, the server will start a board game for this client,
//     * and if request is a quie request, the server will end the connection with this client.
//     * and all other boards that are joined with this client's board will disjoin with it.
//     * 
//     * REQUEST ::= START_REQUEST | QUIT_REQUEST
//     * START_REQUEST ::= "board" FILENAME
//     * QUIT_REQUEST ::= "quit" NEWLINE
//     * FILENAME::= [A-Za-z_][A-Za-z_0-9]*'.fb'
//     * 
//     * if the request doesn't follow the protocol, server will return an error message and
//     * do nothing else.
//     * @param input message from client
//     * @return output message to client
//     */
//    private String handleRequest(String input, Socket socket, int userID)  {
//        String[] tokens = input.split(" ");
//        // handles start requests
//        if (tokens[0].equals("board") && tokens.length == 2) {
//            File fileToUse = new File("boards/"+tokens[1]); 
//            try {
//                Board board = BoardParser.parse(Flingball.boardFileToString(fileToUse));
//                if(hasBoardWithName(board.getName())) {
//                    return "the board was connected already";
//                }
//                Simulator simulator = new Simulator(board);
//                simulator.playFlingball();
//                userBoards.put(userID, board);
//                return "successful connected the board to server";
//            } catch (UnableToParseException e) {
//                return "illegal board";
//            } catch (IOException e) {
//                return "illegal board";
//            }
//        // handles quit requests
//        }else if (tokens[0].equals("quit") && tokens.length == 1) {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return CLOSE;
//        }else {
//            return "illegal command";
//        }
//    }
//    /*
//     * return true if a board with name exist, otherwise false
//     */
//    private boolean hasBoardWithName(String name) {
//        for(Board board:userBoards.values()) {
//            if(board.getName().equals(name)) return true;
//        }
//        return false;
//    }
//    /*
//     * return board with name, requires such board exists
//     */
//    private Board getBoardWithName(String name) {
//        for(Board board:userBoards.values()) {
//            if(board.getName().equals(name)) return board;
//        }
//        throw new RuntimeException("board name doesn't exist, shouldn't reach here");
//    }
//    
//    /**
//     * Joins the first board to the left of the second board
//     * if any of these boards don't exist, do nothing.
//     * @param board1 the board on the left
//     * @param board2 the board on the right
//     */
//    private void leftJoin(String board1, String board2) {
//        if(hasBoardWithName(board1) && hasBoardWithName(board2)) {
//            Board b1=getBoardWithName(board1);
//            Board b2=getBoardWithName(board2);
////            b1.joinBoard(b2, BOARD_DIRECTION.RIGHT);
////            b2.joinBoard(b1, BOARD_DIRECTION.LEFT);
//        }
//    }
//    
//    /**
//     * Joins the first board to the top of the second board
//     * if any of these boards don't exist, do nothing.
//     * @param board1 the board on the top
//     * @param board2 the board on the bottom
//     */
//    private void topJoin(String board1, String board2) {
//        if(hasBoardWithName(board1) && hasBoardWithName(board2)) {
//            Board b1=getBoardWithName(board1);
////            Board b2=getBoardWithName(board2);
////            b1.joinBoard(b2, BOARD_DIRECTION.TOP);
////            b2.joinBoard(b1, BOARD_DIRECTION.BOTTOM);
//        }
//    }
//    
//
////    private void disjoinBoard(Board board) {
////        Set<Board> otherBoards=(Set<Board>) joinedBoards.get(board).values();
////        for(Board otherBoard: otherBoards) {
////            Map<BOARD_DIRECTION, Board> thisMap=joinedBoards.get(otherBoard);
////            assert thisMap.values().contains(board);
////            if(thisMap.get(BOARD_DIRECTION.LEFT).equals(board)) thisMap.remove(BOARD_DIRECTION.LEFT);
////            else if(thisMap.get(BOARD_DIRECTION.RIGHT).equals(board)) thisMap.remove(BOARD_DIRECTION.RIGHT);
////            else if(thisMap.get(BOARD_DIRECTION.TOP).equals(board)) thisMap.remove(BOARD_DIRECTION.TOP);
////            else thisMap.remove(BOARD_DIRECTION.BOTTOM);
////        }
////        joinedBoards.remove(board);
////        checkRep();
////    }
}
