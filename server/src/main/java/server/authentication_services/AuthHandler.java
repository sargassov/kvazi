package server.authentication_services;

import commands.Command;
import server.User;

import java.util.List;

public class AuthHandler {
    private List<User> clients;

    public AuthHandler(List<User> clients) {
        this.clients = clients;
    }

    public void subscribe(User user) {
        clients.add(user);
        broadcastClientList();
    }

    public void unsubscribe(User removingClient) {
        clients.remove(removingClient);
        broadcastClientList();
    }

    public void broadcastClientList(){

        StringBuilder sb = new StringBuilder(Command.CLIENT_LIST);
        clients.forEach(c -> sb.append(" ").append(c.getNickname()));

        String message = sb.toString();
        clients.forEach(c -> c.sendMsg(message));
    }

    public boolean isLoginAuthenticated(String login){
        return clients.stream().anyMatch(c -> c.getLogin().equals(login));
    }

}
