package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connector {
    private ServerSocket serverSocket;
    private Socket socket;
    private final int PORT = 8189;
    private Server server;
    private static Logger log;

    public Connector(Logger log, Server server) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("server started");
            log.info("server started");

            while (true) {
                socket = serverSocket.accept();
                System.out.println("client connected" + socket.getRemoteSocketAddress());
                log.info("client connected");
                new ClientHandler(server, socket, log);
                log.info("client created successfully");
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "IOException", e);
        } finally {
            try {
                serverSocket.close();
                log.info("server closed successfully");
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error at server close", e);
            }
        }
    }
}
