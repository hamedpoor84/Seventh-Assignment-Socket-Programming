package Client;

import HandleServerResponce.HandleServerResponse;

import java.io.*;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * WebSocket client implementation.
 */
public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 3000;
    private static String fileChoose;
    private static String name;



    public void sendMassage (Socket client) throws IOException {

    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Socket client = new Socket("localhost" , SERVER_PORT);
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        HandleServerResponse handleServerResponse = new HandleServerResponse(client);
        String userInput;
        System.out.println("Set your user name: ");
        name = scanner.next();
        new Thread(handleServerResponse).start();

        // Continuously read user input from the console and send it to the server
        out.writeUTF(name);
        out.flush();
        System.out.println("((( you added to the server )))");
        label:
        while (true) {
            out = new DataOutputStream(client.getOutputStream());

            reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("What would you like to do?");
            System.out.println("1- Group chat");
            System.out.println("2- Send file");
            System.out.println("3- Received file");
            System.out.println("4- Exit");
            String choose = scanner.next();

            switch (choose) {
                case "1":
                    out.writeUTF("1");
                    out.flush();
                    while (true) {
                        userInput = "[" + name + "]: " + reader.readLine();
                        out.writeUTF(userInput);
                        out.flush();
                    }
                case "2":
                    List<Path> nameOfFiles = new ArrayList<>();
                    String folderPath = "seventh_assignment\\src\\main\\Resources\\UserData";
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

                    System.out.println("Choose the file you want:");
                    int counter = 1;
                    synchronized (nameOfFiles) {
                        for (Path path : nameOfFiles) {
                            System.out.println(counter + "- " + path.toString());
                            counter++;
                        }
                    }
                    System.out.print("choose File : ");
                    scanner.nextLine(); // Consume newline

                    fileChoose = scanner.nextLine();
                    out.writeUTF("2");
                    out.flush();
                    File file = new File("seventh_assignment\\src\\main\\Resources\\UserData\\" + nameOfFiles.get(Integer.parseInt(fileChoose)-1));
                    out.writeUTF(String.valueOf(nameOfFiles.get(Integer.parseInt(fileChoose)-1)));
                    if (file.exists() && !file.isDirectory()) {
                        try (FileInputStream fileInputStream = new FileInputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                            out.flush();
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
                    break;
                case "3":
                    out.writeUTF("3");
                    out.flush();
                    scanner.nextLine(); // Consume newline

                    String filechoose = scanner.nextLine();
                    out.writeUTF(filechoose);
                    out.flush();
                    File outputFile = new File("seventh_assignment\\src\\main\\Resources\\UserData\\received_file.txt");
                    outputFile.getParentFile().mkdirs(); // Ensure the directory exists


                    try (InputStream inputStream = client.getInputStream();
                         DataInputStream dataInputStream = new DataInputStream(inputStream);
                         FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                        System.out.println("File received successfully.");
                    } catch (IOException e) {
                        System.out.println("Error receiving file: " + e.getMessage());
                    }
                    break;
                case "4":
                    break label;
            }
        }
        client.close();
    }
}


