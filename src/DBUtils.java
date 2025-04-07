package src;

import java.sql.*;
import java.io.InputStream;
import java.util.Properties;

public class DBUtils {
    private static final Properties config = new Properties();

    static {
        try (InputStream input = DBUtils.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            config.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static Connection establishConnection() {
        try {
            return DriverManager.getConnection(
                    config.getProperty("db.url"),
                    config.getProperty("db.user"),
                    config.getProperty("db.password")
            );
        } catch (SQLException e) {
            System.out.println("Connection Error: " + e.getMessage());
            return null;
        }
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

