package src;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MechInterface {
    private Scene mechInterface;
    private Stage stage;
    private String username;

    public MechInterface(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        if (!SessionManager.isValidSession()) {
            UserLogin logIn = new UserLogin(stage);
            CryptUtils.showAlertF("Session Error", "Session has expired.");
            logIn.initializeComponents();
        }

        VBox mechLayout = new VBox(10);
        mechLayout.setPadding(new Insets(10));
        Button registerButton = new Button("Register Spare Part");
        Button scheduleButton = new Button("Schedule Vehicle S/M");
        Button viewButton = new Button("View Vehicle(s)");
        Button performButton = new Button("Perform S/M");
        Button changePassButton = new Button("Change Password");
        Button logOutButton = new Button("Log Out");

        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!SessionManager.renewSession()) {
                    UserLogin logIn = new UserLogin(stage);
                    CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                MechRegisterSP registerSP = new MechRegisterSP(stage, username);
                DBUtils.logQuery(username, "Register Spare Part Menu");
                registerSP.initializeComponents();
            }
        });

        scheduleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!SessionManager.renewSession()) {
                    UserLogin logIn = new UserLogin(stage);
                    CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                MechScheduleInt scheduleInt = new MechScheduleInt(stage, username);
                DBUtils.logQuery(username, "Mechanic Scedhuling Interface");
                scheduleInt.initializeComponents();
            }
        });
        viewButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!SessionManager.renewSession()) {
                    UserLogin logIn = new UserLogin(stage);
                    CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                VehicleView viewvehicle = new VehicleView(stage, username);
                DBUtils.logQuery(username, "View All Vehicles");
                viewvehicle.initializeComponents();
            }
        });
        performButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!SessionManager.renewSession()) {
                    UserLogin logIn = new UserLogin(stage);
                    CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                MechPerformList list = new MechPerformList(stage, username);
                list.initializeComponents();
            }
        });
        changePassButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!SessionManager.renewSession()) {
                    UserLogin logIn = new UserLogin(stage);
                    CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                UserChangePassword changePassword = new UserChangePassword(stage, username);
                changePassword.initializeComponents();
            }
        });
        logOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserLogin logout = new UserLogin(stage);
                DBUtils.logQuery(username, "Log Out");
                SessionManager.logout();
                logout.initializeComponents();
            }
        });

        mechLayout.getChildren().addAll(
                registerButton, viewButton, scheduleButton, performButton, changePassButton, logOutButton);

        mechInterface = new Scene(mechLayout, 300, 230);
        stage.setTitle("Mechanic Interface");
        stage.setScene(mechInterface);
        stage.show();
    }
}

