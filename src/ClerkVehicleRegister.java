package src;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ClerkVehicleRegister {
    private Scene RegisterScene;
    private TextField licensePlateField = new TextField();
    private TextField makeField = new TextField();
    private TextField modelField = new TextField();
    private TextField customerIDField = new TextField();
    private TextField customerPhoneField = new TextField();


    private Stage stage;
    private String username;

    public ClerkVehicleRegister(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        VBox registerLayout = new VBox(10);
        registerLayout.setPadding(new Insets(10));
        Button backButton = new Button("Back");
        Button registerButton = new Button("Register Vehicle");


        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ClerkInterface clerkInterface = new ClerkInterface(stage, username);
                clerkInterface.initializeComponents();
            }
        });
        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                register();
            }
        });

        registerLayout.getChildren().addAll(
                new Label("License Plate:"), licensePlateField,
                new Label("Vehicle Make"), makeField,
                new Label("Vehicle Model"), modelField,
                new Label("Customer ID"), customerIDField,
                new Label("Customer Phone"), customerPhoneField,
                registerButton,
                new Label("or"), backButton
        );

        RegisterScene = new Scene(registerLayout, 300, 415);
        stage.setTitle("Vehicle Registration");
        stage.setScene(RegisterScene);
        stage.show();
    }

    private void register() {
        String licensePlate = licensePlateField.getText();
        String make = makeField.getText();
        String model = modelField.getText();
        String customerID = customerIDField.getText();
        String customerPhone = customerPhoneField.getText();


        if (!CryptUtils.isValidQatariLicensePlate(licensePlate)) {
            CryptUtils.showAlertF("Invalid License Plate", "A Qatari number plate can only have digits and 6 characters long");
            return;
        }

        if (!CryptUtils.isValidName(make)) {
            CryptUtils.showAlertF("Invalid make", "Make can only contain letters");
            return;
        }

        if (!CryptUtils.isValidName(model)) {
            CryptUtils.showAlertF("Invalid model", "Model can only contain letters");
            return;
        }

        if (!CryptUtils.isValidQID(customerID)) {
            CryptUtils.showAlertF("Invalid ID", "Qatar ID must have exactly 11 digits");
            return;
        }

        if (!CryptUtils.isValidQatariPhone(customerPhone)) {
            CryptUtils.showAlertF("Invalid Phone", "Please enter a valid Qatari phone number.\n" +
                    "Formats accepted: +974XXXXXXXX, 00974XXXXXXXX, XXXXXXXX, XXXX-XXXX");
            return;
        }


        Connection con = DBUtils.establishConnection();
        String query = "INSERT INTO `vehicle` (`licensePlate`, `make`, `model`, `customerID`, `customerPhone`) VALUES (?, ?, ?, ?, ?);";

        try {
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, licensePlate);
            statement.setString(2, make);
            statement.setString(3, model);
            statement.setString(4, customerID);
            statement.setString(5, customerPhone);
            int rs = statement.executeUpdate();

            DBUtils.logQuery(username, "Log Out", query);

            if (rs==1) {
                ClerkInterface clerkInterface = new ClerkInterface(stage, username);
                clerkInterface.initializeComponents();
            } else {
                CryptUtils.showAlertF("Registration Failed", "Username Unavailable.");
            }
            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            e.printStackTrace();
            CryptUtils.showAlertF("Database Error", "Failed to register.");
        }
    }
}
