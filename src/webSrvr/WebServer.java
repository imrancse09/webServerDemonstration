package webSrvr;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Imran
 * Date: 11/19/13
 * Time: 10:49 PM
 * To change this template use File | Settings | File Templates.
 */

public final class WebServer {

    public static void main(String[] args) throws Exception {

        int port = 9999;

        Socket clientSocket = null;
        ServerSocket serverSocket = new ServerSocket(port);

         while(true) {

            System.out.println("Waiting for request...");

            clientSocket = serverSocket.accept();

            System.out.println("Request Accepted...");
            System.out.println("\n");

            HttpRequestHandler httpRequestHandler = new HttpRequestHandler(clientSocket);

            Thread thread = new Thread(httpRequestHandler);
            thread.start();

         }
    }
}

