package src;

import java.sql.*;
import java.util.ArrayList;

public class MonthlyManager {
    // Gets a connection to the database
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


    // Creating a class to hold monthly expenses as a single object
    public class MonthlyExpRecord {
        String memo;
        double amt;
        short monthsLeft;

        public MonthlyExpRecord(String memo, double amt, short monthsLeft) {
            this.memo = memo;
            this.amt = amt;
            this.monthsLeft = monthsLeft;
        }
    }


    // Gets and returns the total number of monthly expenses
    public int getNumMonthlyExps() throws SQLException {
        Connection dbConn = getDBConn();

        // Using count(*) to get the total number of spending categories
        String queryStr = "SELECT COUNT(*) AS numExps FROM monthly_expense;";
        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
        rs.next();
        int numExps = rs.getInt("numExps");

        // Closing the DB connection
        dbConn.close();

        return numExps;
    }


    public ArrayList<MonthlyExpRecord> getMonthlyExps() throws SQLException {
        Connection dbConn = getDBConn();

        // Querying for the records and filling the ArrayList
        ArrayList<MonthlyExpRecord> exps = new ArrayList<MonthlyExpRecord>();

        String queryStr = "SELECT * FROM monthly_expense";
        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
        while (rs.next()) {
            String memo = rs.getString("memo");
            double amt = rs.getDouble("amount");
            short numMonths = rs.getShort("months");

            MonthlyExpRecord exp = new MonthlyExpRecord(memo, amt, numMonths);
            exps.add(exp);
        }

        // Closing the DB connection
        dbConn.close();

        return exps;
    }


    public void addMonthlyExp(String memo, double amount, short expLen) throws SQLException {
        Connection dbConn = getDBConn();

        String queryStr = "INSERT INTO monthly_expense(memo, amount, months) ";
        queryStr += "VALUES (?, ?, ?)";
        PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
        dbConn.setAutoCommit(false);

        pstmt.setString(1, memo);
        pstmt.setDouble(2, amount);
        pstmt.setShort(3, expLen);
        pstmt.executeUpdate();

        dbConn.commit();

        dbConn.close();
    }


    public void cancelMonthlyExp(String memo) throws SQLException {
        Connection dbConn = getDBConn();

        String queryStr = "DELETE FROM monthly_expense WHERE memo=?";
        PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
        dbConn.setAutoCommit(false);

        pstmt.setString(1, memo);
        pstmt.executeUpdate();

        dbConn.commit();

        dbConn.close();
    }


    // Creating a class to hold monthly incomes as a single object
    public class MonthlyIncRecord {
        String memo;
        double amt;

        public MonthlyIncRecord(String memo, double amt) {
            this.memo = memo;
            this.amt = amt;
        }
    }


    // Gets and returns the total number of monthly income streams
    public int getNumMonthlyIncs() throws SQLException {
        Connection dbConn = getDBConn();

        // Using count(*) to get the total number of spending categories
        String queryStr = "SELECT COUNT(*) AS numIncs FROM monthly_income;";
        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
        rs.next();
        int numIncs = rs.getInt("numIncs");

        // Closing the DB connection
        dbConn.close();

        return numIncs;
    }


    public ArrayList<MonthlyIncRecord> getMonthlyIncs() throws SQLException {
        Connection dbConn = getDBConn();

        // Querying for the records and filling the ArrayList
        ArrayList<MonthlyIncRecord> incs = new ArrayList<MonthlyIncRecord>();

        String queryStr = "SELECT * FROM monthly_income";
        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
        while (rs.next()) {
            String memo = rs.getString("memo");
            double amt = rs.getDouble("amount");

            MonthlyIncRecord inc = new MonthlyIncRecord(memo, amt);
            incs.add(inc);
        }

        // Closing the DB connection
        dbConn.close();

        return incs;
    }


    public void addMonthlyInc(String memo, double amt) throws SQLException {
        Connection dbConn = getDBConn();

        String queryStr = "INSERT INTO monthly_income(memo, amount) ";
        queryStr += "VALUES (?, ?)";
        PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
        dbConn.setAutoCommit(false);

        pstmt.setString(1, memo);
        pstmt.setDouble(2, amt);
        pstmt.executeUpdate();

        dbConn.commit();

        dbConn.close();
    }


    public void cancelMonthlyInc(String memo) throws SQLException {
        Connection dbConn = getDBConn();

        String queryStr = "DELETE FROM monthly_income WHERE memo=?";
        PreparedStatement pstmt = dbConn.prepareStatement(queryStr);
        dbConn.setAutoCommit(false);

        pstmt.setString(1, memo);
        pstmt.executeUpdate();

        dbConn.commit();

        dbConn.close();
    }


    public static void main(String[] args) {
        System.out.println("This will be modified to be run monthly by cron");
    }
}
