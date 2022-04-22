package server;

import commands.Command;
import lombok.SneakyThrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class ClientHandler { //класс для работы с подклчившимся клиентом
    private Server server;
    private Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    private String nickname;
    private String login;

    private static Logger log;

    private ExecutorService service;

    @SneakyThrows
    public ClientHandler(Server server, Socket socket, Logger logger) {
        service = Executors.newCachedThreadPool();
        this.server = server;
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        log = logger;
        start();
    }

    private void start() {
        service.execute(() -> {
            try {
                //цикл аутентификации
                socket.setSoTimeout(120000);
                while (true) {
                    String str = in.readUTF();

                    if (str.equals(Command.END))
                        endCommandHandler();

                    if (str.startsWith(Command.AUTH))
                        if(authCommandHandler(str))
                            break;

                    if (str.startsWith(Command.REG))
                        regCommandHandler(str);

                }

                //цикл работы
                while (true) {
                    String str = in.readUTF();
                    if(!str.startsWith("/"))
                        server.broadcastMsg(this, str);

                    if (str.equals(Command.END)) {
                        endCommandHandler();
                        break;
                    }

                    if (str.startsWith(Command.PRIVATE_MSG))
                        privateMessageHandler(str);
                }
            } catch (SocketTimeoutException e){
                try {
                    out.writeUTF(Command.END);
                    log.log(Level.INFO, "client's escape successfully");
                } catch (IOException ex) {
                    log.log(Level.SEVERE, "IOException", e);
                }
            } catch (RuntimeException e) {
                log.log(Level.SEVERE, "RuntimeException", e);
            } catch (IOException e) {
                log.log(Level.SEVERE, "IOException", e);
            } finally {
                System.out.println("Client disconnected");
                log.info("client disconnected");
                server.unsubscribe(this);
                try {
                    socket.close();
                    log.info("socket was closed");
                } catch (IOException e) {
                    log.log(Level.SEVERE, "IOException", e);
                }
            }
        });
        service.shutdown();
    }

    @SneakyThrows //обработка команды регистрации
    private void regCommandHandler(String str){
        String[] token = str.split("\\s");
        if (token.length < 4)
            return;

        boolean regSuccessful = server.getAuthService().registration(token[1], token[2], token[3]);

        if(!regSuccessful){
            sendMsg(Command.REG_NO);
            log.log(Level.WARNING, "Registration failed");
            return;
        }

        socket.setSoTimeout(0);
        sendMsg(Command.REG_OK);
        log.info("Registration OK");
    }

    @SneakyThrows //обработка команды аутентификации
    private boolean authCommandHandler(String str) {
        String[] token = str.split("\\s");
        String newNick = server.getAuthService().getNicknameByLoginAndPassword(token[1], token[2]);

        login = token[1];
        if(newNick == null){
            sendMsg("Неверный логин / пароль");
            log.log(Level.WARNING, "incorrect login/password");
            return false;
        }
        if(server.isLoginAuthenticated(login)){
            sendMsg("С этим логином уже вошли");
            log.log(Level.SEVERE, "login has arleady used");
            return false;
        }

        nickname = newNick;
        sendMsg(Command.AUTH_OK + " " + nickname);
        server.subscribe(this);
        log.info("client tryed to authenticated. Successfully");
        return true;
    }

    private void privateMessageHandler(String str) { //отправка приватного сообщения
        String[] token = str.split("\\s+", 3);
        if (token.length < 3) {
            return;
        }
        server.privateMsg(this, token[1], token[2]);
        log.log(Level.INFO, "client wrote a private message");
    }

    @SneakyThrows
    private void endCommandHandler() { //обработка отключения
        out.writeUTF(Command.END);
        log.log(Level.INFO, "client disconnected");
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            log.log(Level.SEVERE, "ERROR in client message", e);
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
