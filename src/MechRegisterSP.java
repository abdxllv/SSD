package src;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class MechRegisterSP {
    private Stage stage;
    private String username;

    public MechRegisterSP(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        if (!SessionManager.isValidSession()) {
            UserLogin logIn = new UserLogin(stage);
            CryptUtils.showAlertF("Session Error", "Session has expired.");
            logIn.initializeComponents();
        }

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));


        TextField partNameField = new TextField();
        partNameField.setPromptText("Enter Part Name");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter Quantity");


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
                MechInterface mechInterface = new MechInterface(stage, username);
                mechInterface.initializeComponents();
            }
        });


        Button submitButton = new Button("Register Part");
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!SessionManager.renewSession()) {
                    UserLogin logIn = new UserLogin(stage);
                    CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                String partName = partNameField.getText();
                String quantityText = quantityField.getText();

                if (partName.isEmpty() || quantityText.isEmpty()) {
                    CryptUtils.showAlertF("Error", "All fields must be filled.");
                    return;
                }

                try {
                    int quantity = Integer.parseInt(quantityText);

                    if (quantity < 0) {
                        CryptUtils.showAlertF("Error", "Quantity cannot be negative.");
                        return;
                    }

                    Connection con = DBUtils.establishConnection();
                    String checkQuery = "SELECT * FROM spare_parts_inventory WHERE LOWER(name) = LOWER(?)";
                    PreparedStatement checkStatement = con.prepareStatement(checkQuery);
                    checkStatement.setString(1, partName.trim());
                    ResultSet rs = checkStatement.executeQuery();

                    if (rs.next()) {
                        // if part exists, increment the quantity
                        int currentQuantity = rs.getInt("quantityInStock");
                        int newQuantity = currentQuantity + quantity;

                        // udate the part's quantity in the inventory
                        String updateQuery = "UPDATE spare_parts_inventory SET quantityInStock = ? WHERE name = ?";
                        PreparedStatement updateStatement = con.prepareStatement(updateQuery);
                        updateStatement.setInt(1, newQuantity);
                        updateStatement.setString(2, partName);
                        updateStatement.executeUpdate();

                        DBUtils.logQuery(username, "Incrementing a part", updateQuery);

                        CryptUtils.showAlertS("Success", "Part quantity updated successfully.");
                    } else {
                        // insert new part into the inventory if it doesn't exist
                        String insertQuery = "INSERT INTO spare_parts_inventory (name, quantityInStock) VALUES (?, ?)";
                        PreparedStatement insertStatement = con.prepareStatement(insertQuery);
                        insertStatement.setString(1, partName);
                        insertStatement.setInt(2, quantity);
                        insertStatement.executeUpdate();

                        DBUtils.logQuery(username, "Inserting a part and its quantity", insertQuery);

                        CryptUtils.showAlertS("Success", "Spare part registered successfully.");
                    }

                    DBUtils.closeConnection(con, checkStatement);
                } catch (NumberFormatException e) {
                    CryptUtils.showAlertF("Error", "Quantity must be a valid number.");
                } catch (Exception e) {
                    e.printStackTrace();
                    CryptUtils.showAlertF("Database Error", "Failed to register the spare part.");
                }
            }
        });

        layout.getChildren().addAll(partNameField, quantityField, submitButton, backButton);

        Scene scene = new Scene(layout, 300, 200);
        stage.setTitle("Register Spare Part");
        stage.setScene(scene);
        stage.show();
    }
}
