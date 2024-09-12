import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class FTPServer {
    public static void main(String[] args) {
        int port = 5005; // Port number

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            // Wait for client connection
            Socket socket = serverSocket.accept();
            System.out.println("New client connected");
            FileHandling fileHandling = new FileHandling();

            sendClientChoice(socket);
            handleClientChoice(socket);

            socket.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void handleClientChoice(Socket socket) {
        while(true){
        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            int choice = dis.readInt();
            switch(choice){
                //Client wants to upload a file
                case 1:
                    System.out.println("Client wants to upload a file");
                    fileHandling.receiveFile(socket);
                    break;
                //Client wants to download a file
                case 2:
                    System.out.println("Client wants to download a file");
                    dos.writeUTF("Write the name of the file you want to download");
                    dos.flush();

                    String fileName = dis.readUTF();
                    try{
                        fileHandling.sendFile(socket,fileName);
                    } catch(Exception e){
                        dos.writeUTF("File not found");
                        dos.flush();
                    }
                    break;
                //CLient wants to close the connection
                case 3:
                    System.out.println("Client wants to close the connection");
                    socket.close();
                    break;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        }
    }

    private static void sendClientChoice(Socket socket){
        try{
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("Velkommen til vores FTP" + "\n[1] for at download fil\n" + "[2] for at sende fil\n" + "[3] for at luk client ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
