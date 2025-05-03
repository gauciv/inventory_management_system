package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class database_utility {
    private static final String database_url = "jdbc:mysql://127.0.0.1:3306/inventory_management_system_database";
    private static final String database_username = "root";
    private static final String database_password = "computerengineering";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(database_url, database_username, database_password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Object[] query(String sql_query, Object... params) {
        try {
            Connection connect = connect();
            PreparedStatement statement = connect.prepareStatement(sql_query);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]); // the '?' in our sqlQuery syntax is being replace by the value of params[i]
            }

            ResultSet result = statement.executeQuery(); // this executes our query and returns rows of data stored in the ResultSet
            return new Object[]{connect, result};

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object[] update(String slq_update, Object[]... params) {
        try {
            Connection connect = connect();
            PreparedStatement statement = connect.prepareStatement(slq_update);
            int result = (int) statement.executeUpdate();
            return new Object[]{connect, result};

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void close(Connection connect) {
        try {
            connect.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
