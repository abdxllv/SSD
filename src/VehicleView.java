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

public class VehicleView {
    private Scene viewVehicleInt;
    private Stage stage;
    private String username;

    public VehicleView(Stage primaryStage, String username) {
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        if (!SessionManager.isValidSession()) {
            UserLogin logIn = new UserLogin(stage);
            CryptUtils.showAlertF("Session Error", "Session has expired.");
            logIn.initializeComponents();
        }

        VBox viewLayout = new VBox(10);
        viewLayout.setPadding(new Insets(10));
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
                try {
                    Connection con = DBUtils.establishConnection();
                    String query = "SELECT role FROM users WHERE username = ?";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setString(1, username);
                    ResultSet rs = statement.executeQuery();

                    if (rs.next()) {
                        String role = rs.getString("role");

                        if ("Clerk".equals(role)) {
                            ClerkInterface clerkInterface = new ClerkInterface(stage, username);
                            clerkInterface.initializeComponents();
                        } else if ("Mechanic".equals(role)) {
                            MechInterface mechInterface = new MechInterface(stage, username);
                            mechInterface.initializeComponents();
                        } else {
                            CryptUtils.showAlertF("Error", "Role not recognized.");
                            UserLogin login = new UserLogin(stage);
                            login.initializeComponents();
                        }
                    }

                    DBUtils.closeConnection(con, statement);
                } catch (SQLException e) {
                    CryptUtils.showAlertF("Database Error", "Failed to retrieve user role.");
                }
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
                        ", Customer ID: "+ customerID);

                HBox userBox;

                Button detailsButton = new Button("Details");
                detailsButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (!SessionManager.renewSession()) {
                            UserLogin logIn = new UserLogin(stage);
                            CryptUtils.showAlertF("Session Error", "Session has expired.");
                            logIn.initializeComponents();
                            return;
                        }
                        VehicleDetail detail = new VehicleDetail(stage, licensePlate, username);
                        detail.initializeComponents();
                    }
                });
                userBox = new HBox(10, userLabel, detailsButton);


                viewLayout.getChildren().add(userBox);
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            CryptUtils.showAlertF("Database Error", "Failed to retrieve user data.");
        }


        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(viewLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);


        VBox finalLayout = new VBox(10);
        finalLayout.setPadding(new Insets(10));
        finalLayout.getChildren().addAll(scrollPane, backButton);


        viewVehicleInt = new Scene(finalLayout, 650, 400);
        stage.setTitle("View All Vehicles");
        stage.setScene(viewVehicleInt);
        stage.show();
    }
}
