package client;

import commands.Command;
import commands.SystemMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;

import javax.sound.midi.SysexMessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public ListView<String> clientList;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private HBox authPanel;
    @FXML
    private HBox messagePanel;

    private static Socket socket;
    private static final int PORT = 8189;
    private static final int WAIT_CONSTANT = 120000;
    private static final String IP_ADDRESS = "localhost";

    private static DataInputStream in;
    private static DataOutputStream out;

    private boolean authenticated;
    private String nickname;

    private Stage stage;
    private Stage regStage;
    private RegController regController;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        messagePanel.setVisible(authenticated);
        messagePanel.setManaged(authenticated);
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
        }

        setTitle(nickname);
        textArea.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @SneakyThrows
                @Override
                public void handle(WindowEvent event) {
                    System.out.println(SystemMessage.GOODBYE);
                    if (socket != null && !socket.isClosed())
                        out.writeUTF(Command.END);
                }
            });
        });

        setAuthenticated(false);
    }

    private void connect() {

        getStreams();
        new Thread(() -> {
            try {
                socket.setSoTimeout(WAIT_CONSTANT);
                boolean authCycle = false;
                while (!authCycle) {
                    String str = in.readUTF();
                    authCycle = authCycleHandler(str);
                }

                boolean workCycle = true;
                while (workCycle) {
                    String str = in.readUTF();
                    workCycle = workCycleHandler(str);
                }
            } catch (SocketTimeoutException e){
                try {
                    out.writeUTF(Command.END);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }catch (RuntimeException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                setAuthenticated(false);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            }).start();
    }

    private boolean workCycleHandler(String str) {
        if(!str.startsWith("/")) textArea.appendText(str + "\n");

        else if (str.equals(Command.END)) {
            setAuthenticated(false);
            return false;
        }
        else if (str.startsWith(Command.CLIENT_LIST)) {
            String[] token = str.split("\\s");
            Platform.runLater(() -> {
                clientList.getItems().clear();
                for (int i = 1; i < token.length; i++) {
                    clientList.getItems().add(token[i]); }
            });
        }
        return true;
    }

    private boolean authCycleHandler(String str) {
        if(!str.startsWith("/")) textArea.appendText(str + "\n");

        else if (str.equals(Command.END)) shutdownCommand();

        else if (str.startsWith(Command.AUTH_OK)) {
            authPassedCommand(str);
            return true;
        }
        else if (str.equals(Command.REG_OK)) regPassedCommand();
        else if (str.equals(Command.REG_NO)) regController.resultTryToReg(false);

        return false;
    }

    @SneakyThrows
    private void getStreams() {
        socket = new Socket(IP_ADDRESS, PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    @SneakyThrows
    private void regPassedCommand() {
        socket.setSoTimeout(0);
        regController.resultTryToReg(true);
    }

    private void authPassedCommand(String str) {
        nickname = str.split("\\s")[1];
        setAuthenticated(true);
    }

    private void shutdownCommand() {
        System.out.println(SystemMessage.SERVER_GOING_SHUTDOWN);
        throw new RuntimeException(SystemMessage.SERVER_GOING_SHUTDOWN);
    }


    @SneakyThrows
    public void sendMsg(ActionEvent actionEvent) {
        if (textField.getText().trim().length() > 0) {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        }
    }

    @SneakyThrows
    public void trytoAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed())
            connect();

        String msg = String.format("%s %s %s", Command.AUTH, loginField.getText().trim(), passwordField.getText().trim());

        out.writeUTF(msg);
        passwordField.clear();
    }

    private void setTitle(String title) {
        Platform.runLater(() -> {
            if (title.equals("")) {
                stage.setTitle(SystemMessage.CHAT);
                return;
            }
            stage.setTitle(String.format("Чат [ %s ]", title));
        });
    }

    public void clientListClicked(MouseEvent mouseEvent) {
        System.out.println(clientList.getSelectionModel().getSelectedItem());
        String msg = String.format("%s %s ", Command.PRIVATE_MSG, clientList.getSelectionModel().getSelectedItem());
        textField.setText(msg);
    }

    public void clickRegBtn(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    @SneakyThrows
    private void createRegWindow() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
        Parent root = fxmlLoader.load();
        regController = fxmlLoader.getController();
        regController.setController(this);

        regStage = new Stage();
        regStage.setTitle(SystemMessage.REGISTRATION);
        regStage.setScene(new Scene(root, 400, 300));

        regStage.initModality(Modality.APPLICATION_MODAL);
        regStage.initStyle(StageStyle.UTILITY);

    }

    @SneakyThrows
    public void tryToReg(String login, String password, String nickname) {
        String message = String.format("%s %s %s %s", Command.REG, login, password, nickname);

        if (socket == null || socket.isClosed())
            connect();

        out.writeUTF(message);
    }
}
