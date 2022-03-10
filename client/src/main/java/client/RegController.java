package client;

import commands.SystemMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nicknameField;
    @FXML
    private TextArea textArea;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nickname = nicknameField.getText().trim();

        if (login.length() * password.length() * nickname.length() == 0) {
            textArea.appendText(SystemMessage.LOGIN_OR_PASSWORD_MUST_NOT_BE_EMPTY);
            return;
        }

        if (login.contains(" ") || password.contains(" ") || nickname.contains(" ")) {
            textArea.appendText(SystemMessage.LOGIN_AND_PASSWORD_MUST_NOT_CONTAINS_SPACES);
            return;
        }

        controller.tryToReg(login, password, nickname);
    }

    public void resultTryToReg(boolean flag) {
        if (flag) {
            textArea.appendText(SystemMessage.REGISTRATION_PASSED);
        } else {
            textArea.appendText(SystemMessage.REGISTRATION_FAILED +
                    SystemMessage.MAYBE_LOGIN_OR_PASSWORD_HAS_ALREDY_BLOCKED);
        }
    }
}
