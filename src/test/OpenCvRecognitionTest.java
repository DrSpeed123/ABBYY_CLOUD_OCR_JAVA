package test;

import main.OpenCvRecognition;

import java.io.IOException;

/**
 * Created by DRSPEED-PC on 08.07.2017.
 */
public class OpenCvRecognitionTest {
    public static void main(String[] args) {
        String fileName = "1920X1080.jpg";
        try {
            OpenCvRecognition recognition = new OpenCvRecognition(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
