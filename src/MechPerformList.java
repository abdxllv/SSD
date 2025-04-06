package src;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.src.CryptUtils;
import src.src.DBUtils;
import src.src.MechInterface;
import src.src.SessionManager;
import src.src.UserLogin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MechPerformList {
    private Scene performListScene;
    private Stage stage;
    private String username;

    public MechPerformList(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        if (!src.src.SessionManager.isValidSession()) {
            src.src.UserLogin logIn = new src.src.UserLogin(stage);
            src.src.CryptUtils.showAlertF("Session Error", "Session has expired.");
            logIn.initializeComponents();
        }

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        Button backButton = new Button("Back");

        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!src.src.SessionManager.renewSession()) {
                    src.src.UserLogin logIn = new src.src.UserLogin(stage);
                    src.src.CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                src.src.MechInterface mechInterface = new MechInterface(stage, username);
                mechInterface.initializeComponents();
            }
        });

        try {
            Connection con = src.src.DBUtils.establishConnection();
            String query = "SELECT id, licensePlate, scheduledDate FROM schedule";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            src.src.DBUtils.logQuery("System", "Showing the vehicles that maintenance/service can be performed on", query);


            while (rs.next()) {
                int scheduleId = rs.getInt("id");
                String licensePlate = rs.getString("licensePlate");
                String scheduledDate = rs.getString("scheduledDate");

                Label scheduleLabel = new Label(scheduleId+ ". License Plate: " + licensePlate + ", Scheduled Date: " + scheduledDate);

                Button performButton = new Button("Perform S/M");
                performButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (!SessionManager.renewSession()) {
                            src.src.UserLogin logIn = new UserLogin(stage);
                            src.src.CryptUtils.showAlertF("Session Error", "Session has expired.");
                            logIn.initializeComponents();
                            return;
                        }
                        MechPerformService performService = new MechPerformService(stage, username, licensePlate);
                        performService.initializeComponents();
                    }
                });

                HBox scheduleBox = new HBox(10, scheduleLabel, performButton);
                layout.getChildren().add(scheduleBox);
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            e.printStackTrace();
            CryptUtils.showAlertF("Database Error", "Failed to retrieve schedule data.");
        }

        layout.getChildren().add(backButton);

        performListScene = new Scene(layout, 500, 400);
        stage.setTitle("Scheduled Maintenance List");
        stage.setScene(performListScene);
        stage.show();
    }
}
