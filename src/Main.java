package src;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main
{
    // Creating a private class to handle individual connections with the
    // interface
    private class ConnHandler implements Runnable   
    {
        // Member variables
        protected Socket clientSock = null;
        protected DataOutputStream toInterface = null;
        protected DataInputStream fromInterface = null;

        public ConnHandler(Socket clientSock) {
            this.clientSock = clientSock;
        }


        // All the functions that handle specific commands
        private void sendCategories() {
            // Creating a category manager to handle interactions with the DB
            CategoryManager catMan = new CategoryManager();

            try {
                // Grabbing the number of categories and sending that first
                int numCats = catMan.getNumCategories();
                toInterface.writeInt(numCats);

                // Getting each category and sending the data for each to the
                // interface
                for (CategoryManager.CatRecord rec : catMan.getRecords()) {
                    String nameStr = rec.name + "\n";
                    toInterface.writeUTF(nameStr);
                    toInterface.writeDouble(rec.budgeted);
                    toInterface.writeDouble(rec.spent);
                }

                // Receiving and discarding the one-byte confirmation string
                byte confirmation = fromInterface.readByte();

            } catch (IOException e) {
                e.printStackTrace();
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
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                clientSock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Setting up the server
    int portNum = 45601;
    boolean stopped = false;
    private void listen() throws Exception
    {
        // Creating the server socket
        ServerSocket listSock = new ServerSocket(portNum, 32);

        while (!stopped)
        {
            try {
                Socket clientSock = listSock.accept();
                new Thread(new ConnHandler(clientSock)).start();
            } catch (IOException e) {
                throw new RuntimeException("Trouble accepting connection", e);
            }
        }
    }


    public static void main(String[] args)
    {
        TransactionLogger logman = new TransactionLogger();
        logman.greet();
        Main protoListener = new Main();
        try {
            protoListener.listen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
