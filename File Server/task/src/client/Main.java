package client;

/*
    Класс, запускающий работу консольного Java-клиента
 */

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(50);
        ClientNetworkConnection networkConnection = new ClientNetworkConnection("127.0.0.1", 23456);
    }
}