package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/*
    Клиентский класс, содержащий методы по работе с потоками из источников данных
*/

public class StreamManager {
    private DataInputStream input;
    private DataOutputStream output;
    private Scanner scanner;

    StreamManager(Socket socket) throws IOException {
        this.scanner = new Scanner(System.in);
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    public String getComandFromScanner() throws IOException {
        String str = scanner.nextLine();
        if (str.equals("exit")){
            writeIntsToServer(665);
            //throw new IOException();
        }else{
            return str.trim();
        }
        return str.trim();
    }

    public void writeIntsToServer(int commandId) throws IOException {
        output.writeInt(commandId);
    }

    public void writeTextToServer(String textInfo) throws IOException {
        output.writeUTF(textInfo);
    }

    public int readIntFromServer() throws IOException {
        if (input.equals(null)){
            throw new IOException();
        }else {
            return input.readInt();
        }
    }

    public String readTextFromServer() throws IOException {
        String str = "";
        if (input.equals(null)) {
                throw new IOException();
        }else{
            try {
                    str = input.readUTF();
            }catch (IOException e) {
                    System.out.println("Ошибка в классе StreamManager");
                    e.printStackTrace();
            }
            return str;
        }
    }

    public void closeStreams() {
        if (input != null){
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        if (output != null){
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        if(scanner != null) {
            scanner.close();
        }
    }

}
