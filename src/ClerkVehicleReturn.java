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

public class ClerkVehicleReturn {
    private Scene viewVehicleReturnInt;
    private Stage stage;
    private String username;

    public ClerkVehicleReturn(Stage primaryStage, String username) {
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
                ClerkInterface clerkInterface = new ClerkInterface(stage, username);
                clerkInterface.initializeComponents();
            }
        });

        try {
            Connection con = DBUtils.establishConnection();
            String query = "SELECT licensePlate, make, model, status, customerID FROM vehicle";
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String licensePlate = rs.getString("licensePlate");
                String make = rs.getString("make");
                String model = rs.getString("model");
                String status = rs.getString("status");
                String customerID = rs.getString("customerID");
                Label userLabel = new Label("License Plate: " + licensePlate +
                        ", Vehicle: " + make + " " + model +
                        ", Status: " + status +
                        ", Customer QID: "+ customerID);

                HBox userBox;

                Button returnButton = new Button("Return");
                returnButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        returnVehicle(licensePlate);
                    }
                });

                userBox = new HBox(10, userLabel, returnButton);
                viewLayout.getChildren().add(userBox);
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            e.printStackTrace();
            CryptUtils.showAlertF("Database Error", "Failed to retrieve vehicle data.");
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(viewLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox finalLayout = new VBox(10);
        finalLayout.setPadding(new Insets(10));
        finalLayout.getChildren().addAll(scrollPane, backButton);

        viewVehicleReturnInt = new Scene(finalLayout, 650, 400);
        stage.setTitle("View All Vehicles to Return");
        stage.setScene(viewVehicleReturnInt);
        stage.show();
    }

    private void returnVehicle(String licensePlate) {
        try {
            Connection con = DBUtils.establishConnection();

            // First, check if the vehicle status is "Ready for Pickup"
            String statusQuery = "SELECT status FROM vehicle WHERE licensePlate = ?";
            PreparedStatement statusStatement = con.prepareStatement(statusQuery);
            statusStatement.setString(1, licensePlate);
            ResultSet statusRs = statusStatement.executeQuery();
            DBUtils.logQuery(username, "Checking status of vehicle for return process", statusQuery);
            if (statusRs.next()) {
                String status = statusRs.getString("status");

                if ("Ready for Pickup".equals(status)) {
                    String invoiceQuery = "SELECT paymentStatus FROM invoice WHERE licensePlate = ?";
                    PreparedStatement invoiceStatement = con.prepareStatement(invoiceQuery);
                    invoiceStatement.setString(1, licensePlate);
                    ResultSet rs = invoiceStatement.executeQuery();
                    DBUtils.logQuery(username, "Checking status of vehicle for return process", invoiceQuery);

                    boolean allPaid = true; // Flag to check if all invoices are paid

                    while (rs.next()) {
                        boolean paymentStatus = rs.getBoolean("paymentStatus");
                        if (!paymentStatus) {
                            allPaid = false; // If any invoice is unpaid, set the flag to false
                            break;
                        }
                    }

                    if (!allPaid) {
                        // If not all invoices are paid, navigate to the ClerkReturnPayment form
                        ClerkReturnPayment clerkReturnPayment = new ClerkReturnPayment(stage, username, licensePlate);
                        DBUtils.logQuery(username, "Vehicle Return Payment Menu");
                        clerkReturnPayment.initializeComponents();
                    } else {
                        // If all invoices are paid, proceed with deleting the vehicle
                        String deleteQuery = "DELETE FROM vehicle WHERE licensePlate = ?";
                        PreparedStatement deleteStatement = con.prepareStatement(deleteQuery);
                        deleteStatement.setString(1, licensePlate);
                        int rowsAffected = deleteStatement.executeUpdate();

                        DBUtils.logQuery(username, "Vehicle payments cleared and returned, therefor vehicle is being erased from table", deleteQuery);

                        DBUtils.closeConnection(con, deleteStatement);

                        if (rowsAffected > 0) {
                            CryptUtils.showAlertS("Success", "Vehicle returned successfully.");
                            DBUtils.logQuery(username, "Vehicle Return Menu");
                            new ClerkVehicleReturn(stage, username).initializeComponents(); // Refresh UI
                        } else {
                            CryptUtils.showAlertF("Error", "Failed to return vehicle.");
                        }
                    }

                    DBUtils.closeConnection(con, invoiceStatement);

                } else {
                    CryptUtils.showAlertF("Error", "This vehicle is not ready for return.");
                }
            } else {
                CryptUtils.showAlertF("Error", "Vehicle not found.");
            }

            DBUtils.closeConnection(con, statusStatement);

        } catch (Exception e) {
            e.printStackTrace();
            CryptUtils.showAlertF("Database Error", "Failed to return vehicle.");
        }
    }

}
