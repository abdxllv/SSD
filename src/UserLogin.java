package src;

import java.sql.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserLogin {
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
                    HashUtils.showAlertS("Account Setup Required", "Please set your password as it has not been configured in the database yet.");
                    UserChangePassword userChangePassword = new UserChangePassword(stage, username);
                    userChangePassword.initializeComponents();

                    return;
                }

                String userHash = HashUtils.generateHash(password, salt);
                System.out.println("Hash stored in Database:\n" + storedHash);
                System.out.println("Hash made from user input:\n" + userHash);

                if (userHash.equals(storedHash)) {
                    if ("Supervisor".equals(rs.getString("role"))) {
                        SuperInterface superInterface = new SuperInterface(stage, username);
                        DBUtils.logQuery(username, "Supervisor Interface");

                        superInterface.initializeComponents();
                        return;
                    }
                    if("Clerk".equals(rs.getString("role"))){
                        ClerkInterface clerkInterface = new ClerkInterface(stage, username);
                        DBUtils.logQuery(username, "Clerk Interface");

                        clerkInterface.initializeComponents();
                    }
                    if("Mechanic".equals(rs.getString("role"))){
                        MechInterface mechInterface = new MechInterface(stage, username);
                        DBUtils.logQuery(username, "Mechanic Interface");

                        mechInterface.initializeComponents();
                    }


                } else {
                    System.err.println("Authentication Failed: Invalid username or password.");
                    DBUtils.logQuery(username, "Log in attempt failure", query);
                    HashUtils.showAlertF("Authentication Failed", "Invalid username or password.");
                }
            } else {
                System.err.println("Authentication Failed: Invalid username or password.");
                DBUtils.logQuery(username, "Log in attempt failure", query);
                HashUtils.showAlertF("Authentication Failed", "Invalid username or password.");
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            HashUtils.showAlertF("Database Error", "Failed to connect to the database.");
        }
    }


}
