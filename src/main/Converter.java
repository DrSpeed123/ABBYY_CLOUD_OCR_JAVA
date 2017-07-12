package main;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;


/**
 * Created by DRSPEED-PC on 08.07.2017.
 */
public class Converter {
    static String OUTPUT_PATH = "F:\\GIT\\ABBYY_OCR\\Abbyy.Ocrsdk.client\\RESULTS\\";
    static String INPUT_PATH = "F:\\GIT\\ABBYY_OCR\\Abbyy.Ocrsdk.client\\PICTURES\\";

    public Converter(){
    }

    /**
     * Convert image file to Mat image for recognition.
     * @param fileName file name to convert.
     * @return
     * @throws IOException
     */
    public static Mat toMat(String fileName) throws IOException {
        BufferedImage bufferedImage;
        Mat mat;
        byte[] data;

        String path = INPUT_PATH + fileName;
        File file = new File(path);
        bufferedImage = ImageIO.read(file);

        data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer())
                .getData();
        mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);

        return mat;
    }

    /**
     * Convert Mat image to File to save.
     * @param mat image to convert.
     * @param fileName name of file created.
     * @return
     */
    public static File toFile(Mat mat, String fileName){
        String path = OUTPUT_PATH + fileName;
        File file = new File(path);
        BufferedImage bufferedImage;

        try {
            byte[] data = new byte[mat.cols() * mat.rows() * (int)mat.elemSize()];
            int type;
            mat.get(0, 0, data);

            if(mat.channels() == 1)
                type = BufferedImage.TYPE_BYTE_GRAY;
            else
                type = BufferedImage.TYPE_3BYTE_BGR;

            bufferedImage = new BufferedImage(mat.cols(), mat.rows(), type);
            mat.get(0, 0, ((DataBufferByte)bufferedImage.getRaster().getDataBuffer()).getData());

            if (!ImageIO.write(bufferedImage, "PNG", file)) {
                throw new RuntimeException("Unexpected error writing image");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() == type) {
            return original;
        }

        // Create a buffered image
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);

        // Draw the image onto the new buffer
        Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(original, 0, 0, null);
        }
        finally {
            g.dispose();
        }

        return image;
    }
}
