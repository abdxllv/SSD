package src;

import java.sql.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VehicleDetail {
    private Scene vehicleDetailScene;
    private Stage stage;
    private String username;
    private String licensePlate;

    public VehicleDetail(Stage primaryStage, String licensePlate, String username) {
        this.stage = primaryStage;
        this.licensePlate = licensePlate;
        this.username = username;
    }

    public void initializeComponents() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button backButton = new Button("Back");
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                VehicleView vehicleView = new VehicleView(stage, username);
                vehicleView.initializeComponents();
            }
        });

        try {
            Connection con = DBUtils.establishConnection();
            String query = "SELECT make, model, status, customerID, customerPhone FROM vehicle WHERE licensePlate = ?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, licensePlate);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String make = rs.getString("make");
                String model = rs.getString("model");
                String status = rs.getString("status");
                String customerID = rs.getString("customerID");
                String customerPhone = rs.getString("customerPhone");

                Label licensePlateLabel = new Label("License Plate: " + licensePlate);
                Label makeLabel = new Label("Make: " + make);
                Label modelLabel = new Label("Model: " + model);
                Label statusLabel = new Label("Status: " + status);
                Label customerIDLabel = new Label("Customer ID: " + customerID);
                Label customerPhoneLabel = new Label("Customer Phone: " + customerPhone);

                layout.getChildren().addAll(licensePlateLabel, makeLabel, modelLabel, statusLabel, customerIDLabel, customerPhoneLabel);
            } else {
                Label noDataLabel = new Label("No details available for this vehicle.");
                layout.getChildren().add(noDataLabel);
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            e.printStackTrace();
            HashUtils.showAlertF("Database Error", "Failed to retrieve vehicle details.");
        }

        layout.getChildren().add(backButton);
        vehicleDetailScene = new Scene(layout, 400, 400);
        stage.setTitle("Vehicle Details");
        stage.setScene(vehicleDetailScene);
        stage.show();
    }
}
