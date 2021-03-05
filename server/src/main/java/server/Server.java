package server;

import commands.Command;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;
    private static Logger log;

    public Server(Logger logger) throws SQLException, ClassNotFoundException {
        log = logger;
        clients = new CopyOnWriteArrayList<>();
        authService = new SQLAuthService();
        try {
            server = new ServerSocket(PORT);
            System.out.println("server started");
            log.info("server started");

            while (true) {
                socket = server.accept();
                System.out.println("client connected" + socket.getRemoteSocketAddress());
                log.info("client connected");
                new ClientHandler(this, socket, log);
                log.info("client created successfully");
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "IOException", e);
        } finally {
            try {
                server.close();
                log.info("server closed successfully");
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error at server close", e);
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("[ %s ] : %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
            log.log(Level.INFO,"message: \"" + message + "\" was sent");
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] to [ %s ]: %s", sender.getNickname(), receiver, msg);
        for (ClientHandler c : clients) {
            if(c.getNickname().equals(receiver)){
                c.sendMsg(message);
                if(!c.equals(sender)){
                    sender.sendMsg(message);
                    log.log(Level.INFO, "private message: \"" + message + "\" was sent to " + receiver);
                }
                return;
            }
        }
        sender.sendMsg("not found user: "+ receiver);
        log.log(Level.WARNING, "not found user: "+ receiver);
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        log.log(Level.INFO, "client was added");
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        log.log(Level.INFO, "client was removed");
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthenticated(String login){
        for (ClientHandler c : clients) {
            if(c.getLogin().equals(login)){
                log.log(Level.WARNING, "client was trying to enter with usable login");
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList(){
        StringBuilder sb = new StringBuilder(Command.CLIENT_LIST);
        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getNickname());
        }

        String message = sb.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }
}
