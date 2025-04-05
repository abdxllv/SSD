package src;

import java.security.NoSuchAlgorithmException;
import java.sql.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserChangePassword {
    private Scene changePasswordScene;
    private PasswordField newPasswordField = new PasswordField();
    private PasswordField confirmNewPasswordField = new PasswordField();
    private Stage stage;
    private String username;

    public UserChangePassword(Stage primaryStage, String username){
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        VBox changePasswordLayout = new VBox(10);
        changePasswordLayout.setPadding(new Insets(10));
        Button changePasswordButton = new Button("Change Password");
        Button logOutButton = new Button("Logout");

        changePasswordButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                changePassword();
            }
        });

        logOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                UserLogin logIn = new UserLogin(stage);
                logIn.initializeComponents();
            }
        });


        changePasswordLayout.getChildren().addAll(new Label("New password for " + username),
                new Label("New Password:"), newPasswordField,
                new Label("Confirm New Password:"), confirmNewPasswordField, changePasswordButton,
                new Label("or"), logOutButton);

        changePasswordScene = new Scene(changePasswordLayout, 300, 300);
        stage.setTitle("Change Password");
        stage.setScene(changePasswordScene);
        stage.show();
    }

    private void changePassword(){
        String newPassword = newPasswordField.getText().trim();
        String confirmNewPassword = confirmNewPasswordField.getText().trim();

        if (!newPassword.equals(confirmNewPassword)){
            CryptUtils.showAlertF("Error", "Passwords do not match.");
            return;
        }

        if (newPassword.isEmpty()){
            CryptUtils.showAlertF("Error", "Password cannot be empty. Please enter a valid password");
            return;
        }

        if(!(CryptUtils.isValidPassword(newPassword))){
            CryptUtils.showAlertF("Error", "Password should be atleast 6 characters, with atleast one lowercase, one uppercase and one special character!");
            return;
        }

        String hashedPassword = "";
        byte[] salt = CryptUtils.createSalt();
        try{
            hashedPassword = CryptUtils.generateHash(newPassword,salt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        Connection con = DBUtils.establishConnection();
        String query = "UPDATE users SET password=?, salt=? WHERE username =?;";
        try{
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, hashedPassword);
            statement.setBytes(2, salt);
            statement.setString(3, username);
            int result = statement.executeUpdate();
            DBUtils.logQuery(username, "Password Changed", query);
            if (result == 1) {
                CryptUtils.showAlertS("Success", "Password successfully changed");

                UserLogin userLogin = new UserLogin(stage);
                userLogin.initializeComponents();

            } else {
                CryptUtils.showAlertF("Failure", "Failed to update password");
            }
            DBUtils.closeConnection(con, statement);
        }catch(Exception e){
            CryptUtils.showAlertF("Database Error", "Failed to connect to the database.");
        }
    }


}
