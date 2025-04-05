package src;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClerkInterface {
    private Scene clerkInterface;
    private Stage stage;
    private String username;

    public ClerkInterface(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        VBox clerkLayout = new VBox(10);
        clerkLayout.setPadding(new Insets(10));
        Button registerButton = new Button("Register Vehicle");
        Button viewButton = new Button("View Vehicle(s)");
        Button returnButton = new Button("Return Vehicle");
        Button changePassButton = new Button("Change Password");
        Button logOutButton = new Button("Log Out");


        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ClerkVehicleRegister clerkVehicleRegister = new ClerkVehicleRegister(stage, username);
                clerkVehicleRegister.initializeComponents();
            }
        });
        viewButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                VehicleView viewvehicle = new VehicleView(stage, username);
                DBUtils.logQuery(username, "View All Vehicles");
                viewvehicle.initializeComponents();
            }
        });
        returnButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ClerkVehicleReturn returnVehicle = new ClerkVehicleReturn(stage, username);
                DBUtils.logQuery(username, "Vehicle Return Menu");
                returnVehicle.initializeComponents();
            }
        });
        changePassButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserChangePassword changePassword = new UserChangePassword(stage, username);
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

        clerkLayout.getChildren().addAll(
                registerButton, viewButton, returnButton, changePassButton, logOutButton);

        clerkInterface = new Scene(clerkLayout, 300, 230);
        stage.setTitle("Clerk Interface");
        stage.setScene(clerkInterface);
        stage.show();
    }
}
