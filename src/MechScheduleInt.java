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
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class MechScheduleInt {
    private Scene mechInterface;
    private Stage stage;
    private String username;

    public MechScheduleInt(Stage primaryStage, String username) {
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

        try {
            Connection con = DBUtils.establishConnection();
            String query = "SELECT licensePlate, make, model, status FROM vehicle WHERE status IS NULL OR status = 'Awaiting Service'";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            DBUtils.logQuery(username, "Showing the vehicles that need to be scheduled for service", query);


            while (rs.next()) {
                String licensePlate = rs.getString("licensePlate");
                String make = rs.getString("make");
                String model = rs.getString("model");
                String status = rs.getString("status");

                Label label = new Label("License Plate: " + licensePlate +
                        ", Vehicle: " + make + " " + model +
                        ", Status: " + (status == null ? "None" : status));

                Button scheduleButton = new Button("Schedule");
                scheduleButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (!SessionManager.renewSession()) {
                            UserLogin logIn = new UserLogin(stage);
                            CryptUtils.showAlertF("Session Error", "Session has expired.");
                            logIn.initializeComponents();
                            return;
                        }
                        MechScheduleFinal scheduler = new MechScheduleFinal(stage, username, licensePlate);
                        scheduler.initializeComponents();
                    }
                });

                HBox hBox = new HBox(10, label, scheduleButton);
                mechLayout.getChildren().add(hBox);
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            CryptUtils.showAlertF("Database Error", "Failed to retrieve vehicle data.");
        }

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
                MechInterface back = new MechInterface(stage, username);
                back.initializeComponents();
            }
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mechLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox finalLayout = new VBox(10, scrollPane, backButton);
        finalLayout.setPadding(new Insets(10));

        mechInterface = new Scene(finalLayout, 600, 400);
        stage.setTitle("Schedule Maintenance");
        stage.setScene(mechInterface);
        stage.show();
    }
}
