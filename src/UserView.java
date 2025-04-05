package src;

import java.sql.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class UserView {
    private Scene viewUsersInt;

    private Stage stage;
    private String username;

    public UserView(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        VBox viewLayout = new VBox(10);
        viewLayout.setPadding(new Insets(10));
        Button backButton = new Button("Back");

        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SuperInterface superInterface = new SuperInterface(stage, username);
                superInterface.initializeComponents();
            }
        });

        try {
            Connection con = DBUtils.establishConnection();
            String query = "SELECT username, name, role FROM users";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            DBUtils.logQuery(username, "View all users", query);

            while (rs.next()) {
                String userName = rs.getString("username");
                String name = rs.getString("name");
                String role = rs.getString("role");
                Label userLabel = new Label("Username: " + userName + ", Name: " + name + ", Role: " + role);
                viewLayout.getChildren().add(userLabel);
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            e.printStackTrace();
            HashUtils.showAlertF("Database Error", "Failed to retrieve user data.");
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(viewLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox finalLayout = new VBox(10);
        finalLayout.setPadding(new Insets(10));
        finalLayout.getChildren().addAll(scrollPane, backButton);

        viewUsersInt = new Scene(finalLayout, 400, 400);
        stage.setTitle("View All Users");
        stage.setScene(viewUsersInt);
        stage.show();
    }
}
