package src;

import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;


public class TransactionLogger
{
    // Creating and returning a connection to the database
    private Connection getDBConn() {
        Connection dbConn = null;

        try 
        {
            //Class.forName("org.mariadb.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/budgetApp?user=michael&password=clinesworth"
            );
        } catch (SQLException e) {
            System.err.println("Database connection rejected");
            e.printStackTrace();
            System.exit(2);
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
            System.exit(3);
        }

        return dbConn;
    }


    // Logging one-time expenditures and updating the corresponding spending
    // category
    public void logEvent(String category, String memo, double amount) 
                                            throws SQLException {
        Connection dbConn = getDBConn();

        // Creating the timestamp for recording datetime here
        String timeStamp = new SimpleDateFormat("MMddyyHHmmss")
            .format(new java.util.Date());

        // Making a prepared statement to log the transaction
        String queryStr = 
            "INSERT INTO expenditure(datetime, spending_category, action_amount, memo)";
        queryStr += "VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
        dbConn.setAutoCommit(false);

        pstmt.setString(1, timeStamp);
        pstmt.setString(2, category);
        pstmt.setDouble(3, amount);
        pstmt.setString(4, memo);
        pstmt.executeUpdate();

        dbConn.commit();

        // Then updating the spending category

        // First getting the current amount that's been spent in the category
        queryStr = "SELECT spent FROM category WHERE name=?";
        pstmt = dbConn.prepareStatement(queryStr);

        pstmt.setString(1, category);

        ResultSet rs = pstmt.executeQuery();

        // Then adding the amount of the new transaction
        rs.next();
        double spent = rs.getDouble("spent");
        spent += amount;

        // And updating the category
        queryStr = "UPDATE category SET spent=? WHERE name=?";
        pstmt = dbConn.prepareStatement(queryStr);

        pstmt.setDouble(1, spent);
        pstmt.setString(2, category);

        pstmt.executeUpdate();

        dbConn.commit();

        dbConn.close();
    }


    // Logging variable or one-time income
    public void logEvent(String memo, double amount) throws SQLException {
        Connection dbConn = getDBConn();

        // Creating the timestamp for recording datetime here
        String timeStamp = new SimpleDateFormat("MMddyyHHmmss")
            .format(new java.util.Date());

        // Making a prepared statement to log the transaction
        String queryStr = 
            "INSERT INTO income(datetime, action_amount, memo)";
        queryStr += "VALUES (?, ?, ?)";
        PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
        dbConn.setAutoCommit(false);

        pstmt.setString(1, timeStamp);
        pstmt.setDouble(2, amount);
        pstmt.setString(3, memo);
        pstmt.executeUpdate();

        dbConn.commit();

        dbConn.close();
    }
}
