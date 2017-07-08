package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by DRSPEED-PC on 28.06.2017.
 */
public class TCPServer {

    private ServerSocket serverSocket = null;
    private List<TCPClientHandler> clientHandlerList;
    private long sleep = 2000;

    public TCPServer(int serverPort){
        clientHandlerList = new LinkedList<>();
        System.out.println("TCPServer starting...");
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        System.out.println("TCPServer socket init OK.");
    }

    // TODO: 30.06.2017 запилить авторизацию пользователей. 
    public synchronized void addClient(TCPClientHandler clientHandler) {
        clientHandlerList.add(clientHandler);
    }

    public void waitForClient() {
        Socket socket = null;
        try {
            while(true) {
                socket = serverSocket.accept();
                System.out.println("New client connected.");
                TCPClientHandler client = new TCPClientHandler(socket, this);
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                System.out.println("main.TCPServer closed.");
                socket.close();
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
    }

    public synchronized void newFileFromClient(TCPClientHandler handler, String fileName) {
        try {
            CloudRecognition recognition = new CloudRecognition(fileName);
            while (!recognition.isTaskComplete){
                Thread.sleep(sleep);
            }
            System.out.println("Result recieved from cloud.");
            /**
             * Uncomment for OpenCV recognition.
             */
//            OpenCvRecognition recognition = new OpenCvRecognition(fileName);
            handler.transferAnswer(recognition.resultFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
