package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class database_utility {    private static final String database_url = String.format("jdbc:mysql://%s:%s/%s",
            System.getenv().getOrDefault("DB_HOST", "127.0.0.1"),
            System.getenv().getOrDefault("DB_PORT", "3307"),
            System.getenv().getOrDefault("DB_NAME", "inventory_management_system_database"));
    private static final String database_username = System.getenv().getOrDefault("DB_USER", "root");
    private static final String database_password = System.getenv().getOrDefault("DB_PASS", "computerengineering");

    static {
        try {
            // Register the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        }
    }

    public static Connection connect() {
        try {
            Connection connection = DriverManager.getConnection(database_url, database_username, database_password);
            if (connection == null) {
                System.err.println("Database connection failed");
            }
            return connection;
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Object[] query(String sql_query, Object... params) {
        Connection connect = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try {
            connect = connect();
            if (connect == null) {
                throw new Exception("Could not establish database connection");
            }

            statement = connect.prepareStatement(sql_query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            result = statement.executeQuery();
            return new Object[]{connect, result};

        } catch (Exception e) {
            System.err.println("Database query error: " + e.getMessage());
            e.printStackTrace();
            if (connect != null) {
                close(connect);
            }
            return null;
        }
    }

    public static Object[] update(String sql_update, Object... params) {
        Connection connect = null;
        PreparedStatement statement = null;
        
        try {
            connect = connect();
            if (connect == null) {
                throw new Exception("Could not establish database connection");
            }

            statement = connect.prepareStatement(sql_update);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            int result = statement.executeUpdate();
            return new Object[]{connect, result};

        } catch (Exception e) {
            System.err.println("Database update error: " + e.getMessage());
            e.printStackTrace();
            if (connect != null) {
                close(connect);
            }
            return null;
        }
    }

    public static void close(Connection connect) {
        try {
            if (connect != null && !connect.isClosed()) {
                connect.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
