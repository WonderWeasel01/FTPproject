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

            sendClientChoice(socket);
            handleClientChoice(socket);

            socket.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void handleClientChoice(Socket socket) {

        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            String test = dis.readUTF();
            switch(test){
                //Client wants to upload a file
                case "1":
                    receiveFile(socket);
                    break;
                //Client wants to download a file
                case "2":
                    dos.writeUTF("Write the name of the file you want to download");
                    String fileName = dis.readUTF();
                    try{
                        sendFile(socket,fileName);
                    } catch(Exception e){
                        dos.writeUTF("File not found");
                    }
                    break;
                //CLient wants to close the connection
                case "3":
                    socket.close();
                    break;
            }
        } catch(IOException e){
            e.printStackTrace();
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


    private static List<File> getFilesInFolder(File folder) {
        File[] files = folder.listFiles();
        List<File> fileList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    private static File getSavedFile(String fileName) throws Exception {
        String folderPath = "src/resources/receivedFiles";
        File folder = new File(folderPath);
        if(folder.listFiles() != null){
            List<File> fileList = List.of(folder.listFiles());
            for(File file : fileList){
                if(file.getName().equals(fileName)){
                    return file;
                } else throw new Exception("File not found");
            }
        }
        throw new Exception("No files found in " + folderPath);
    }

    private static void sendFile(Socket socket, String fileName) throws Exception{
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            //Send file data to differentiate between files
            File file = getSavedFile(fileName);
            long fileSize = file.length();
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.writeLong(fileSize);
            dataOutputStream.flush();


            //Create an inputstream to the file that we're sending, so we can read the data we want to send.
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            //Get the outputstream, so we can send the data to the socket.
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());

            byte[] buffer = new byte[1024];
            int bytesRead;
            //bufferedinputstream.read returns -1 if there's no more data to be read.
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            bufferedInputStream.close();

            System.out.println("File " + fileName + " sent successfully");

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch(Exception e){
            throw e;
        }
    }

    private static boolean receiveFile(Socket socket) {
        String saveDir = "src/resources/receivedFiles";
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
