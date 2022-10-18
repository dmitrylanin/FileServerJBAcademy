package client;

import java.io.*;
import java.net.Socket;

public class ClientFileStorage {
    private File directory;
    private Socket socket;

    ClientFileStorage(Socket socket, StreamManager streamManager){
        this.socket = socket;
        this.directory = new File(System.getProperty("user.dir")
                + File.separator + "src" + File.separator + "client" + File.separator + "data" + File.separator);
        if (!this.directory.exists()){
            this.directory.mkdirs();
        }
    }


    protected String saveFileFromServer(DataInputStream inputStream){
        int messageLength;
        byte [] buffer = new byte[128];

        try(FileOutputStream outputStream = new FileOutputStream(directory + System.getProperty("file.separator") + "ProxyName")){
            while (true){
                messageLength = inputStream.readInt();
                if (messageLength == -1){
                    break;
                }
                inputStream.read(buffer);
                outputStream.write(buffer, 0, messageLength);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "The file was not downloaded!";
        }
        return "The file was downloaded! Specify a name for it: ";
    }

    protected boolean sendFileToServer(String fileName){
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(FileInputStream fileInputStream = new FileInputStream(directory+File.separator+fileName)){
            int size = 0;
            byte[] buffer = new byte[128];
            while ((size = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.writeInt(size);
                dataOutputStream.write(buffer, 0, buffer.length);
            }
            dataOutputStream.writeInt(size);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected boolean renameFile(String newName){
        if(new File(directory+File.separator+"ProxyName").renameTo(new File(directory+File.separator+newName))){
            return true;
        }else{
            return false;
        }
    }
}