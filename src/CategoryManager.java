package src;

import java.sql.*;
import java.util.ArrayList;


public class CategoryManager {
    Connection dbConn;
    public CategoryManager() throws SQLException {
        // Creating a database connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/budgetApp?user=michael&password=clinesworth"
            );
        /*} catch (SQLException e) {
            printSQLException(e);
            System.exit(1);*/
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }


    public int getNumCategories() throws SQLException {
        // Using count(*) to get the total number of spending categories
        String queryStr = "SELECT COUNT(*) AS numCats FROM category;";
        //try (Statement stmt = dbConn.createStatement()) {
            Statement stmt = dbConn.createStatement();
            ResultSet rs = stmt.executeQuery(queryStr);
            rs.next();
            int numCats = rs.getInt("numCats");
            return numCats;
        /*} catch (SQLException e) {
            printSQLException(e);
        }*/

        //return -1;
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
    public ArrayList<CatRecord> getRecords() throws SQLException {
        String queryStr = "SELECT * FROM category";
        ArrayList<CatRecord> recs = new ArrayList<CatRecord>();

        //try (Statement stmt = dbConn.createStatement()) {
            Statement stmt = dbConn.createStatement();
            ResultSet rs = stmt.executeQuery(queryStr);
            while (rs.next()) {
                String name = rs.getString("name");
                double budgeted = rs.getDouble("budgeted");
                double spent = rs.getDouble("spent");

                CatRecord currRec = new CatRecord(name, budgeted, spent);
                recs.add(currRec);
            }

            return recs;
       /* } catch (SQLException e) {
            printSQLException(e);
        }*/

        //return recs;
    }


    // Updates the budgeted amount for a single spending category
    public void updateCategory(String catName, double moddedAmount) throws SQLException {
        String queryStr = "UPDATE category SET budgeted=? WHERE name=?";
        //try (PreparedStatement pstmt = dbConn.prepareStatement(queryStr)) {
            PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
            dbConn.setAutoCommit(false);

            pstmt.setDouble(1, moddedAmount);
            pstmt.setString(2, catName);
            pstmt.executeUpdate();

            dbConn.commit();

/*        } catch (SQLException e) {
            printSQLException(e);
        }*/
    }


    // Updates the budgeted amount for a single spending category
    public void addCategory(String catName, double budgetAmount) throws SQLException {
        String queryStr =
            "INSERT INTO category(name, budgeted, spent) VALUES (?, ?, 0.00)";
        //try (PreparedStatement pstmt = dbConn.prepareStatement(queryStr)) {
            PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
            dbConn.setAutoCommit(false);

            pstmt.setString(1, catName);
            pstmt.setDouble(2, budgetAmount);
            pstmt.executeUpdate();

            dbConn.commit();

        /*} catch (SQLException e) {
            printSQLException(e);
        }*/
    }
}
