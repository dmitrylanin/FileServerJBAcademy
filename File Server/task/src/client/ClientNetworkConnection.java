package client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientNetworkConnection implements Runnable{
    private Socket socket;
    private StreamManager streamManager;
    private ClientFileStorage clientFileStorage;


    ClientNetworkConnection(String address, int port){
        try {
            this.socket = new Socket(InetAddress.getByName(address), port);
            this.clientFileStorage = new ClientFileStorage(socket, streamManager);
            this.streamManager = new StreamManager(socket);

            if(streamManager != null) {
                run();
            }else{
                throw new ConnectException();
            }
        } catch (IOException | NumberFormatException e){
            closeClient();
        }
    }

    public void ioWithServer() throws IOException{
            String comand = "";
            int commandId;
            System.out.println("Client started!");

            while (true){
                System.out.println(streamManager.readTextFromServer());
                comand = streamManager.getComandFromScanner();

                if (comand.equals("exit")) {
                    try {
                        streamManager.writeIntsToServer(665);
                    } catch (IOException e) {
                        closeClient();
                        break;
                        //e.printStackTrace();
                    }
                    closeClient();
                    break;
                }

                commandId = Integer.parseInt(comand);
                switch (commandId) {
                    case 1:
                        getFile(commandId);
                        break;
                    case 2:
                        saveFileOnServer(commandId);
                        break;
                    case 3:
                        deleteFile(commandId);
                        break;
                    case 665:
                        closeClient();
                        break;
                    default:
                        break;
                }
                break;
            }
        closeClient();
    }

    //1 - Получение файла от сервера
    public void getFile(int commandId){
        try {
            streamManager.writeIntsToServer(commandId);

            //Do you want to get the file by name or by id (1 - name, 2 - id):
            System.out.println(streamManager.readTextFromServer());
            //Вводим: 1 - имя файла, 2 - id файла
            streamManager.writeIntsToServer(Integer.parseInt(streamManager.getComandFromScanner()));

            System.out.println(streamManager.readTextFromServer());
            streamManager.writeTextToServer(streamManager.getComandFromScanner());
            System.out.println("The request was sent.");

            if (streamManager.readIntFromServer() == 200){
                System.out.println(clientFileStorage.saveFileFromServer(new DataInputStream(socket.getInputStream())));
                boolean isCorrectRename = clientFileStorage.renameFile((streamManager.getComandFromScanner()));

                if (isCorrectRename) {
                    System.out.println("File saved on the hard drive!\n");
                } else {
                    System.out.println("The response says that this file is not found!\n");
                }
            } else {
                System.out.println("The response says that this file is not found!\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //2 - Сохранение файла на сервере
    public void saveFileOnServer(int commandId) throws IOException{
            String fileNameOnClient = "";
            String fileNameOnServer = "";
            System.out.println("Enter name of the file: ");
            fileNameOnClient = streamManager.getComandFromScanner();
            streamManager.writeIntsToServer(commandId);
            System.out.println(streamManager.readTextFromServer());
            fileNameOnServer = streamManager.getComandFromScanner();
            if (fileNameOnServer.equals("")){
                streamManager.writeTextToServer((fileNameOnClient));
            }else{
                streamManager.writeTextToServer((fileNameOnServer));
            }
            clientFileStorage.sendFileToServer(fileNameOnClient);
            System.out.println("The request was sent.");

            if (streamManager.readIntFromServer() == 404) {
                System.out.println("The response says that the file was not found!\n");
            } else {
                System.out.println(streamManager.readTextFromServer());
            }
    }

    public void deleteFile(int commandId) throws IOException{

            streamManager.writeIntsToServer(commandId);

            //Do you want to delete the file by name or by id (1 - name, 2 - id):
            System.out.println(streamManager.readTextFromServer());
            //Вводим: 1 - имя файла, 2 - id файла
            streamManager.writeIntsToServer(Integer.parseInt(streamManager.getComandFromScanner()));

            //Enter id/name
            System.out.println(streamManager.readTextFromServer());
            streamManager.writeTextToServer(streamManager.getComandFromScanner());

            System.out.println("The request was sent.");

            if (streamManager.readIntFromServer() == 200) {
                System.out.println("The response says that this file was deleted successfully!\n");
            } else {
                System.out.println("The response says that the file was not found!\n");
            }
    }

    public void closeClient(){
        try {
            streamManager.closeStreams();
            socket.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {
            ioWithServer();
        } catch (IOException e) {
            closeClient();
        }
    }
}