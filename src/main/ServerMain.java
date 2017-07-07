package main;

/**
 * Created by DRSPEED-PC on 30.06.2017.
 */
public class ServerMain {
    private static final int SERVER_PORT = 3334;

    public static void main(String[] args) {

        /**
         * Сервер для статичных снимков
         */
        TCPServer server = new TCPServer(SERVER_PORT);

        /**
         * Сервер для потокового видео
         */
//        UDPServer server = new UDPServer(SERVER_PORT);

        new Thread(() -> {

            /**
             * Нить для TCP.
             */
            server.waitForClient();

            /**
             * Нить для UDP.
             */
//            server.clientRequest();
        }).start();
    }
}
