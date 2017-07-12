package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * Created by DRSPEED-PC on 30.06.2017.
 */
public class MockClientMain {


    public static void main(String[] args) {
        String server = "0.0.0.0";
        int port = 3334;
        String PATH = "F:\\GIT\\ABBYY_OCR\\Abbyy.Ocrsdk.client\\PICTURES\\IMG_1710.JPG";
        String resultPath = "F:\\GIT\\ABBYY_OCR\\Abbyy.Ocrsdk.client\\PICTURES\\IMG_0502.JPG";

        try {
            Socket socket = new Socket(server, port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            File file = new File(PATH);
            dataOutputStream.writeLong(file.length());
            dataOutputStream.writeUTF(file.getName());

            System.out.println("Имя файла: " + file.getName());
            System.out.println("Размер: " + file.length());

            FileInputStream fileInputStream = new FileInputStream(file);
            byte [] buffer = new byte[64*1024];
            int count;

            while((count = fileInputStream.read(buffer)) != -1){
                dataOutputStream.write(buffer, 0, count);
            }

//            dataOutputStream.flush();
            fileInputStream.close();
            Thread.sleep(10000);
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }
}
