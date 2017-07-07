package main;

import java.io.*;
import java.net.Socket;

/**
 * Created by DRSPEED-PC on 29.06.2017.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private TCPServer server;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String inputFilePath = "F:\\GIT\\ABBYY_OCR\\Abbyy.Ocrsdk.client\\PICTURES\\1\\";
    private static int clientCount = 0;

    public ClientHandler(Socket socket, TCPServer server){
        try {
            this.server = server;
            this.socket = socket;
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            clientCount++;
            System.out.println("Client #" + clientCount + ".");
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public void run() {
        recieveFile();
    }

    private void recieveFile() {
        while (true) {
            try {
                long fileSize = dataInputStream.readLong(); // получаем размер файла
                String fileName = dataInputStream.readUTF(); //прием имени файла

                System.out.println("Имя файла: " + fileName);
                System.out.println("Размер файла: " + fileSize + " байт");
                String PATH = inputFilePath + fileName;

                byte[] buffer = new byte[64*1024];
                FileOutputStream fileOutputStream = new FileOutputStream(PATH);
                int count, total = 0;

                while ((count = dataInputStream.read(buffer)) != -1){
                    total += count;
                    fileOutputStream.write(buffer, 0, count);

                    if(total == fileSize){
                        break;
                    }
                }
                fileOutputStream.flush();
                fileOutputStream.close();

                System.out.println("File sent");
                
                new Thread(new FileSender(this, fileName, server)).start();
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
    }

    public void transferAnswer(String resultFilePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(resultFilePath);
            byte [] buffer = new byte[64*1024];
            int count;
            while((count = fileInputStream.read(buffer)) != -1){
                dataOutputStream.write(buffer, 0, count);
            }
            dataOutputStream.flush();
            fileInputStream.close();
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        System.out.println("Answer sent.");
    }
}