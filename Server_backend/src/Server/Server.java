package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Server implements Runnable {
    private static final int PORT = 9999;
    Socket csocket;
    VotingSystem vs;

    Server(Socket csocket, VotingSystem vs) {
        System.out.print("making a new server\n");
        this.csocket = csocket;
        this.vs = vs;


    }

    public static void main(String args[])
            throws Exception {
        ServerSocket ssock = new ServerSocket(PORT);
        System.out.println("Listening");
        VotingSystem vs = new VotingSystem();


        while (true) {
            Socket sock = ssock.accept();
//            ssock.close();
            System.out.println("Connected");
            new Thread(new Server(sock, vs)).start();
        }
    }
    public void run() {
        try {
            InputStream is = csocket.getInputStream();
            DataOutputStream outToServer = new DataOutputStream(csocket.getOutputStream());

            
            byte[] bytes = new byte[csocket.getReceiveBufferSize()];
            is.read(bytes);  // byte array send from client Conncetion class 36 line
            
            String sendBack = vs.router(bytes)+"\n";
            outToServer.write(sendBack.getBytes());

            outToServer.flush();
            outToServer.close();
            is.close();
            csocket.close();
        }
        catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



