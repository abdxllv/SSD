package src;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserLogin {
    private static final Map<String, Integer> attemptCounts = new HashMap<>();
    private static final Map<String, Long> lockoutTimes = new HashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MS = 3 * 60 * 1000;

    private Scene loginScene;
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();

    private Stage stage;

    public UserLogin(Stage primaryStage) {
        this.stage = primaryStage;
    }

    public void initializeComponents() {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(10));
        Button loginButton = new Button("Sign In");

        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                authenticate();
            }
        });

        loginLayout.getChildren().addAll(new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                loginButton);

        loginScene = new Scene(loginLayout, 300, 230);
        stage.setTitle("User Login");
        stage.setScene(loginScene);
        stage.show();
    }


    private void authenticate() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Long lockoutTime = lockoutTimes.get(username);
        if (lockoutTime != null && System.currentTimeMillis() < lockoutTime) {
            long remainingSeconds = (lockoutTime - System.currentTimeMillis()) / 1000;
            CryptUtils.showAlertF("Locked Out",
                    "Too many attempts. Try again in " + remainingSeconds + " seconds.");
            return;
        }

        Connection con = DBUtils.establishConnection();
        String query = "SELECT salt,role, password FROM users WHERE username=?;";

        try {
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            DBUtils.logQuery(username, "Log in", query);
            if (rs.next()) {
                byte[] salt = rs.getBytes("salt");
                String storedHash = rs.getString("password");

                if (salt == null || storedHash == null || storedHash.isEmpty()) {
                    CryptUtils.showAlertS("Account Setup Required", "Please set your password as it has not been configured in the database yet.");
                    UserChangePassword userChangePassword = new UserChangePassword(stage, username);
                    userChangePassword.initializeComponents();

                    return;
                }

                String userHash = CryptUtils.generateHash(password, salt);
                System.out.println("Hash stored in Database:\n" + storedHash);
                System.out.println("Hash made from user input:\n" + userHash);

                if (userHash.equals(storedHash)) {
                    attemptCounts.remove(username);
                    lockoutTimes.remove(username);
                    DBUtils.logQuery(username, "Successful login");

                    String role = rs.getString("role");
                    SessionManager.createSession(username, role);

                    switch(role) {
                        case "Supervisor":
                            SuperInterface superInterface = new SuperInterface(stage, username);
                            DBUtils.logQuery(username, "Supervisor Interface");

                            superInterface.initializeComponents();
                            break;
                        case "Clerk":
                            ClerkInterface clerkInterface = new ClerkInterface(stage, username);
                            DBUtils.logQuery(username, "Clerk Interface");

                            clerkInterface.initializeComponents();
                            break;
                        case "Mechanic":
                            MechInterface mechInterface = new MechInterface(stage, username);
                            DBUtils.logQuery(username, "Mechanic Interface");

                            mechInterface.initializeComponents();
                            break;
                    }


                } else {
                    handleFailedAttempt(username, "Failed login attempt - wrong password");
                }
            } else {
                handleFailedAttempt(username, "Failed login attempt - unknown user");
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            handleFailedAttempt(username, "Failed login attempt - system error");
        }
    }


    private void handleFailedAttempt(String username, String logMessage) {
        // log to database
        DBUtils.logQuery(username, logMessage);

        // ipdate attempt count
        int attempts = attemptCounts.getOrDefault(username, 0) + 1;
        attemptCounts.put(username, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            long lockoutEnd = System.currentTimeMillis() + LOCKOUT_MS;
            lockoutTimes.put(username, lockoutEnd);
            CryptUtils.showAlertF("Locked Out",
                    "Too many attempts. Try again in 3 minutes.");
        } else {
            CryptUtils.showAlertF("Login Failed",
                    "Invalid credentials. " + (MAX_ATTEMPTS - attempts) + " attempts left.");
        }
    }


}
