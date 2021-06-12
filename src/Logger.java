package src;

import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;


public class Logger
{
    public static void printSQLException(SQLException e)
    {
        e.printStackTrace(System.err);
        System.err.println("SQLState: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
        System.err.println("Message: " + e.getMessage());
    }


    Connection dbConn;
    public Logger()
    {
        // Creating the db connection

        // I don't know what this is, but I'm putting it here for posterity
        // Class.forName("org.mariadb.jdbc.Driver");
        try 
        {
            //Class.forName("org.mariadb.jdbc.Driver");
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


    public void logEvent(String actionType, double amount, String memo)
    {
        // Creating the timestamp for recording datetime here
        String timeStamp = new SimpleDateFormat("MMddyyHHmmss")
            .format(new java.util.Date());

        // Making a prepared statement for the transaction
        String actionStr = 
            "INSERT INTO transaction(datetime, action_type, action_amount, memo)";
        actionStr += "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConn.prepareStatement(actionStr))
        {
            dbConn.setAutoCommit(false);

            pstmt.setString(1, timeStamp);
            pstmt.setString(2, actionType);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, memo);
            pstmt.executeUpdate();

            dbConn.commit();

        } catch (SQLException e) {
            printSQLException(e);
        }
    }


    public void greet()
    {
        System.out.println("Welcome back, java");
        TestClass.greet();
    }
}
