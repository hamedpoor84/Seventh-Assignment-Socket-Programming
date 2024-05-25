package Server;
import ClientHandler.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
/**
 * WebSocket server implementation.
 */
public class Server {
    // Port to listen for incoming connections
    private static final int PORT = 3000;
    // List to hold connected clients
    private static ArrayList<Socket> clients = new ArrayList<>();
    // Thread pool to manage client connections efficiently
    private static ExecutorService pool = Executors.newFixedThreadPool(4);
    // hashmap for save member and their socket
    HashMap<String, Integer> ClientSocket  = new HashMap<>();
    /**
     * Main method to start the WebSocket server.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        ServerSocket listener = null;
        try {
            // Start listening on the specified port
            listener = new ServerSocket(PORT);
            System.out.println("[SERVER] Server started. Waiting for client connections...");

            // Continuously accept incoming client connections
            while (true) {
                // Accept a new client connection
                Socket client = listener.accept();
                System.out.println("[SERVER] Connected to client: " + client.getInetAddress());

                // Create a new client handler thread to handle client requests
                ClientHandler clientThread = new ClientHandler(client, clients);
                // Add the client socket to the list of connected clients
                clients.add(client);
                // Execute the client handler thread in the thread pool
                pool.execute(clientThread);
            }
        } catch (IOException e) {
            // Handle IOExceptions (e.g., socket errors)
            e.printStackTrace();
        } finally {
            // Close the server socket when the server is shutting down
            if (listener != null) {
                try {
                    listener.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Shutdown the thread pool to release resources
            pool.shutdown();
        }
    }
}
