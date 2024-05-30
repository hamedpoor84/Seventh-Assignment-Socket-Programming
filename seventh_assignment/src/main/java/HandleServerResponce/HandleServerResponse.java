package HandleServerResponce;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles responses from the server asynchronously for a WebSocket client.
 */
public class HandleServerResponse implements Runnable {
    private DataInputStream in; // Input stream to receive server responses

    /**
     * Constructor to initialize a HandleServerResponse object.
     *
     * @param client The client socket
     * @throws IOException If an I/O error occurs while setting up the input stream
     */
    public HandleServerResponse(Socket client) throws IOException {
        // Initialize the input stream to receive server responses
        this.in = new DataInputStream(client.getInputStream());
    }

    /**
     * Continuously listens for server responses and prints them to the console.
     */
    @Override
    public void run() {
        try {
            // Continuously listen for server responses
            while (true) {
                // Read server response from the input stream and print it to the console
                String message = this.in.readUTF();
                System.out.println(message);
            }
        } catch (Exception e) {
            // Handle any I/O exceptions that occur while reading server responses
            throw new RuntimeException(e);
        }
    }
}
