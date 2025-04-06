package src;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class MechScheduleFinal {
    private Scene scheduleScene;
    private Stage stage;
    private String username;
    private String licensePlate;

    public MechScheduleFinal(Stage primaryStage, String username, String licensePlate) {
        this.stage = primaryStage;
        this.username = username;
        this.licensePlate = licensePlate;
    }

    public void initializeComponents() {
        if (!SessionManager.isValidSession()) {
            UserLogin logIn = new UserLogin(stage);
            CryptUtils.showAlertF("Session Error", "Session has expired.");
            logIn.initializeComponents();
        }

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        DatePicker datePicker = new DatePicker();
        Button submitButton = new Button("Submit");

        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!SessionManager.renewSession()) {
                    UserLogin logIn = new UserLogin(stage);
                    CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                if (datePicker.getValue() != null) {
                    LocalDate selectedDate = datePicker.getValue();
                    if (selectedDate.isBefore(LocalDate.now()) ) {
                        CryptUtils.showAlertF("Error", "The selected date cannot be in the past.");
                    } else {
                        insertSchedule(selectedDate.toString());
                    }
                } else {
                    CryptUtils.showAlertF("Error", "Please select a date for the maintenance.");
                }
            }
        });

        layout.getChildren().addAll(datePicker, submitButton);

        scheduleScene = new Scene(layout, 300, 200);
        stage.setTitle("Schedule Maintenance");
        stage.setScene(scheduleScene);
        stage.show();
    }

    private void insertSchedule(String scheduledDate) {
        try {
            Connection con = DBUtils.establishConnection();
            String query = "INSERT INTO schedule (licensePlate, scheduledDate) VALUES (?, ?)";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, licensePlate);
            statement.setString(2, scheduledDate);

            int rowsAffected = statement.executeUpdate();

            DBUtils.logQuery(username, "Inserting a schedule record for maintenance", query);


            if (rowsAffected > 0) {
                String updateQuery = "UPDATE vehicle SET status = ? WHERE licensePlate = ?";
                PreparedStatement updateStatement = con.prepareStatement(updateQuery);
                updateStatement.setString(1, "Scheduled for S/M");
                updateStatement.setString(2, licensePlate);

                int updateRows = updateStatement.executeUpdate();

                DBUtils.logQuery("System", "Updating the status of the vehicle automatically", updateQuery);


                if (updateRows > 0) {
                    CryptUtils.showAlertS("Success", "Maintenance scheduled and vehicle status updated.");
                } else {
                    CryptUtils.showAlertF("Error", "Failed to update vehicle status.");
                }


                DBUtils.closeConnection(con, statement);
                DBUtils.closeConnection(con, updateStatement);

                MechInterface mechInterface = new MechInterface(stage, username);
                mechInterface.initializeComponents();
            } else {
                CryptUtils.showAlertF("Error", "Failed to schedule maintenance.");
            }

        } catch (SQLException e) {
            CryptUtils.showAlertF("Database Error", "Failed to insert schedule data.");
        }
    }
}
