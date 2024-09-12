import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FTPClient {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Change to server IP if needed
        int port = 5005;

        try (Socket socket = new Socket(serverAddress, port)) {
            System.out.println("Connected to server");
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            System.out.println("Velkommen til vores FTP" + "[1] for at download fil\n" + "[2] for at sende fil\n" + "[3] for at luk client ");

            Scanner scanner = new Scanner(System.in);
            byte[] buffer = new byte[1024];

            while (true) {
                dis.read(buffer);
                dos.writeUTF(scanner.next());
            }


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Method to receive file from server and save it locally
    private static boolean receiveFile(Socket socket) {
        String saveDir = "src/resources/clientRecievedFiles";
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            String fileName = dataInputStream.readUTF();

            if(fileName.equals("END_OF_FILES")){
                return false; // No more files to read
            }

            //Create path- & output stream to the file.
            String filePath = saveDir + File.separator + fileName;
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));


            //We use the filesize to know when to separate files
            long fileSize = dataInputStream.readLong();
            long bytesRemaining = fileSize;

            byte[] buffer = new byte[1024];
            int bytesRead;

            //Read data from server until end of stream. DataInputStream.read returns -1 when there is no more data to read.
            while ((bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                //Write the data from the inputstream to the file
                bos.write(buffer, 0, bytesRead);
                //Calculate when the file is fully read.
                bytesRemaining -= bytesRead;
                if (bytesRemaining <= 0) {
                    break;
                }
            }

            bos.flush();
            bos.close();

            System.out.println("File " + fileName + " saved to " + saveDir);
            return true;
        } catch (EOFException eof) {
            // End of stream reached;
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
