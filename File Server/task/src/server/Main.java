package server;


/*
    Класс, запускающий сервер. Сервер слушает сеть и открывает сокет, после того как к нему подключается клиент
 */
public class Main {
    public static void main(String[] args) {
        NetworkConnection networkConnection = new NetworkConnection("127.0.0.1", 23456);
        networkConnection.serverListening();
    }
}