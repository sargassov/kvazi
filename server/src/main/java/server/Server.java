package server;


import commands.SystemMessage;
import server.authentication_services.AuthHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


public class Server {
    private static List<User> clients;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        new Connector(this).getConnect();
        new AuthHandler(clients);
    }

    public void broadcastMsg(User sender, String msg) {
        String message = String.format("[ %s ] : %s", sender.getNickname(), msg);
        clients.forEach(c -> c.sendMsg(message));
    }

    public void privateMsg(User sender, String receiver, String msg) {
        String message = String.format("[ %s ] to [ %s ]: %s", sender.getNickname(), receiver, msg);
        List<User> clientList = clients.stream()
                .filter(c -> c.getNickname().equals(receiver) || c.equals(sender))
                .collect(Collectors.toList());

        if(clientList.size() == 0){
            sender.sendMsg(SystemMessage.USER_NOT_FOUND + receiver);
            return;
        }

        clientList.stream().filter(c -> c.getNickname().equals(receiver)).forEach(c -> c.sendMsg(message));
        clientList.stream().filter(c -> !c.equals(sender)).forEach(c -> sender.sendMsg(message));
    }

    public static List<User> getClients() {
        return clients;
    }
}
