package src;

import java.sql.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;

public class UserDeactivate {
    private Scene viewUsersInt;
    private Stage stage;
    private String username;

    public UserDeactivate(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        if (!SessionManager.isValidSession()) {
            UserLogin logIn = new UserLogin(stage);
            CryptUtils.showAlertF("Session Error", "Session has expired.");
            logIn.initializeComponents();
        }

        VBox viewLayout = new VBox(10);
        viewLayout.setPadding(new Insets(10));
        Button backButton = new Button("Back");

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

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(viewLayout);
        scrollPane.setFitToWidth(true);

        try {
            Connection con = DBUtils.establishConnection();
            String query = "SELECT username, name, role FROM users";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            DBUtils.logQuery(username, "Deactivate Users Menu", query);
            while (rs.next()) {
                String userName = rs.getString("username");
                String name = rs.getString("name");
                String role = rs.getString("role");
                Label userLabel = new Label("Username: " + userName + ", Name: " + name + ", Role: " + role);

                HBox userBox;
                if (!role.equalsIgnoreCase("Supervisor")) {
                    Button deactivateButton = new Button("Deactivate");
                    deactivateButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            if (!SessionManager.renewSession()) {
                                UserLogin logIn = new UserLogin(stage);
                                CryptUtils.showAlertF("Session Error", "Session has expired.");
                                logIn.initializeComponents();
                                return;
                            }
                            deactivateUser(userName);
                        }
                    });
                    userBox = new HBox(10, userLabel, deactivateButton);
                } else {
                    userBox = new HBox(10, userLabel);
                }

                viewLayout.getChildren().add(userBox);
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            e.printStackTrace();
            CryptUtils.showAlertF("Database Error", "Failed to retrieve user data.");
        }

        viewLayout.getChildren().add(backButton);
        viewUsersInt = new Scene(scrollPane, 400, 400);
        stage.setTitle("View All Users");
        stage.setScene(viewUsersInt);
        stage.show();
    }

    private void deactivateUser(String userName) {
        try {
            Connection con = DBUtils.establishConnection();
            String query = "DELETE FROM users WHERE username = ?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, userName);
            int rowsAffected = statement.executeUpdate();
            DBUtils.logQuery(username, "Deactivate a user", query);

            DBUtils.closeConnection(con, statement);

            if (rowsAffected > 0) {
                CryptUtils.showAlertS("Success", "User deactivated successfully.");
                new UserDeactivate(stage,username).initializeComponents();
            } else {
                CryptUtils.showAlertF("Error", "Failed to deactivate user.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CryptUtils.showAlertF("Database Error", "Failed to deactivate user.");
        }
    }
}
