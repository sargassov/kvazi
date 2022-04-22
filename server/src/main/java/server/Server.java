package server;

import commands.Command;
import lombok.Data;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class Server { //Класс сервера

    private List<ClientHandler> clients;
    private AuthService authService;
    private static Logger log;
    private Connector connector;

    public Server(Logger logger) throws SQLException, ClassNotFoundException {
        log = logger;
        clients = new CopyOnWriteArrayList<>();
        authService = new SQLAuthService();
        connector = new Connector(logger, this);

    }

    public void broadcastMsg(ClientHandler sender, String msg) { //отправка широковещательного сообщения
        String message = String.format("[ %s ] : %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
            log.log(Level.INFO,"message: \"" + message + "\" was sent");
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) { //отправка приватного сообщения
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

    public void subscribe(ClientHandler clientHandler) { //добавление в список пользователей в чате
        clients.add(clientHandler);
        log.log(Level.INFO, "client was added");
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) { //удаление пользователя из списка
        clients.remove(clientHandler);
        log.log(Level.INFO, "client was removed");
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthenticated(String login){  //проверка на уже зашедшего пользователя
        for (ClientHandler c : clients) {
            if(c.getLogin().equals(login)){
                log.log(Level.WARNING, "client was trying to enter with usable login");
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList(){ //оправка списка клиентов для отображения на клиенте
        StringBuilder sb = new StringBuilder(Command.CLIENT_LIST);
        clients.forEach(c -> sb.append(" ").append(c.getNickname()));
        clients.forEach(c -> c.sendMsg(sb.toString()));
    }
}
