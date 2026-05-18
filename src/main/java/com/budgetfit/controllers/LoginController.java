package com.budgetfit.controllers;

import com.budgetfit.dao.UserDAO;
import com.budgetfit.model.User;
import com.budgetfit.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * Controller handling user login and registration flows.
 * Integrates secure BCrypt authentication, 4-digit PIN setup, validation constraints, and user session initializations.
 */
public class LoginController {

    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordBox;
    @FXML private CheckBox rememberMeBox;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button loginButton;

    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regPasswordTextField;
    @FXML private PasswordField regPinField;
    @FXML private TextField regPinTextField;
    @FXML private PasswordField regPinConfirmField;
    @FXML private TextField regPinConfirmTextField;
    @FXML private CheckBox regShowPasswordBox;
    @FXML private Label regErrorLabel;

    private Preferences prefs;

    @FXML
    private void initialize() {
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        regPasswordTextField.textProperty().bindBidirectional(regPasswordField.textProperty());
        regPinTextField.textProperty().bindBidirectional(regPinField.textProperty());
        regPinConfirmTextField.textProperty().bindBidirectional(regPinConfirmField.textProperty());

        prefs = Preferences.userNodeForPackage(LoginController.class);
        String savedUser = prefs.get("saved_username", "");
        if (!savedUser.isEmpty()) {
            usernameField.setText(savedUser);
            if (rememberMeBox != null) {
                rememberMeBox.setSelected(true);
            }
        }
    }

    @FXML
    private void togglePasswordVisible() {
        boolean visible = showPasswordBox.isSelected();
        passwordTextField.setVisible(visible);
        passwordField.setVisible(!visible);
    }

    @FXML
    private void toggleRegPasswordVisible() {
        boolean visible = regShowPasswordBox.isSelected();
        regPasswordTextField.setVisible(visible);
        regPasswordField.setVisible(!visible);
        regPinTextField.setVisible(visible);
        regPinField.setVisible(!visible);
        regPinConfirmTextField.setVisible(visible);
        regPinConfirmField.setVisible(!visible);
    }

    @FXML
    private void showRegisterForm() {
        loginForm.setVisible(false);
        loginForm.setManaged(false);
        registerForm.setVisible(true);
        registerForm.setManaged(true);
        hideMessages();
    }

    @FXML
    private void showLoginForm() {
        registerForm.setVisible(false);
        registerForm.setManaged(false);
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        hideMessages();
    }

    private void hideMessages() {
        if (errorLabel != null) errorLabel.setVisible(false);
        if (successLabel != null) successLabel.setVisible(false);
        if (regErrorLabel != null) regErrorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        hideMessages();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        User user = UserDAO.authenticate(username, password);
        if (user != null) {
            UserSession.setCurrentUser(user.getId(), user.getUsername(), user.getRole());
            UserDAO.recordLogin(user.getId());

            if (rememberMeBox != null && rememberMeBox.isSelected()) {
                prefs.put("saved_username", username);
            } else {
                prefs.remove("saved_username");
            }

            showSuccess("Success! Proceeding...");

            try {
                String fxmlFile = UserSession.isAdmin() ? "/fxml/admin.fxml" : "/fxml/dashboard.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 800));
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error loading dashboard.");
            }
        } else {
            showError("Invalid username or password.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        hideMessages();
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String pin = regPinField.getText().trim();
        String pinConfirm = regPinConfirmField.getText().trim();

        if (username.length() < 3) {
            showRegError("Username must be at least 3 characters.");
            return;
        }
        if (username.contains(":") || username.contains(" ")) {
            showRegError("Username cannot contain spaces or colons.");
            return;
        }
        if (password.length() < 6) {
            showRegError("Password must be at least 6 characters.");
            return;
        }
        if (!pin.matches("\\d{4}")) {
            showRegError("ATM PIN must be exactly 4 digits.");
            return;
        }
        if (!pin.equals(pinConfirm)) {
            showRegError("ATM PINs do not match.");
            return;
        }
        if (UserDAO.usernameExists(username)) {
            showRegError("Username already exists.");
            return;
        }

        if (UserDAO.registerUser(username, password, pin, "user")) {
            regErrorLabel.setVisible(false);
            regUsernameField.clear();
            regPasswordField.clear();
            regPinField.clear();
            regPinConfirmField.clear();
            showRegError("Registration successful. You can now log in.");
            regErrorLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        } else {
            showRegError("Registration failed due to a system error.");
            regErrorLabel.setTextFill(javafx.scene.paint.Color.web("#ba1a1a"));
        }
    }

    private void showError(String message) {
        if (successLabel != null) successLabel.setVisible(false);
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void showSuccess(String message) {
        if (errorLabel != null) errorLabel.setVisible(false);
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
        }
    }

    private void showRegError(String message) {
        if (regErrorLabel != null) {
            regErrorLabel.setText(message);
            regErrorLabel.setVisible(true);
        }
    }
}
