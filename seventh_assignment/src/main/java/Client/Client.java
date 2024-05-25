package Client;
import HandleServerResponce.HandleServerResponse;
import Server.Server;
import HandleServerResponce.HandleServerResponse;

import javax.naming.Name;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * WebSocket client implementation.
 */
public class Client {
    // IP address of the server to connect to
    private static final String SERVER_IP = "127.0.0.1";
    // Port of the server to connect to
    private static final int SERVER_PORT = 3000;

    private static String name ;


    /**
     * Main method to start the WebSocket client.
     *
     * @param args Command line arguments (not used)
     * @throws IOException If an I/O error occurs while communicating with the server
     */
    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Set your user name : ");
        name = scanner.next();
        // Establish a connection to the server
        Socket client = new Socket(SERVER_IP, SERVER_PORT);
        // Output stream to send data to the server
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        // Print a message indicating successful connection
        System.out.println("[CLIENT] connected to server :)");

        // Input stream reader to read user input from the console
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Start a thread to handle server responses asynchronously
        HandleServerResponse handleServerResponse = new HandleServerResponse(client);
        new Thread(handleServerResponse).start();

        String userInput;
        // Continuously read user input from the console and send it to the server
        while (true) {
            userInput = "["+name+"] : " + reader.readLine();
            out.writeUTF(userInput);
        }
    }
}
