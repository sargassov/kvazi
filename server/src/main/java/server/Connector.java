package server;

import commands.SystemMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector {

    private Server server;
    private ServerSocket serverSocket;
    private Socket socket;
    private final int PORT = 8189;

    public Connector(Server server) {
        this.server = server;
    }

    public void getConnect() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println(SystemMessage.SERVER_STARTED);

            while (true) {
                socket = serverSocket.accept();
                System.out.println(SystemMessage.CLIENT_CONNECTED + socket.getRemoteSocketAddress());
                new User(server, socket).setHandler();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
