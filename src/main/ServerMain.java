package main;

/**
 * Created by DRSPEED-PC on 30.06.2017.
 */
public class ServerMain {
    private static final int SERVER_PORT = 3334;

    public static void main(String[] args) {
        Server server = new main.Server(SERVER_PORT);
        new Thread(() -> {
            server.waitForClient();
        }).start();
    }
}
