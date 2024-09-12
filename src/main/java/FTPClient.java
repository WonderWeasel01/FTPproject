import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FTPClient {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Change to server IP if needed
        int port = 5005;

        try (Socket socket = new Socket(serverAddress, port)) {
            System.out.println("Connected to server");

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            FileHandling fileHandling = new FileHandling();

            String welcomeMessage = dis.readUTF();
            System.out.println(welcomeMessage);

            boolean running = true;

            while (running) {
                // Prompt the client for a choice
                System.out.println("Enter your choice: ");
                System.out.println("[1] Download a file");
                System.out.println("[2] Upload a file");
                System.out.println("[3] Close connection");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                // Send the choice to the server
                dos.writeInt(choice);
                dos.flush();

                switch (choice) {
                    case 1: // Download a file
                        System.out.println(dis.readUTF()); // Read prompt from the server
                        String fileNameToDownload = scanner.nextLine();
                        dos.writeUTF(fileNameToDownload);
                        dos.flush();

                        // Receive the file from the server
                        fileHandling.recieveFile(socket);
                        break;

                    case 2: // Upload a file
                        System.out.println("Enter the file path to upload:");
                        String filePathToUpload = scanner.nextLine();
                        filehandling.sendFile(socket, filePathToUpload);
                        break;

                    case 3: // Close connection
                        System.out.println("Closing connection...");
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid choice, try again.");
                        break;
                }
            }

            socket.close();
            scanner.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
