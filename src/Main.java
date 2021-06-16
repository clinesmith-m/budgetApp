package src;

import java.net.*;
import java.io.*;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;

public class Main
{
    // Creating a private class to handle individual connections with the
    // interface
    private class ConnHandler implements Runnable   
    {
        // Error printing
        protected void printSQLException(SQLException e) {
            e.printStackTrace(System.err);
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
        }


        // Member variables
        protected Main parent = null;
        protected Socket clientSock = null;
        protected DataOutputStream toInterface = null;
        protected DataInputStream fromInterface = null;

        public ConnHandler(Socket clientSock, Main parent) {
            this.clientSock = clientSock;
            this.parent = parent;
        }


        // All the functions that handle specific commands

        // Sending all spending categories
        private void sendCategories() {
            // Creating a category manager to handle interactions with the DB
            CategoryManager catMan = new CategoryManager();

            try {
                // Grabbing the number of categories and sending that first
                int numCats = -1;
                try {
                    numCats = catMan.getNumCategories();
                } catch (SQLException e) {
                    printSQLException(e);
                    parent.stopped = true;
                    System.exit(2);
                }
                toInterface.writeInt(numCats);

                try {
                    // Only sending data if there's at least one result
                    if (numCats > 0) {
                        // Getting each category and sending the data for each to the
                        // interface
                        for (CategoryManager.CatRecord rec : catMan.getRecords()) {
                            toInterface.writeUTF(rec.name);
                            toInterface.writeDouble(rec.budgeted);
                            toInterface.writeDouble(rec.spent);
                        }
                    }

                    // Receiving and discarding the one-byte confirmation string
                    byte confirmation = fromInterface.readByte();

                } catch (SQLException e) {
                    printSQLException(e);
                    parent.stopped = true;
                    System.exit(2);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }


        // Modifies a single, already-existing spending category
        public void modCategory() {
            CategoryManager catMan = new CategoryManager();

            // Receiving the data from the interface
            try {
                String catName = fromInterface.readUTF();
                double moddedAmt = fromInterface.readDouble();

                try {
                    catMan.updateCategory(catName, moddedAmt);
                } catch (SQLException e) {
                    this.printSQLException(e);
                    parent.stopped = true;
                    System.exit(2);
                }
                toInterface.writeBytes("T");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }


        // Creates a new spending category
        public void addCategory() {
            CategoryManager catMan = new CategoryManager();

            // Receiving the data from the interface
            try {
                String catName = fromInterface.readUTF();
                double budgetAmt = fromInterface.readDouble();

                try {
                    catMan.addCategory(catName, budgetAmt);
                } catch (SQLException e) {
                    this.printSQLException(e);
                    parent.stopped = true;
                    System.exit(2);
                }
                toInterface.writeBytes("T");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }


        // The run function listens for a 4 letter code from the connection
        public void run()
        {
            try {
                // Establishing the I/O streams
                toInterface = new DataOutputStream(clientSock.getOutputStream());
                fromInterface = new DataInputStream(clientSock.getInputStream());

                // Receiving the 4 letter command and casting it to a string
                byte[] bCommand = new byte[4];
                fromInterface.readFully(bCommand);
                String command = new String(bCommand, StandardCharsets.UTF_8);

                // Calling the function associated with the command
                if (command.equals("GCAT")) {
                    sendCategories();
                } else if (command.equals("MCAT")) {
                    modCategory();
                } else if (command.equals("ACAT")) {
                    addCategory();
                } else if (command.equals("DISC")) {
                    System.out.println("Ordering shutdown");
                    parent.stopped = true;
                    System.exit(0);
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            try {
                clientSock.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }


    // Member variables for the server
    private int portNum;
    public boolean stopped;


    // The constructor
    public Main() {
        this.portNum = 45601;
        this.stopped = false;
    }


    // The actual server code
    private void listen() throws Exception
    {
        // Creating the server socket
        ServerSocket listSock = new ServerSocket(portNum, 32);
        System.out.println("Listening");

        while (!stopped)
        {
            try {
                Socket clientSock = listSock.accept();
                new Thread(new ConnHandler(clientSock, this)).start();
            } catch (IOException e) {
                throw new RuntimeException("Trouble accepting connection", e);
            }
        }
    }


    public static void main(String[] args)
    {
        Main protoListener = new Main();
        try {
            protoListener.listen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
