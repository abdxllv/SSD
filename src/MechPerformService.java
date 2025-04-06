package src;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import src.src.CryptUtils;
import src.src.DBUtils;
import src.src.MechInterface;
import src.src.SessionManager;
import src.src.UserLogin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MechPerformService {
    private Stage stage;
    private String username;
    private String licensePlate;
    private Scene inputScene;
    private VBox formLayout;
    private List<ComboBox<String>> partsComboBoxes = new ArrayList<>();
    private List<TextField> quantityFields = new ArrayList<>();
    private List<String> partsUsedList = new ArrayList<>();

    public MechPerformService(Stage stage, String username, String licensePlate) {
        this.stage = stage;
        this.username = username;
        this.licensePlate = licensePlate;
    }

    public void initializeComponents() {
        if (!src.src.SessionManager.isValidSession()) {
            src.src.UserLogin logIn = new src.src.UserLogin(stage);
            src.src.CryptUtils.showAlertF("Session Error", "Session has expired.");
            logIn.initializeComponents();
        }

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label askLabel = new Label("How many services/maintenance were performed?");
        TextField countField = new TextField();
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
                src.src.MechInterface mechInterface = new src.src.MechInterface(stage, username);
                mechInterface.initializeComponents();
            }
        });

        Button nextButton = new Button("Next");

        nextButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!src.src.SessionManager.renewSession()) {
                    src.src.UserLogin logIn = new src.src.UserLogin(stage);
                    src.src.CryptUtils.showAlertF("Session Error", "Session has expired.");
                    logIn.initializeComponents();
                    return;
                }
                try {
                    int count = Integer.parseInt(countField.getText());
                    if (count > 0) {
                        createForms(count);
                    } else {
                        src.src.CryptUtils.showAlertF("Invalid Input", "Please enter a number greater than 0.");
                    }
                } catch (NumberFormatException e) {
                    src.src.CryptUtils.showAlertF("Invalid Input", "Please enter a valid integer.");
                }
            }
        });

        layout.getChildren().addAll(askLabel, countField, nextButton, backButton);
        inputScene = new Scene(layout, 400, 150);
        stage.setTitle("Service Count Input");
        stage.setScene(inputScene);
        stage.show();
    }

    private void createForms(int count) {
        formLayout = new VBox(10);
        formLayout.setPadding(new Insets(10));

        List<TextField> descFields = new ArrayList<>();
        List<TextField> amountFields = new ArrayList<>();


        List<String> availableParts = fetchSpareParts();
        availableParts.add(0, "None");

        for (int i = 0; i < count; i++) {
            Label label = new Label("Service/Maintenance " + (i + 1));
            TextField desc = new TextField();
            desc.setPromptText("Service Description");
            TextField amount = new TextField();
            amount.setPromptText("Amount");

            ComboBox<String> partsComboBox = new ComboBox<>();
            partsComboBox.getItems().addAll(availableParts);
            partsComboBox.setPromptText("Select Part");


            TextField quantityField = new TextField();
            quantityField.setPromptText("Quantity Used");

            descFields.add(desc);
            amountFields.add(amount);
            partsComboBoxes.add(partsComboBox);
            quantityFields.add(quantityField);

            formLayout.getChildren().addAll(label, desc, partsComboBox, quantityField, amount);
        }

        Button submitButton = new Button("Submit All");
        submitButton.setOnAction(event -> {
            if (!src.src.SessionManager.renewSession()) {
                src.src.UserLogin logIn = new src.src.UserLogin(stage);
                src.src.CryptUtils.showAlertF("Session Error", "Session has expired.");
                logIn.initializeComponents();
                return;
            }
            Connection con = null;
            PreparedStatement statement = null;
            try {
                con = src.src.DBUtils.establishConnection();
                boolean validSubmission = true;
                List<String> partsUsed = new ArrayList<>();
                List<Integer> quantitiesUsed = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    String desc = descFields.get(i).getText();
                    String amountText = amountFields.get(i).getText();
                    String selectedPart = partsComboBoxes.get(i).getValue();
                    String quantityText = quantityFields.get(i).getText();

                    if (desc.isEmpty() || amountText.isEmpty() || (selectedPart == null || selectedPart.isEmpty())) {
                        src.src.CryptUtils.showAlertF("Validation Error", "Please enter valid values for service " + (i + 1));
                        return;
                    }
                    if ((selectedPart.equals("None") && !quantityText.isEmpty())){
                        src.src.CryptUtils.showAlertF("Validation Error", "If no parts are used, keep quantity used field empty, for service " + (i + 1));
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(amountText);
                        Integer quantityUsed = selectedPart.equals("None") ? 0 : Integer.parseInt(quantityText);


                        if (!selectedPart.equals("None")) {
                            int availableStock = getPartStock(con, selectedPart);
                            if (availableStock < quantityUsed) {
                                src.src.CryptUtils.showAlertF("Stock Error", "Not enough stock for " + selectedPart);
                                validSubmission = false;
                                return;
                            }


                            partsUsed.add(selectedPart);
                            quantitiesUsed.add(quantityUsed);

                            // Insert the service data
                            insertServiceAndInvoice(con, desc, selectedPart, quantityUsed, amount);
                        } else {
                            // For "None", insert with null in partsUsed column
                            insertServiceAndInvoice(con, desc, null, 0, amount);
                        }

                    } catch (NumberFormatException e) {
                        src.src.CryptUtils.showAlertF("Amount/Quantity Error", "Please enter valid values for service " + (i + 1));
                        return;
                    }
                }

                if (validSubmission) {
                    for (int i = 0; i < partsUsed.size(); i++) {
                        decrementPartStock(con, partsUsed.get(i), quantitiesUsed.get(i));
                    }


                    deleteFromScheduleTable(con);


                    updateVehicleStatus(con);

                    src.src.CryptUtils.showAlertS("Success", "All services recorded successfully. Vehicle is ready for pickup.");

                    src.src.MechInterface mechInterface = new MechInterface(stage, username);
                    mechInterface.initializeComponents();
                }

            } catch (Exception e) {
                e.printStackTrace();
                src.src.CryptUtils.showAlertF("Database Error", "Failed to save service or invoice.");
            } finally {
                src.src.DBUtils.closeConnection(con, statement);
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            if (!SessionManager.renewSession()) {
                src.src.UserLogin logIn = new UserLogin(stage);
                src.src.CryptUtils.showAlertF("Session Error", "Session has expired.");
                logIn.initializeComponents();
                return;
            }
            MechPerformList mechPerformList = new MechPerformList(stage, username);
            mechPerformList.initializeComponents();
        });

        formLayout.getChildren().addAll(submitButton, backButton);


        ScrollPane scrollPane = new ScrollPane(formLayout);
        scrollPane.setFitToWidth(true);

        Scene formScene = new Scene(scrollPane, 500, 400);
        stage.setScene(formScene);
        stage.setTitle("Enter Service Details");
        stage.show();
    }


    private void insertServiceAndInvoice(Connection con, String description, String partUsed, int quantityUsed, double amount) {
        PreparedStatement statement = null;
        try {
            // Service Query
            String serviceQuery = "INSERT INTO service_history (licensePlate, mechanicUsername, serviceDescription, partsUsed, amount) VALUES (?, ?, ?, ?, ?)";
            statement = con.prepareStatement(serviceQuery);
            statement.setString(1, licensePlate);
            statement.setString(2, username);
            statement.setString(3, description);
            statement.setString(4, partUsed != null ? partUsed + " (x" + quantityUsed + ")" : null); // Null if "None" is selected
            statement.setDouble(5, amount);
            statement.executeUpdate();

            src.src.DBUtils.logQuery(username, "Inserting a service record for a vehicle", serviceQuery);


            // Invoice Query
            String invoiceQuery = "INSERT INTO invoice (licensePlate, paymentStatus, amount) VALUES (?, ?, ?)";
            statement = con.prepareStatement(invoiceQuery);
            statement.setString(1, licensePlate);
            statement.setBoolean(2, false);
            statement.setDouble(3, amount);
            statement.executeUpdate();

            src.src.DBUtils.logQuery(username, "making an invoice for a vehicle service/maintenance", invoiceQuery);


        } catch (SQLException e) {
            e.printStackTrace();
            src.src.CryptUtils.showAlertF("Database Error", "Failed to save service or invoice.");
        }
    }

    private List<String> fetchSpareParts() {
        List<String> parts = new ArrayList<>();
        Connection con = null;
        Statement stmt = null;
        try {
            con = src.src.DBUtils.establishConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM spare_parts_inventory");

            while (rs.next()) {
                parts.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parts;
    }

    private int getPartStock(Connection con, String partName) {
        int stock = 0;
        PreparedStatement statement = null;
        try {
            String query = "SELECT quantityInStock FROM spare_parts_inventory WHERE name = ?";
            statement = con.prepareStatement(query);
            statement.setString(1, partName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                stock = rs.getInt("quantityInStock");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stock;
    }

    private void decrementPartStock(Connection con, String partName, int quantityUsed) {
        PreparedStatement statement = null;
        try {
            String updateQuery = "UPDATE spare_parts_inventory SET quantityInStock = quantityInStock - ? WHERE name = ?";
            statement = con.prepareStatement(updateQuery);
            statement.setInt(1, quantityUsed);
            statement.setString(2, partName);
            statement.executeUpdate();

            src.src.DBUtils.logQuery("System", "decrementing quantity of a spare part", updateQuery);



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void deleteFromScheduleTable(Connection con) {
        PreparedStatement statement = null;
        try {
            String deleteQuery = "DELETE FROM schedule WHERE licensePlate = ?";
            statement = con.prepareStatement(deleteQuery);
            statement.setString(1, licensePlate);
            statement.executeUpdate();

            src.src.DBUtils.logQuery("System", "Deleting the vehicle entry from schedule", deleteQuery);


            System.out.println("Entry deleted from schedule table.");
        } catch (SQLException e) {
            e.printStackTrace();
            src.src.CryptUtils.showAlertF("Database Error", "Failed to delete entry from schedule table.");
        }
    }

    private void updateVehicleStatus(Connection con) {
        PreparedStatement statement = null;
        try {
            String updateQuery = "UPDATE vehicle SET status = ? WHERE licensePlate = ?";
            statement = con.prepareStatement(updateQuery);
            statement.setString(1, "Ready for Pickup");
            statement.setString(2, licensePlate);
            statement.executeUpdate();

            DBUtils.logQuery("System", "Updating the status of the vehicle automatically", updateQuery);


            System.out.println("Vehicle status updated to 'Ready for Pickup'.");
        } catch (SQLException e) {
            e.printStackTrace();
            CryptUtils.showAlertF("Database Error", "Failed to update vehicle status.");
        }
    }
}
