package flingball;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;
import physics.Vect;

public class ServerTest {
   
    private static final String LOCALHOST = "127.0.0.1";
    public static final int PORT = 10987;

    /*
     * testing:
     * 
     * tests for FlingballServer:
     * 
     * test by sending request:
     * partition on request: CONNECT_REQUEST,QUIT_REQUEST,SERVER_REQUEST,TRANSFER_BALL_REQUEST,PORTAL_BALL_REQUEST
     * 
     * partition on TRANSFER_BALL_REQUEST: 
     *    succeed, fail
     *    board exist, board not exist
     *    
     * partition on PORTAL_BALL_REQUEST: 
     *    succeed, fail
     *    board exist, board not exist
     *    portal exist, board not exist
     * 
     * partition on user number: =1, >1
     * 
     * tests for Flingball:
     * 
     * (methods that send requests can be tested jointly with tests of FlingballServer)
     * 
     * test by sending request to Flingball :
     * partition on request: JOIN_REQUEST |DISJOIN_REQUEST | TRANSFER_BALL_REQUEST | PORTAL_BALL_REQUEST
     * 
     * partition on JOIN_REQUEST: 
     *    direction is left, right, top, bottom
     *    
     * partition on DISJOIN_REQUEST: 
     *    direction is left, right, top, bottom
     *    
     * partition on TRANSFER_BALL_REQUEST
     *    
     * PORTAL_BALL_REQUEST
     * 
     * partition on user number: =1, >1
     * 
     * since the sendRequest method is private in FlingballServer
     * we can design tests that could trigger FlingballServer to send requests
     * to test the behaviors after getting requests in Flingball
     * 
     */
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    //covers ball Request
    @Test
    public void testServerBallRequest1() throws IOException, InterruptedException {
        final int port=PORT+2;
        FlingballServer server = new FlingballServer(port);
        startServer(server);
        final String[] reply = new String[1];
        Board board = new Board("b1",0,0,0);
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        //"tran" BOARDNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME NEWLINE
        String ballRequest="tran b2 1 1 1 1 ball";
        reply[0]=flingball.sendBallRequest(ballRequest);
        flingball.close();
        assertEquals("reply","fail: no such board", reply[0]);
    }
    
    
    //covers ball Request
    @Test
    public void testServerBallRequest2() throws IOException, InterruptedException {
        final int port=PORT+4;
        FlingballServer server = new FlingballServer(port);
        startServer(server);
        
        final String[] result = new String[2];
        
        Board board = new Board("b2",0,0,0);
        board.addPortal(new Portal("portal1", "p2", "b3", new Vect(1,1)));
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest("b2");
        
        Board board2 = new Board("b1",0,0,0);
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        flingball2.sendConnectRequest(board2.getName());
        String ballRequest2="port b2 portal1 1 1 1 1 ball from b1";
        flingball2.sendBallRequest(ballRequest2);
        
        Thread.sleep(300);
        result[0]=board.getBalls().size()+"";
        result[1]=board.getBalls().get(0).getName();
        flingball.close();   
        flingball2.close();
        
        assertEquals("get ball","1",result[0]);
        assertEquals("get ball","ball",result[1]);
    }
    
    //covers ball Request
    @Test
    public void testServerBallRequest3() throws IOException, InterruptedException {
        final int port=PORT+5;
        FlingballServer server = new FlingballServer(port);
        startServer(server);
        final String[] result = new String[2];
        Board board = new Board("b2",0,0,0);
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest("b2 portal2");
        
        Board board2 = new Board("b1",0,0,0);
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        flingball2.sendConnectRequest(board2.getName());
        String ballRequest2="port b2 portal1 1 1 1 1 ball from b1";
        
        Thread.sleep(500);
        result[0]=board.getBalls().size()+"";
        
        result[1]=flingball2.sendBallRequest(ballRequest2);
        flingball2.close();
        flingball.close();
        assertEquals("get ball","0",result[0]);
        assertEquals("reply","fail: no such portal", result[1]);
    }
    //covers ball Request
    @Test
    public void testServerBallRequest4() throws IOException, InterruptedException {
        final int port=PORT+6;
        FlingballServer server = new FlingballServer(port);
        startServer(server);
        final String[] reply = new String[2];
        Board board = new Board("b1",0,0,0);
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        //"tran" BOARDNAME LOCA_X LOCA_Y VELO_X VELO_Y BALLNAME NEWLINE
        String ballRequest="tran b1 1 1 1 1 ball";
        reply[0]=flingball.sendBallRequest(ballRequest);
        Thread.sleep(200);
        reply[1]=board.getBalls().size()+"";
        flingball.close();
        assertEquals("reply","succeed: tran ball received and sent", reply[0]);
        assertEquals("ball number","1", reply[1]);
    }
    //covers ball Request
    // this is a visually tested test. The ball should be teleported between two portals
    @Test
    public void testServerBallRequest_Portal_VisualTest() throws IOException, InterruptedException {
        final int port=PORT + 11;
        FlingballServer server = new FlingballServer(port);
        startServer(server);        
        
        Board board2 = new Board("board2",0,0,0);
        board2.addPortal(new Portal("portal2", "portal1", "board1", new Vect(10,10)));
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        flingball2.sendConnectRequest(board2.getName());
        Simulator simulator2 = new Simulator(board2);
        simulator2.playFlingball();

        Board board = new Board("board1",0,0,0);
        board.addBall(new Ball("ball", new Vect(1,1), new Vect(-10,-10)));
        board.addPortal(new Portal("portal1", "portal2", "board2", new Vect(10,10)));
        
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Thread.sleep(15000);
        flingball.close();
        flingball2.close();
    }
    //covers ConnectRequest
    @Test
    public void testServerConnectRequest() throws IOException, InterruptedException {
        final int port=PORT;
        FlingballServer server = new FlingballServer(port);
        startServer(server);

        final String[] reply = new String[3];
        
        Board board1 = new Board("b10",0,0,0);
        final Flingball flingball1 = new Flingball(LOCALHOST, port, board1);
        reply[0]=flingball1.sendConnectRequest(board1.getName());
        
        Board board2 = new Board("b20",0,0,0);
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        reply[1]=flingball2.sendConnectRequest(board2.getName());
        
        reply[2]=server.getUserNumber()+"";
        
        flingball1.close();
        flingball2.close();
        
        Thread.sleep(500);
        assertEquals("reply","succeed: 1 connected the board to server",reply[0]);
        assertEquals("reply","succeed: 2 connected the board to server",reply[1]);
        assertEquals("user number","2", reply[2]);
        assertEquals("user last number",0, server.getUserNumber());
    }

    //covers quitRequest
    @Test
    public void testServerQuitRequest() throws IOException, InterruptedException {
        final int port=PORT+1;
        FlingballServer server = new FlingballServer(port);
        startServer(server);
        Board board = new Board("bq",0,0,0);
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        assertEquals("user number",1, server.getUserNumber() );
        flingball.sendQuitRequest();
        flingball.close();
        Thread.sleep(500);
        assertEquals("user number",0, server.getUserNumber() );
    }
    
    //covers JOIN_REQUEST
    @Test
    public void testaFlingballJoinRequest() throws IOException, InterruptedException {
        final int port=PORT+17;
        FlingballServer server = new FlingballServer(port);
        startServer(server);
        final String[] result = new String[6];
        
        Board board = new Board("b2",0,0,0);
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        
        Board board2 = new Board("b1",0,0,0);
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        flingball2.sendConnectRequest(board2.getName());
        
        new Thread(() ->  {
            try {
                Thread.sleep(300);
                String command = "h b1 b2";
                System.setIn(new ByteArrayInputStream(command.getBytes()));
                server.getCommandAndAct();
            } catch (Exception ioe) {
                ioe.printStackTrace(); 
            } 
        }).start();
        
        Thread.sleep(1000);
        result[0]=board.getJoinedBoard(BOARD_DIRECTION.LEFT);
        result[2]=board.getJoinedBoard(BOARD_DIRECTION.RIGHT);
        result[5]=board.getJoinedBoard(BOARD_DIRECTION.BOTTOM);
        flingball.close();

        result[1]=board2.getJoinedBoard(BOARD_DIRECTION.RIGHT);
        result[3]=board2.getJoinedBoard(BOARD_DIRECTION.LEFT);
        result[4]=board2.getJoinedBoard(BOARD_DIRECTION.TOP);
        flingball2.close();
        
        assertEquals("left","b1",result[0]);
        assertEquals("right","b2",result[1]);
        assertEquals("empty","",result[2]+result[3]+result[4]+result[5]);
    }
    //covers JOIN_REQUEST
    //JOIN_REQUEST JOIN_REQUEST |DISJOIN_REQUEST | TRANSFER_BALL_REQUEST | PORTAL_BALL_REQUEST
    @Test
    public void testFlingballJoinRequest2() throws IOException, InterruptedException {
        final int port=PORT+8;
        FlingballServer server = new FlingballServer(port);
        startServer(server);
        Board board = new Board("b1",0,0,0);
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        new Thread(() ->  {
            try {
                Thread.sleep(300);
                String command = "v b1 b1";
                System.setIn(new ByteArrayInputStream(command.getBytes()));
                server.getCommandAndAct();
            } catch (Exception ioe) {
                ioe.printStackTrace(); 
            } 
        }).start();
        Thread.sleep(1000);
        assertEquals("top","b1",board.getJoinedBoard(BOARD_DIRECTION.TOP));
        assertEquals("bottom","b1",board.getJoinedBoard(BOARD_DIRECTION.BOTTOM));
        assertEquals("left","",board.getJoinedBoard(BOARD_DIRECTION.LEFT));
        assertEquals("right","",board.getJoinedBoard(BOARD_DIRECTION.RIGHT));
        flingball.close();
    }
    // covers join and disjoin request
    // this is a visually tested test.There should be a ball in the left game board first,
    // then the two boards are joined left to right, then the right one is closed, and the 
    // ball goes back to the left board
    @Test
    public void testFlingballJoinRequest_BoardJoinAndDisjoin_VisualTest() throws IOException, InterruptedException {
        final int port=PORT+10;
        FlingballServer server = new FlingballServer(port);
        startServer(server);        
        Board board = new Board("b1",0,0,0);
        board.addBall(new Ball("ball", new Vect(10,10), new Vect(80,20)));
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        new Thread(() ->  {
            try {
                Thread.sleep(2000);
                String command = "h b1 b2";
                System.setIn(new ByteArrayInputStream(command.getBytes()));
                server.getCommandAndAct();
            } catch (Exception ioe) {
                ioe.printStackTrace(); 
            } 
        }).start();
        Board board2 = new Board("b2",0,0,0);
        Simulator simulator2 = new Simulator(board2);
        simulator2.playFlingball();
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        flingball2.sendConnectRequest(board2.getName());
        Thread.sleep(8000);
        flingball2.close();
        Thread.sleep(4000);
        flingball.close();
    }
    @Test
    public void testFlingballJoinRequest_Corner_VisualTest() throws IOException, InterruptedException {
        final int port=PORT+15;
        FlingballServer server = new FlingballServer(port);
        startServer(server);        
        Board board = new Board("b1");
        
        board.addBall(new Ball("ball", new Vect(3,17), new Vect(120,-120)));
        
        Thread.sleep(800);
        System.err.println("ballnum"+board.getBalls().size());
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        new Thread(() ->  {
            try {
                Thread.sleep(500);
                String command = "v b1 b2";
                System.setIn(new ByteArrayInputStream(command.getBytes()));
                server.getCommandAndAct();
                String command4 = "h b2 b1";
                System.setIn(new ByteArrayInputStream(command4.getBytes()));
                server.getCommandAndAct();
                String command2 = "h b1 b1";
                System.setIn(new ByteArrayInputStream(command2.getBytes()));
                server.getCommandAndAct();

            } catch (Exception ioe) {
                ioe.printStackTrace(); 
            } 
        }).start();
        Board board2 = new Board("b2",0,0,0);
        Simulator simulator2 = new Simulator(board2);
        simulator2.playFlingball();
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        flingball2.sendConnectRequest(board2.getName());
        Thread.sleep(40000);
        flingball.close();
        flingball2.close();
    }
    // covers join and disjoin request
    // this is a visually tested test.There should be a ball in the left game board first,
    // then the two boards are joined left to right, then the right one is closed, and the 
    // ball goes back to the left board
    @Test
    public void testFlingballJoinRequest_ThreeBall_VisualTest() throws IOException, InterruptedException {
        final int port=PORT+12;
        FlingballServer server = new FlingballServer(port);
        startServer(server);        
        Board board = new Board("bcd",0,0,0);
        board.addBall(new Ball("ball1", new Vect(10,10), new Vect(10,-10)));
        board.addBall(new Ball("ball2", new Vect(18,15), new Vect(0,10)));
        board.addBall(new Ball("ball3", new Vect(18,12), new Vect(10,0)));
        System.err.println(board);
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        new Thread(() ->  {
            try {
                Thread.sleep(500);
                String command = "h bcd board2";
                System.setIn(new ByteArrayInputStream(command.getBytes()));
                server.getCommandAndAct();
                String command2 = "v bcd bcd";
                System.setIn(new ByteArrayInputStream(command2.getBytes()));
                server.getCommandAndAct();
            } catch (Exception ioe) {
                ioe.printStackTrace(); 
            } 
        }).start();
        Board board2 = new Board("board2",0,0,0);
        Simulator simulator2 = new Simulator(board2);
        simulator2.playFlingball();
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        flingball2.sendConnectRequest(board2.getName());
        Thread.sleep(10000);
        flingball.close();
        flingball2.close();
    }
 // covers join and disjoin request
    // this is a visually tested test.There should be a ball in the left game board first,
    // then the two boards are joined left to right, then the right one is closed, and the 
    // ball goes back to the left board
    @Test
    public void testFlingballJoinRequest_BumperOnEdge_VisualTest() throws IOException, InterruptedException {
        final int port=PORT+18;
        FlingballServer server = new FlingballServer(port);
        startServer(server);        
        Board board = new Board("b1",0,0,0);
        board.addBall(new Ball("ball", new Vect(18,10), new Vect(5,0)));
        board.addBall(new Ball("ball", new Vect(18,15), new Vect(5,0)));
        board.addBumper(new SquareBumper("bump1", new Vect(16,10)));
        board.addBumper(new SquareBumper("bump1", new Vect(16,15)));
        System.err.println(board);
        Simulator simulator = new Simulator(board);
        simulator.playFlingball();
        
        Board board2 = new Board("b2",0,0,0);
        board2.addBumper(new SquareBumper("bump", new Vect(0,10)));
        board2.addBumper(new SquareBumper("bump", new Vect(3,15)));
        Simulator simulator2 = new Simulator(board2);
        simulator2.playFlingball();
        
        
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        
        new Thread(() ->  {
            try {
                Thread.sleep(1000);
                String command = "h b1 b2";
                System.setIn(new ByteArrayInputStream(command.getBytes()));
                server.getCommandAndAct();
            } catch (Exception ioe) {
                ioe.printStackTrace(); 
            } 
        }).start();
        
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        flingball2.sendConnectRequest(board2.getName());
        Thread.sleep(8000);
        flingball2.close();
        Thread.sleep(3000);
        flingball.close();
    }
    //covers DISJOIN_REQUEST
    @Test
    public void testFlingballDisjoinRequest() throws IOException, InterruptedException {
        final int port=PORT + 9;
        FlingballServer server = new FlingballServer(port);
        startServer(server);
        final String[] result = new String[4];  
        Board board = new Board("b9",0,0,0);
        Flingball flingball = new Flingball(LOCALHOST, port, board);
        flingball.sendConnectRequest(board.getName());
        Board board2 = new Board("b1",0,0,0);
        Flingball flingball2 = new Flingball(LOCALHOST, port, board2);
        flingball2.sendConnectRequest(board2.getName());
        
        new Thread(() ->  {
            try {
                Thread.sleep(300);
                String command = "v b9 b1";
                System.setIn(new ByteArrayInputStream(command.getBytes()));
                server.getCommandAndAct();
            } catch (Exception ioe) {
                ioe.printStackTrace(); 
            } 
        }).start();
        Thread.sleep(700);
        result[0]=board.getJoinedBoard(BOARD_DIRECTION.BOTTOM);
        flingball2.close();
        Thread.sleep(700);
        result[1]=board.getJoinedBoard(BOARD_DIRECTION.BOTTOM);
        flingball.close();
        assertEquals("bottom","b1",result[0]);
        assertEquals("bottom no board","",result[1]);
    }
 
    /* Start server on its own thread. */
    private static Thread startServer(final FlingballServer server) {
        Thread thread = new Thread(() ->  {
            try {
                server.serve();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
        thread.start();
        return thread;
    }
}
