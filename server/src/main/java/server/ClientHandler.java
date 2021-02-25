package server;

import commands.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;

public class ClientHandler {
    private Server server;
    private Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    socket.setSoTimeout(120000);
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                System.out.println("client want to disconnect ");
                                out.writeUTF(Command.END);
                                throw new RuntimeException("client want to disconnect");
                            }
                            if (str.startsWith(Command.AUTH)) {
                                String[] token = str.split("\\s");
                                String newNick = server.getAuthService()
                                        .getNicknameByLoginAndPassword(token[1], token[2]);
                                login = token[1];
                                if (newNick != null) {
                                    if (!server.isLoginAuthenticated(login)) {
                                        nickname = newNick;
                                        sendMsg(Command.AUTH_OK + " " + nickname);
                                        server.subscribe(this);
                                        break;
                                    } else {
                                        sendMsg("С этим логином уже вошли");
                                    }
                                } else {
                                    sendMsg("Неверный логин / пароль");
                                }
                            }

                            if (str.startsWith(Command.REG)) {
                                String[] token = str.split("\\s");
                                if (token.length < 4) {
                                    continue;
                                }
                                boolean regSuccessful = server.getAuthService()
                                        .registration(token[1], token[2], token[3]);
                                if (regSuccessful) {
                                    socket.setSoTimeout(0);
                                    sendMsg(Command.REG_OK);
                                } else {
                                    sendMsg(Command.REG_NO);
                                }
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                out.writeUTF(Command.END);
                                break;
                            }

                            if (str.startsWith(Command.PRIVATE_MSG)) {
                                String[] token = str.split("\\s+", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                server.privateMsg(this, token[1], token[2]);
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }

//               SocketTimeoutExceptioт
                } catch (SocketTimeoutException e){
                    try {
                        out.writeUTF(Command.END);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Client disconnected");
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
