package ClientHandler;

import User.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static Server.Server.clients;

/**
 * Handles communication with a single client in the WebSocket server.
 */
public class ClientHandler implements Runnable {
    private Socket client; // The client socket
    private DataInputStream in; // Input stream to receive data from the client
    private DataOutputStream out; // Output stream to send data to the client
    private static final List<String> messages = new ArrayList<>();
    private static final List<Path> nameOfFiles = new ArrayList<>();

    /**
     * Constructor to initialize a ClientHandler object.
     *
     * @param client The client socket
     * @throws IOException If an I/O error occurs while setting up input and output streams
     */
    public ClientHandler(Socket client) throws IOException {
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
            String name = this.in.readUTF();
            User user = new User(client, name);
            clients.add(user);
            sendToAll("((( " + name + " add to Server )))", client);

            while (true) {
                request = this.in.readUTF();
                switch (request) {
                    case "1" -> {
                        sendChatHistory();
                        while (true) {
                            request = this.in.readUTF();
                            synchronized (messages) {
                                messages.add(request);
                            }
                            sendToAll(request, client);
                            System.out.println("[SERVER] request: " + request);
                        }
                    }
                    case "2" -> receiveFile();
                    case "3" -> sendFile();
                    case "4" -> {
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToAll(String msg, Socket client) throws IOException {
        synchronized (clients) {
            for (User aClient : clients) {
                if (!aClient.getSocket().equals(this.client)) {
                    DataOutputStream out = new DataOutputStream(aClient.getSocket().getOutputStream());
                    out.writeUTF(msg);
                    out.flush();
                }
            }
        }
    }

    private void sendChatHistory() throws IOException {
        synchronized (messages) {
            if (messages.size() >= 10) {
                messages.remove(0);
            }
            for (String message : messages) {
                out.writeUTF(message);
                out.flush();
            }
        }
    }

    private void receiveFile() throws IOException {
        System.out.println("start.");
        String nameOfFile = this.in.readUTF();
        try (InputStream inputStream = client.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream);
             FileOutputStream fileOutputStream = new FileOutputStream("seventh_assignment\\src\\main\\Resources\\ServerData\\" + nameOfFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                String message = new String(buffer, 0, bytesRead);
                fileOutputStream.write(buffer, 0, bytesRead);
                if (message.contains("FILE_END")) {
                    break;
                }
            }

            System.out.println("File received successfully.");
        } catch (IOException e) {
            System.out.println("Error receiving file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendFile() {
        String folderPath = "seventh_assignment\\src\\main\\Resources\\ServerData";
        Path folder = Paths.get(folderPath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.txt")) {
            synchronized (nameOfFiles) {
                nameOfFiles.clear();
                for (Path file : stream) {
                    nameOfFiles.add(file.getFileName());
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            sendFileNames();
            String fileRequest = this.in.readUTF();

            Path fileName;
            synchronized (nameOfFiles) {
                fileName = nameOfFiles.get(Integer.parseInt(fileRequest)-1);
            }
            File file = new File(folderPath + "\\" + fileName);
            if (file.exists() && !file.isDirectory()) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    // Send a special message indicating end of file transfer
                    out.writeUTF("FILE_END");
                    out.flush();
                    System.out.println("File sent successfully.");
                } catch (IOException e) {
                    System.out.println("Error sending file: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("File does not exist or is a directory.");
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void sendFileNames() throws IOException {
        out.writeUTF("Choose the file you want:");
        int counter = 1;
        synchronized (nameOfFiles) {
            for (Path path : nameOfFiles) {
                out.writeUTF(counter + "- " + path.toString());
                counter++;
            }
        }
        out.flush();
    }
}
