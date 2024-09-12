import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileHandling {



    private List<File> getFilesInFolder(File folder) {
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

    private File getSavedFile(String filePath) throws Exception {
        File folder = new File(filePath);
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

    private void sendFile(Socket socket, String fileName) throws Exception{
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

    private boolean receiveFile(Socket socket) {
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
