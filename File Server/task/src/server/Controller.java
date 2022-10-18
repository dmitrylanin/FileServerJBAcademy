package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
/*
    Контроллер - класс получает сокет и управляет логикой соединения с клиентом
 */

public class Controller implements Runnable{
    private ServerFileStorage fileStorage;
    private DataInputStream input;
    private DataOutputStream output;
    private NetworkConnection networkConnection;
    private Socket socket;
    private boolean engineMarker = true;

    public Controller(NetworkConnection networkConnection,
                      Socket socket,
                      ServerFileStorage fileStorage){
            this.socket = socket;
            this.fileStorage = fileStorage;
            this.networkConnection = networkConnection;
        try {
            this.input = new DataInputStream(this.socket.getInputStream());
            this.output = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void engine() throws IOException{
        int currentCommandId;

        while (engineMarker){

            output.writeUTF("Enter action (1 - get a file, 2 - save a file, 3 - delete a file):");
            currentCommandId = input.readInt();

            if (currentCommandId == 665){
                break;
            }else{
                switch (currentCommandId) {
                    case 1:
                        sendFileToClient();
                        break;
                    case 2:
                        saveFile();
                        break;
                    case 3:
                        deleteFile();
                        break;
                    default:
                        break;
                }
            }
        }
        networkConnection.closeAllSocket();
    }

    public synchronized void deleteFile(){
        try {
            output.writeUTF("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
            int nameOrId = input.readInt();
            String fileName = null;
            Integer fileId = null;

            if (nameOrId == 1) {
                output.writeUTF("Enter name: ");
                fileName = input.readUTF();
            } else if (nameOrId == 2) {
                output.writeUTF("Enter id: ");
                fileId = Integer.parseInt(input.readUTF());
            }

            String finalFileName = fileName;
            Integer finalFileId = fileId;
            fileStorage.deleteFile(finalFileName, finalFileId, output);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized void saveFile(){
        try {
            output.writeUTF("Enter name of the file to be saved on server: ");
            String fileName = input.readUTF();
            fileStorage.saveNewFile(fileName,
                    new DataInputStream(input),
                    new DataOutputStream(output));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized void sendFileToClient() {
        try {
            output.writeUTF("Do you want to get the file by name or by id (1 - name, 2 - id): ");
            int nameOrId = input.readInt();
            int fileId = 0;
            boolean marker = true;
            String fileName = "";

            if (nameOrId == 1) {
                output.writeUTF("Enter name: ");
                fileName = input.readUTF();
            } else if (nameOrId == 2) {
                output.writeUTF("Enter id: ");
                fileId = Integer.parseInt(input.readUTF());
            }

            if (fileStorage.isFileIn(fileName, null) || fileStorage.isFileIn(null, fileId)) {
                String finalFileName = fileName;
                int finalFileId = fileId;
                if (marker && nameOrId == 1) {
                    fileStorage.sendFileToClient(finalFileName, null, output);
                } else {
                    fileStorage.sendFileToClient(null, finalFileId, output);
                }
            } else {
                output.writeInt(404);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void run(){
        try {
            engine();
        }catch (IOException e) {
            try {
                if(!socket.isClosed()){
                    socket.getOutputStream().close();
                }
                if(!socket.isClosed()){
                    socket.getInputStream().close();
                }
                socket.close();
            } catch (IOException ee) {
                ee.printStackTrace();
            }
        }
    }
}