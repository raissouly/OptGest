package sample;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Connectiondb {

    private static final String url = "jdbc:mysql://localhost/optique";
    private static final String username = "root";
    private static final String password = "";

    private static Connection connection = null;

    public static Connection Connection() {
        try {
            // التأكد من أن الاتصال موجود ومفتوح
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("✅ Connection established.");
            }
        } catch (SQLException ex) {
            System.out.println("❌ Error in Connection() method: " + ex.getMessage());
        }
        return connection;
    }

    public static void Insert(String query) {
        try {
            Statement statement = Connection().createStatement();
            statement.execute(query);
        } catch (SQLException ex) {
            System.out.println("❌ Error in Insert(): " + ex.getMessage());
        }
    }

    public static ResultSet Dispaly(String query) {
        ResultSet result = null;
        try {
            Statement statement = Connection().createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeQuery(query);
        } catch (SQLException ex) {
            System.out.println("❌ Error in Dispaly(): " + ex.getMessage());
        }
        return result;
    }

    public static int Upadate(String query) {
        int i = 0;
        try {
            Statement statement = Connection().createStatement();
            i = statement.executeUpdate(query);
        } catch (SQLException ex) {
            System.out.println("❌ Error in Upadate(): " + ex.getMessage());
        }
        return i;
    }

    public static void Delete(String query) {
        try {
            Statement statement = Connection().createStatement();
            statement.execute(query);
        } catch (SQLException ex) {
            System.out.println("❌ Error in Delete(): " + ex.getMessage());
        }
    }
    public static void ExecuteStatement(String sql) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

}
