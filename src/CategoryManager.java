package src;

import java.sql.*;
import java.util.ArrayList;


public class CategoryManager {
    public static void printSQLException(SQLException e) {
        e.printStackTrace(System.err);
        System.err.println("SQLState: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
        System.err.println("Message: " + e.getMessage());
    }


    Connection dbConn;
    public CategoryManager() {
        // Creating a database connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/budgetApp?user=michael&password=clinesworth"
            );
        } catch (SQLException e) {
            printSQLException(e);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        } 
    }


    public int getNumCategories() {
        // Using count(*) to get the total number of spending categories
        String queryStr = "SELECT COUNT(*) FROM category AS numCats";
        try (Statement stmt = dbConn.createStatement()) {
            ResultSet rs = stmt.executeQuery(queryStr);
            rs.next();
            int numCats = rs.getInt("numCats");
            return numCats;
        } catch (SQLException e) {
            printSQLException(e);
        }

        return -1;
    }


    // Creating an inner class to hold category records as single object
    public class CatRecord {
        String name;
        double budgeted;
        double spent;

        public CatRecord(String name, double budget, double spent) {
            this.name = name;
            this.budgeted = budget;
            this.spent = spent;
        }
    }


    // Querying for all records, putting them in an ArrayList and returning it
    public ArrayList<CatRecord> getRecords() {
        String queryStr = "SELECT * FROM category";
        ArrayList<CatRecord> recs = null;

        try (Statement stmt = dbConn.createStatement()) {
            ResultSet rs = stmt.executeQuery(queryStr);
            while (rs.next()) {
                String name = rs.getString("name");
                double budgeted = rs.getDouble("budgeted");
                double spent = rs.getDouble("spent");

                CatRecord currRec = new CatRecord(name, budgeted, spent);
                recs.add(currRec);
            }

            return recs;
        } catch (SQLException e) {
            printSQLException(e);
        }

        return recs;
    }
}
