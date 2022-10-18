package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
   Служебный класс с методами взаимодействия по сети
 */

public class NetworkConnection{
    protected ServerSocket server;
    private boolean stopMarker = true;
    private ServerFileStorage fileStorage;
    private ArrayList<Socket> socketsList = new ArrayList<>();

    NetworkConnection(String address, int port){
        this.fileStorage = new ServerFileStorage();
        try {
            this.server = new ServerSocket(port, 50, InetAddress.getByName(address));
            System.out.println("Server started!");
        }catch (Exception e) {}
    }

    protected void serverListening(){
        while (stopMarker){
             try {
                 Socket socket = server.accept();
                 socketsList.add(socket);
                 Controller controller = new Controller(this, socket, fileStorage);
                 controller.run();
             } catch (IOException e) {
                 closeAllSocket();
                 if(e instanceof MyException && MyException.isGlobalStop){
                     stopServer();
                 }
             }
        }
    }

    protected void closeAllSocket(){
        for (Socket socket : socketsList){
            try {
                if(!socket.isClosed()){
                    socket.getOutputStream().close();
                }
                if(!socket.isClosed()){
                    socket.getInputStream().close();
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stopServer();
    }

    protected void stopServer(){
        try {
            server.close();
            stopMarker = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
