package server;

import commands.Command;
import commands.SystemMessage;
import lombok.SneakyThrows;
import server.authentication_services.AuthHandler;
import server.authentication_services.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static commands.SystemMessage.CLIENT_DISCONNECTED;

public class User {

    private static final AuthService authService = new SimpleAuthService();
    private static final AuthHandler authHandler = new AuthHandler(Server.getClients());

    private DataInputStream in;
    private DataOutputStream out;

    private Server server;
    private Socket socket;

    private String nickname;
    private String login;

    @SneakyThrows
    public User(Server server, Socket socket){
        this.server = server;
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void setHandler() {
        try {
            new Thread(() -> {
                try {
                    //цикл аутентификации
                    socket.setSoTimeout(120000);
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals(Command.END)) { disconnectCommand();}
                        else if (str.startsWith(Command.AUTH)) {
                                if(authCommand(str)) break;
                        }
                        else if (str.startsWith(Command.REG)) registrCommand(str);

                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith(Command.PRIVATE_MSG)) {
                            privateMessageCommand(str);
                            continue;
                        }
                        else if (str.equals(Command.END)) {
                            disconnectCommand();
                            authHandler.unsubscribe(this);
                            socket.close();
                            break;
                        }

                        server.broadcastMsg(this, str);
                    }

//               SocketTimeoutExceptioт
                } catch (SocketTimeoutException e) {
                    try {
                        out.writeUTF(Command.END);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Client disconnected");
                    authHandler.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } finally {

        }
    }

    private void privateMessageCommand(String str) {
        String[] token = str.split("\\s+", 3);
        if (token.length < 3) {
            return;
        }
        server.privateMsg(this, token[1], token[2]);
    }

    private boolean authCommand(String str) {
        String[] token = str.split("\\s");
        String newUser = authService
                .getNicknameByLoginAndPassword(token[1], token[2]);

        if(newUser == null){
            sendMsg(SystemMessage.WRONG_LOGIN_OR_PASSWORD);
            return false;
        }
        if (authHandler.isLoginAuthenticated(token[1])){
            sendMsg(SystemMessage.ALREADY_USEFUL_LOGIN);
            return false;
        }

        nickname = newUser;
        login = token[1];
        sendMsg(Command.AUTH_OK + " " + nickname);
        authHandler.subscribe(this);
        return true;
    }

    @SneakyThrows
    private void disconnectCommand() {
        System.out.println(SystemMessage.CLIENT_DISCONNECTED);
        out.writeUTF(Command.END);
    }

    private void registrCommand(String str) {
        String[] token = str.split("\\s");
        if (token.length < 4) { return;}

        boolean regSuccessful = authService
                .registration(token[1], token[2], token[3]);

        if (regSuccessful) sendMsg(Command.REG_OK);

        sendMsg(Command.REG_NO);
    }

    @SneakyThrows
    public void sendMsg(String msg) {
        out.writeUTF(msg);
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
