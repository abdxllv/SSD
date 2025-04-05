package src;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SuperInterface {
    private Scene superInterface;
    private Stage stage;
    private String username;

    public SuperInterface(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        VBox superLayout = new VBox(10);
        superLayout.setPadding(new Insets(10));
        Button registerButton = new Button("Register User");
        Button viewUsersButton = new Button("View Users");
        Button deactivateUserButton = new Button("Deactivate User");
        Button changePassButton = new Button("Change Password");
        Button logOutButton = new Button("Log Out");


        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserRegister userRegister = new UserRegister(stage, username);
                userRegister.initializeComponents();
            }
        });
        viewUsersButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserView viewuser = new UserView(stage, username);
                viewuser.initializeComponents();
            }
        });
        deactivateUserButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserDeactivate deactivate = new UserDeactivate(stage,username);
                deactivate.initializeComponents();
            }
        });
        changePassButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserChangePassword changePassword = new UserChangePassword(stage,username);
                changePassword.initializeComponents();
            }
        });
        logOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserLogin logout = new UserLogin(stage);
                DBUtils.logQuery(username, "Log Out");
                logout.initializeComponents();
            }
        });

        superLayout.getChildren().addAll(
                registerButton, viewUsersButton, deactivateUserButton, changePassButton, logOutButton);

        superInterface = new Scene(superLayout, 300, 230);
        stage.setTitle("Supervisor Interface");
        stage.setScene(superInterface);
        stage.show();
    }



}
