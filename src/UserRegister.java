package src;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserRegister {
    private Scene RegisterScene;
    private TextField usernameField = new TextField();
    private TextField nameField = new TextField();
    private TextField emailField = new TextField();
    private TextField phoneField = new TextField();
    private ComboBox<String> roleComboBox = new ComboBox<>();  // Changed to ComboBox

    private Stage stage;
    private String username;

    public UserRegister(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        if (!SessionManager.isValidSession()) {
            UserLogin logIn = new UserLogin(stage);
            CryptUtils.showAlertF("Session Error", "Session has expired.");
            logIn.initializeComponents();
        }

        VBox registerLayout = new VBox(10);
        registerLayout.setPadding(new Insets(10));
        Button backButton = new Button("Back");
        Button registerButton = new Button("Register User");

        // Populate the ComboBox with roles
        roleComboBox.getItems().addAll("Supervisor", "Clerk", "Mechanic");
        roleComboBox.setValue("Clerk");

        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!SessionManager.renewSession()) {
                    UserLogin logIn = new UserLogin(stage);
                    CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                SuperInterface superInterface = new SuperInterface(stage, username);
                superInterface.initializeComponents();
            }
        });
        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!SessionManager.renewSession()) {
                    UserLogin logIn = new UserLogin(stage);
                    CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                register();
            }
        });

        registerLayout.getChildren().addAll(
                new Label("Username:"), usernameField,
                new Label("Name"), nameField,
                new Label("Email"), emailField,
                new Label("Phone"), phoneField,
                new Label("Role"), roleComboBox,
                registerButton,
                new Label("or"), backButton
        );

        RegisterScene = new Scene(registerLayout, 300, 420);
        stage.setTitle("User Registration");
        stage.setScene(RegisterScene);
        stage.show();
    }

    private void register() {
        String userName = usernameField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = roleComboBox.getValue();

        if (!CryptUtils.isValidUsername(userName)) {
            CryptUtils.showAlertF("Invalid username", "Username can contain only letters, digits, underscores and '.', minimum of 3 characters");
            return;
        }

        if (!CryptUtils.isValidName(name)) {
            CryptUtils.showAlertF("Invalid Name", "Name should only contain letters");
            return;
        }

        if (!CryptUtils.isValidEmail(email)) {
            CryptUtils.showAlertF("Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (!CryptUtils.isValidQatariPhone(phone)) {
            CryptUtils.showAlertF("Invalid Phone", "Please enter a valid Qatari phone number.\n" +
                    "Formats accepted: +974XXXXXXXX, 00974XXXXXXXX, XXXXXXXX, XXXX-XXXX");
            return;
        }

        Connection con = null;
        PreparedStatement statement = null;

        try {
            con = DBUtils.establishConnection();
            String query = "INSERT INTO `users` (`username`, `name`, `email`, `phone`, `role`) VALUES (?, ?, ?, ?, ?);";
            statement = con.prepareStatement(query);
            statement.setString(1, userName);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, phone);
            statement.setString(5, role);
            int rs = statement.executeUpdate();

            DBUtils.logQuery(username, "New user registered", query);

            if (rs == 1) {
                CryptUtils.showAlertS("Success", "User registered successfully!");
                SuperInterface superInterface = new SuperInterface(stage, username);
                superInterface.initializeComponents();
            } else {
                CryptUtils.showAlertF("Registration Failed", "Username Unavailable.");
            }
        } catch (Exception e) {
            CryptUtils.showAlertF("Database Error", "Failed to register.");
        } finally {
            DBUtils.closeConnection(con, statement);
        }
    }

}
