package src;

import java.sql.*;

public class DBUtils {
    private static String url = "jdbc:mysql://localhost:3306/assignment";
    private static String appUsername = "abdulla";
    private static String appPassword = "DACSpass";

    public static Connection establishConnection(){
        Connection con = null;
        try{
            con = DriverManager.getConnection(url, appUsername, appPassword);
            System.out.println("Connection Successful");
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return con;
    }
    public static void closeConnection(Connection con, Statement stmt){
        try{
            if (stmt != null) {  // Check if the statement is not null before closing
                stmt.close();
            }
            if (con != null) {   // Check if the connection is not null before closing
                con.close();
            }
            System.out.println("Connection is closed");
        }catch(SQLException e){
            System.out.print("Database Error");
        }
    }

    public static void logQuery(String username, String actionType, String query) {
        try (Connection con = establishConnection()) {
            String logQuery = "INSERT INTO audit_log (username, action_type, query_text) VALUES (?, ?, ?)";
            PreparedStatement logStatement = con.prepareStatement(logQuery);
            logStatement.setString(1, username);
            logStatement.setString(2, actionType);
            logStatement.setString(3, query);
            logStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.print("Database Error");
        }
    }

    public static void logQuery(String username, String actionType) {
        try (Connection con = establishConnection()) {
            String logQuery = "INSERT INTO audit_log (username, action_type) VALUES (?, ?)";
            PreparedStatement logStatement = con.prepareStatement(logQuery);
            logStatement.setString(1, username);
            logStatement.setString(2, actionType);
            logStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.print("Database Error");
        }
    }
}

