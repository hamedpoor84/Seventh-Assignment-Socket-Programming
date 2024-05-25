package ClientHandler;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles communication with a single client in the WebSocket server.
 */
public class ClientHandler implements Runnable {
    private Socket client; // The client socket
    private static ArrayList<Socket> clients; // List of all connected clients
    private DataInputStream in; // Input stream to receive data from the client
    private DataOutputStream out; // Output stream to send data to the client
    private static String[] names = {"Jack", "Daniel", "Wiliam", "Hobob", "Danzel", "Kate", "michel"}; // Array of random names
    ArrayList<String> requests = new ArrayList<>();

    /**
     * Constructor to initialize a ClientHandler object.
     *
     * @param client  The client socket
     * @param clients List of all connected clients
     * @throws IOException If an I/O error occurs while setting up input and output streams
     */
    public ClientHandler(Socket client, ArrayList<Socket> clients) throws IOException {
        this.clients = clients;
        this.client = client;
        // Initialize input and output streams for communication with the client
        this.in = new DataInputStream(client.getInputStream());
        this.out = new DataOutputStream(client.getOutputStream());
    }

    /**
     * Continuously reads client requests and responds accordingly.
     */
    @Override
    public void run() {
        try {
            String request;
            // Continuously listen for client requests
            while (true) {
                sendToAll("(((  New member add to Server  ))) " , client);
                // Read client request from the input stream
                request = this.in.readUTF();
                // Process client request
                if (request != null) {
                    if (request.startsWith("name")) {
                        // Respond with a random name if the request is for a name
                        this.out.writeUTF("Hi " + getRandomName());
                    } else {
                        // Otherwise, broadcast the message to all connected clients
                        sendToAll(request , client);
                    }
                    // Print the received request on the server console
                    System.out.println("[SERVER] request: " + request);
                }
            }
        } catch (IOException e) {
            // Handle any I/O exceptions that occur during communication with the client
            System.err.println("IO Exception in client handler!!!!!!");
            e.printStackTrace();
        } finally {
            try {
                // Close input and output streams and the client socket when done
                in.close();
                out.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates and returns a random name from the array of names.
     *
     * @return A random name
     */
    private String getRandomName() {
        return names[(int) (Math.random() * names.length)];
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param msg The message to broadcast
     * @throws IOException If an I/O error occurs while sending the message to clients
     */
    private void sendToAll(String msg , Socket client ) throws IOException {
        for (Socket aClient : clients ) {
            // Get the output stream of each client and send the message
            if (!aClient.equals(this.client)) {
                DataOutputStream out = new DataOutputStream(aClient.getOutputStream());
                out.writeUTF(msg);
            }
        }
    }
}
