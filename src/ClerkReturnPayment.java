package src;

import java.sql.*;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ClerkReturnPayment {
    private Scene paymentScene;
    private Stage stage;
    private String username;
    private String licensePlate;

    public ClerkReturnPayment(Stage primaryStage, String username, String licensePlate) {
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

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            if (!SessionManager.renewSession()) {
                UserLogin logIn = new UserLogin(stage);
                CryptUtils.showAlertF("Session Error", "Session has expired.");
                logIn.initializeComponents();
                return;
            }
            ClerkInterface clerkInterface = new ClerkInterface(stage, username);
            clerkInterface.initializeComponents();
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox invoiceLayout = new VBox(10);
        scrollPane.setContent(invoiceLayout);

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            con = DBUtils.establishConnection();
            String query = "SELECT id, amount, paymentStatus, payment_method FROM invoice WHERE licensePlate = ? AND paymentStatus = false";
            statement = con.prepareStatement(query);
            statement.setString(1, licensePlate);
            rs = statement.executeQuery();
            DBUtils.logQuery(username, "Select to show all unpaid invoices", query);

            while (rs.next()) {
                int invoiceId = rs.getInt("id");
                double amount = rs.getDouble("amount");

                HBox invoiceBox = new HBox(10);
                Label invoiceLabel = new Label(String.format("Invoice ID: %d, Amount: %.2f, Paid: No", invoiceId, amount));

                ComboBox<String> paymentMethodComboBox = new ComboBox<>();
                paymentMethodComboBox.getItems().addAll("Cash", "Card");

                Button processPaymentButton = new Button("Process Payment");
                processPaymentButton.setOnAction(e -> {
                    if (!SessionManager.renewSession()) {
                        UserLogin logIn = new UserLogin(stage);
                        CryptUtils.showAlertF("Session Error", "Session has expired.");
                        logIn.initializeComponents();
                        return;
                    }
                    if (paymentMethodComboBox.getValue() == null) {
                        CryptUtils.showAlertF("Error", "Please select a payment method.");
                    } else {
                        processPayment(invoiceId, paymentMethodComboBox.getValue());
                    }
                });

                invoiceBox.getChildren().addAll(invoiceLabel, paymentMethodComboBox, processPaymentButton);
                invoiceLayout.getChildren().add(invoiceBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CryptUtils.showAlertF("Database Error", "Failed to retrieve invoice data.");
        } finally {
            DBUtils.closeConnection(con, statement);
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        layout.getChildren().addAll(scrollPane, backButton);
        paymentScene = new Scene(layout, 650, 400);
        stage.setTitle("Process Payments for " + licensePlate);
        stage.setScene(paymentScene);
        stage.show();
    }

    private void processPayment(int invoiceId, String paymentMethod) {
        Connection con = null;
        PreparedStatement checkStatement = null;
        PreparedStatement updateStatement = null;
        PreparedStatement insertHistoryStatement = null;
        PreparedStatement deleteVehicleStatement = null;
        PreparedStatement checkAllPaidStatement = null;
        ResultSet rs = null;
        ResultSet paidRs = null;

        try {
            con = DBUtils.establishConnection();

            // Check payment status of current invoice
            String checkQuery = "SELECT paymentStatus, licensePlate, amount FROM invoice WHERE id = ?";
            checkStatement = con.prepareStatement(checkQuery);
            checkStatement.setInt(1, invoiceId);
            rs = checkStatement.executeQuery();

            DBUtils.logQuery(username, "Checking payment status of an invoice", checkQuery);

            if (rs.next()) {
                boolean paymentStatus = rs.getBoolean("paymentStatus");
                String licensePlate = rs.getString("licensePlate");
                double amount = rs.getDouble("amount");

                if (!paymentStatus) {
                    // Update invoice
                    String updateQuery = "UPDATE invoice SET paymentStatus = ?, payment_method = ? WHERE id = ?";
                    updateStatement = con.prepareStatement(updateQuery);
                    updateStatement.setBoolean(1, true);
                    updateStatement.setString(2, paymentMethod);
                    updateStatement.setInt(3, invoiceId);

                    if (updateStatement.executeUpdate() > 0) {
                        DBUtils.logQuery(username, "Updating paymentStatus and paymentMethod of an invoice", updateQuery);

                        String insertHistoryQuery = "INSERT INTO payment_history (id, licensePlate, amount, paymentStatus, handler, payment_method) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
                        insertHistoryStatement = con.prepareStatement(insertHistoryQuery);
                        insertHistoryStatement.setInt(1, invoiceId);
                        insertHistoryStatement.setString(2, licensePlate);
                        insertHistoryStatement.setDouble(3, amount);
                        insertHistoryStatement.setBoolean(4, true);
                        insertHistoryStatement.setString(5, username);
                        insertHistoryStatement.setString(6, paymentMethod);
                        insertHistoryStatement.executeUpdate();

                        DBUtils.logQuery(username, "Inserting into payment_history for payment records", insertHistoryQuery);


                        // Check if all invoices for this vehicle are paid
                        String checkAllPaidQuery = "SELECT COUNT(*) AS unpaidCount FROM invoice WHERE licensePlate = ? AND paymentStatus = false";
                        checkAllPaidStatement = con.prepareStatement(checkAllPaidQuery);
                        checkAllPaidStatement.setString(1, licensePlate);
                        paidRs = checkAllPaidStatement.executeQuery();

                        DBUtils.logQuery(username, "Checking if there is any invoices unpaid still", checkAllPaidQuery);

                        if (paidRs.next()) {
                            int unpaidCount = paidRs.getInt("unpaidCount");

                            if (unpaidCount == 0) {
                                // All invoices paid - delete vehicle
                                String deleteVehicleQuery = "DELETE FROM vehicle WHERE licensePlate = ?";
                                deleteVehicleStatement = con.prepareStatement(deleteVehicleQuery);
                                deleteVehicleStatement.setString(1, licensePlate);

                                if (deleteVehicleStatement.executeUpdate() > 0) {
                                    DBUtils.logQuery(username, "Vehicle payments cleared and returned, therefore vehicle is being erased from table", deleteVehicleQuery);

                                    CryptUtils.showAlertS("Success", "All payments processed and vehicle deleted successfully.");
                                    new ClerkInterface(stage, username).initializeComponents();
                                } else {
                                    CryptUtils.showAlertF("Error", "Failed to delete the vehicle.");
                                }
                            } else {
                                // Not all invoices paid - refresh UI
                                CryptUtils.showAlertS("Success", "Payment processed successfully. " + unpaidCount + " invoice(s) remaining.");
                                DBUtils.logQuery(username, "Vehicle Return Payment Menu");
                                new ClerkReturnPayment(stage, username, licensePlate).initializeComponents();
                            }
                        }
                    } else {
                        CryptUtils.showAlertF("Error", "Failed to process the payment.");
                    }
                } else {
                    CryptUtils.showAlertF("Error", "This invoice is already paid.");
                }
            } else {
                CryptUtils.showAlertF("Error", "Invoice not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CryptUtils.showAlertF("Database Error", "Failed to process the payment.");
        } finally {
            // Close all resources using DBUtils
            DBUtils.closeConnection(con, checkStatement);
            try {
                if (rs != null) rs.close();
                if (paidRs != null) paidRs.close();
                if (updateStatement != null) updateStatement.close();
                if (insertHistoryStatement != null) insertHistoryStatement.close();
                if (deleteVehicleStatement != null) deleteVehicleStatement.close();
                if (checkAllPaidStatement != null) checkAllPaidStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}