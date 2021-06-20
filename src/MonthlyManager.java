package src;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MonthlyManager {
    // Gets a connection to the database
    private static Connection getDBConn() {
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


    // Creating a class to hold data from past months
    public class MonthRecord {
        String date;
        double totInc;
        double totExp;

        public MonthRecord(String date, double totInc, double totExp) {
            this.date = date;
            this.totInc = totInc;
            this.totExp = totExp;
        }
    }


    // Gets the most recent month's data from the database and returning it
    public MonthRecord getLastMonth() throws SQLException {
        Connection dbConn = getDBConn();

        // Getting the record from the database and closing the db connection.
        // This'll get y2k'd in 2100, but I'm choosing to not care.
        String queryStr = "SELECT * FROM past_month ";
        queryStr += "WHERE date_code=(SELECT max(date_code) AS m FROM past_month)";

        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);

        rs.next();
        int dateAsNum = rs.getInt("date_code");
        double income = rs.getDouble("tot_income");
        double expenses = rs.getDouble("tot_expense");

        dbConn.close();

        // Converting the date to a string
        String dateStr = Integer.toString(dateAsNum);
        // Adding a leading 0 to the date if necessary
        if (dateStr.length() != 4)
            dateStr = "0" + dateStr;

        // Creating the object and returning it
        MonthRecord rec = new MonthRecord(dateStr, income, expenses);

        return rec;
    }


    // Getting the income/expense numbers for the entire year to date. This
    // data'll just be shoved into a MonthRecord because it can.
    public MonthRecord getCurrentYear() throws SQLException {
        Connection dbConn = getDBConn();

        // Getting the current year from a timestamp
        String currYear = new SimpleDateFormat("yy").format(new java.util.Date());

        // Using the year to make the query string
        String queryStr = "SELECT SUM(tot_income) AS yIncome, ";
        queryStr += "SUM(tot_expense) AS yExpense FROM past_month ";
        queryStr += "WHERE MOD((date_code-" + currYear + "), 100) = 0";

        // Getting the data from the db and closing the connection
        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);

        rs.next();
        double income = rs.getDouble("yIncome");
        double expenses = rs.getDouble("yExpense");

        dbConn.close();

        // Making the object and returning it
        String bogusMonthCode = "00" + currYear;
        MonthRecord rec = new MonthRecord(bogusMonthCode, income, expenses);

        return rec;
    }


    // Getting the amount that's been saved/spent all time (and putting it in a
    // MonthRecord)
    public MonthRecord getFullHistory() throws SQLException {
        Connection dbConn = getDBConn();

        // Querying for the sum of all past_month records and closing the
        // connection
        String queryStr = "SELECT SUM(tot_income) AS income, ";
        queryStr += "SUM(tot_expense) AS expense FROM past_month";

        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);

        rs.next();
        double income = rs.getDouble("income");
        double expenses = rs.getDouble("expense");

        dbConn.close();

        // Making the object and returning it
        String bogusMonthCode = "0000";
        MonthRecord rec = new MonthRecord(bogusMonthCode, income, expenses);

        return rec;
    }


    // When the monthly manager is run by itself, it'll log and reset all the
    // data from the current month, as needed. On my machine, this'll also be
    // set up to be run on the first of every month by cron
    public static void main(String[] args) {
        Connection dbConn = getDBConn();

        try {
            dbConn.setAutoCommit(false);

            // These variable names'll be used repeatedly, so I'm initializing them
            // here
            String queryStr;
            Statement stmt = dbConn.createStatement();
            ResultSet rs;

            // Part 1: Logging the total income and expenses for the month that
            // just ended

            // First getting the current month and year from a timestamp
            String currDate = new SimpleDateFormat("MMyy").format(new java.util.Date());

            // This'll run on the first day of the new month, meaning that the date
            // will have to be modified to the MM/YY code for the previous month.
            // Also, it's easier to modify the date if it's an int.
            int intDate = Integer.parseInt(currDate);
            // The current date is January, so the timestamp needs to be December
            // of the previous year
            if (currDate.startsWith("01")) {
                intDate += 1099; //This'll break in 2100, but I don't care
            } else {
                intDate -= 100;
            }
            // Changing the modified int date back to a 4 character string
            String lastMonth = Integer.toString(intDate);
            if (lastMonth.length() < 4)
                lastMonth = "0" + lastMonth;

            // Using the date of the previous month to query the income and
            // expenditure tables for the sum of all of last month's transactions
            // First income
            queryStr = "SELECT SUM(action_amount) AS mIncome FROM income WHERE ";
            queryStr += "datetime LIKE '" + lastMonth.charAt(0) + lastMonth.charAt(1);
            queryStr += "__" + lastMonth.charAt(2) + lastMonth.charAt(3) + "______'";

            double oneTimeInc = 0.0;
            rs = stmt.executeQuery(queryStr);
            if (rs.next())
                oneTimeInc = rs.getDouble("mIncome");

            // Then expenses
            queryStr = "SELECT SUM(action_amount) AS mExpense FROM expenditure WHERE ";
            queryStr += "datetime LIKE '" + lastMonth.charAt(0) + lastMonth.charAt(1);
            queryStr += "__" + lastMonth.charAt(2) + lastMonth.charAt(3) + "______'";

            double oneTimeExp = 0.0;
            rs = stmt.executeQuery(queryStr);
            if (rs.next())
                oneTimeExp = rs.getDouble("mExpense");

            // Then adding in all the current monthly figures
            // Again, income first
            queryStr = "SELECT SUM(amount) AS income FROM monthly_income";

            double recInc = 0.0;
            rs = stmt.executeQuery(queryStr);
            if (rs.next())
                recInc = rs.getDouble("income");

            // Expenses second
            queryStr = "SELECT SUM(amount) AS expenses FROM monthly_expense";

            double recExp = 0.0;
            rs = stmt.executeQuery(queryStr);
            if (rs.next())
                recExp = rs.getDouble("expenses");

            // Adding the pieces together for the final numbers
            double totIncome = oneTimeInc + recInc;
            double totExpenses = oneTimeExp + recExp;

            // Logging all of that data into the past_month table
            queryStr = "INSERT INTO past_month(date_code, tot_income, tot_expense) ";
            queryStr += "VALUES(" + lastMonth + ", " + Double.toString(totIncome);
            queryStr += ", " + Double.toString(totExpenses) + ")";

            stmt.executeUpdate(queryStr);

            // And finishing off by commiting the insertion
            dbConn.commit();


            // Part 2: Resetting/Modifying all the spending categories

            // Starting by creating an UPDATE prepared statement that'll be
            // executed after iterating through all the categories
            queryStr = "UPDATE category SET budgeted=?, spent=0.0 WHERE name=?";
            PreparedStatement updateCat = dbConn.prepareStatement(queryStr);

            queryStr = "SELECT * FROM category";

            rs = stmt.executeQuery(queryStr);
            while (rs.next()) {
                String catName = rs.getString("name");
                double budgeted = rs.getDouble("budgeted");

                // Rolling over the budgeted amount if it's a rollover category
                if (catName.endsWith("[R]")) {
                    double spent = rs.getDouble("spent");

                    double diff = budgeted - spent;
                    // Adding to the budget if diff is greater than 0, doing
                    // nothing if it's less than 0
                    if (diff > 0)
                        budgeted += diff;
                }

                // Adding the statement for this category to the update batch
                updateCat.setDouble(1, budgeted);
                updateCat.setString(2, catName);
                updateCat.addBatch();
            }

            // Executing the batch of updates
            updateCat.executeBatch();

            // Committing the updates
            dbConn.commit();

            // And closing the connection
            dbConn.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.err.println("Monthly update failed");
            System.exit(2);
        }
    }
}
