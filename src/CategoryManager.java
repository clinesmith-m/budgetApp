package src;

import java.sql.*;
import java.util.ArrayList;


public class CategoryManager {
    public CategoryManager() {
    }


    // Getting a connection to the database
    private Connection getDBConn() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection dbConn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/budgetApp?user=michael&password=clinesworth"
            );

            return dbConn;

        } catch (SQLException e) {
            System.err.println("Database connect rejected");
            e.printStackTrace(System.err);
            System.exit(2);
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
            System.exit(2);
        }

        return null;
    }


    public int getNumCategories() throws SQLException {
        Connection dbConn = getDBConn();

        // Using count(*) to get the total number of spending categories
        String queryStr = "SELECT COUNT(*) AS numCats FROM category;";
        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
        rs.next();
        int numCats = rs.getInt("numCats");

        // Closing the DB connection
        dbConn.close();

        return numCats;
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
        Connection dbConn = getDBConn();

        // Running the query and constructing the ArrayList
        String queryStr = "SELECT * FROM category";
        ArrayList<CatRecord> recs = new ArrayList<CatRecord>();

        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
        while (rs.next()) {
            String name = rs.getString("name");
            double budgeted = rs.getDouble("budgeted");
            double spent = rs.getDouble("spent");

            CatRecord currRec = new CatRecord(name, budgeted, spent);
            recs.add(currRec);
        }

        // Closing the DB connection
        dbConn.close();

        return recs;
    }


    // Querying for a single category by name and returning it
    public CatRecord getRecord(String name) throws SQLException {
        Connection dbConn = getDBConn();

        // Creating and running the query
        String queryStr = "SELECT * FROM category WHERE name=?";
        PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
        dbConn.setAutoCommit(false);
        pstmt.setString(1, name);
        ResultSet rs = pstmt.executeQuery();

        // Making the CatRecord object
        rs.next();
        double budgeted = rs.getDouble("budgeted");
        double spent = rs.getDouble("spent");
        CatRecord rec = new CatRecord(name, budgeted, spent);

        // Closing the DB connection
        dbConn.close();

        return rec;
    }


    // Updates the budgeted amount for a single spending category
    public void updateCategory(String catName, double moddedAmount) throws SQLException {
        Connection dbConn = getDBConn();

        String queryStr = "UPDATE category SET budgeted=? WHERE name=?";
        PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
        dbConn.setAutoCommit(false);

        pstmt.setDouble(1, moddedAmount);
        pstmt.setString(2, catName);
        pstmt.executeUpdate();

        dbConn.commit();

        // Closing the DB connection
        dbConn.close();
    }


    // Updates the budgeted amount for a single spending category
    public void addCategory(String catName, double budgetAmount) throws SQLException {
        Connection dbConn = getDBConn();

        // Adding all new categories to the category table
        String queryStr =
            "INSERT INTO category(name, budgeted, spent) VALUES (?, ?, 0.00)";
        PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
        dbConn.setAutoCommit(false);

        pstmt.setString(1, catName);
        pstmt.setDouble(2, budgetAmount);
        pstmt.executeUpdate();

        dbConn.commit();

        // Then adding rollover categories to the rollover table
        if (catName.endsWith("[R]"))
        {
            queryStr = "INSERT INTO rollover_category(name, baseline) VALUES (?,?)";
            pstmt = dbConn.prepareStatement(queryStr);

            pstmt.setString(1, catName);
            pstmt.setDouble(2, budgetAmount);
            pstmt.executeUpdate();

            dbConn.commit();
        }

        // Closing the DB connection
        dbConn.close();
    }
}
