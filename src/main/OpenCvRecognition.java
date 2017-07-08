package main;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static main.Converter.*;

/**
 * Created by DRSPEED-PC on 08.07.2017.
 */
public class OpenCvRecognition {

    Mat oldFrame;
    Point clickedPoint = new Point(0, 0);
    boolean inverse = false;

    /**
     * Recognition static image from image file.
     * @param fileName name of file to recognition.
     * @throws IOException
     */
    public OpenCvRecognition(String fileName) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat mat = toMat(fileName);
//        doBackgroundRemovalAbsDiff(mat);
//        doBackgroundRemovalFloodFill(mat);
//        doBackgroundRemoval(mat);
//        doCanny(mat);
        doSobel(mat);
//        File outputFile = toFile(mat, "Test1.png");
    }

    private void doBackgroundRemovalAbsDiff(Mat currFrame)
    {
        Mat greyImage = new Mat();
        Mat foregroundImage = new Mat();

        Core.absdiff(currFrame, currFrame, foregroundImage);
        Imgproc.cvtColor(foregroundImage, greyImage, Imgproc.COLOR_BGR2GRAY);
        int thresh_type = Imgproc.THRESH_BINARY_INV;
        if (inverse)
            thresh_type = Imgproc.THRESH_BINARY;

        Imgproc.threshold(greyImage, greyImage, 10, 255, thresh_type);
        currFrame.copyTo(foregroundImage, greyImage);

        oldFrame = currFrame;

        File outputFile = toFile(greyImage, "doBackgroundRemovalAbsDiff.png");
//        return foregroundImage;

    }

    /**
     * Background removal. Mat to grayscale.
     * @param frame
     * @return
     */
    private void doBackgroundRemovalFloodFill(Mat frame)
    {

        Scalar newVal = new Scalar(255, 255, 255);
        Scalar loDiff = new Scalar(50, 50, 50);
        Scalar upDiff = new Scalar(50, 50, 50);
        Point seedPoint = clickedPoint;
        Mat mask = new Mat();
        Rect rect = new Rect();

        // Imgproc.floodFill(frame, mask, seedPoint, newVal);
        Imgproc.floodFill(frame, mask, seedPoint, newVal, rect, loDiff, upDiff, Imgproc.FLOODFILL_FIXED_RANGE);

        File outputFile = toFile(mask, "doBackgroundRemovalFloodFill.png");
//        return frame;
    }

    private void doBackgroundRemoval(Mat frame)
    {
        // init
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();

        int thresh_type = Imgproc.THRESH_BINARY_INV;
        if (inverse)
            thresh_type = Imgproc.THRESH_BINARY;

        // threshold the image with the average hue value
        hsvImg.create(frame.size(), CvType.CV_8U);
        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV,1);
        Core.split(hsvImg, hsvPlanes);

        // get the average hue value of the image
        double threshValue = this.getHistAverage(hsvImg, hsvPlanes.get(0));

        Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, thresh_type);

        Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);

        // create the new image
        Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        frame.copyTo(foreground, thresholdImg);

        File outputFile = toFile(foreground, "doBackgroundRemoval.png");
//        return foreground;
    }

    /**
     * Get the average hue value of the image starting from its Hue channel
     * histogram
     *
     * @param hsvImg
     *            the current frame in HSV
     * @param hueValues
     *            the Hue component of the current frame
     * @return the average Hue value
     */
    private double getHistAverage(Mat hsvImg, Mat hueValues)
    {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range of Hue values
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // get the average Hue value of the image
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < 180; h++)
        {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        return average = average / hsvImg.size().height / hsvImg.size().width;
    }

    /**
     * Apply Canny
     *
     * @param frame
     *            the current frame
     * @return an image elaborated with Canny
     */
    private void doCanny(Mat frame)
    {
        int threshold = 60;
        // init
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY,1);

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        frame.copyTo(dest, detectedEdges);

        File outputFile = toFile(dest, "doCanny.png");
//        return dest;
    }

    /**
     * Apply Sobel
     *
     * @param frame
     *            the current frame
     * @return an image elaborated with Sobel derivation
     */
    private void doSobel(Mat frame)
    {
        // init
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();
        int threshold = 70;

        // reduce noise with a 3x3 kernel
        Imgproc.GaussianBlur(frame, frame, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);

        // convert to grayscale
        if(!frame.empty()){
            Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGRA2GRAY,1);
        }
        else             System.out.println("cannot access frame");

        // Gradient X
//         Imgproc.Sobel(grayImage, grad_x, ddepth, 1, 0, 3, scale,
//         threshold, Core.BORDER_DEFAULT );
        Imgproc.Sobel(grayImage, grad_x, ddepth, 1, 0);
        Core.convertScaleAbs(grad_x, abs_grad_x);

        // Gradient Y
//         Imgproc.Sobel(grayImage, grad_y, ddepth, 0, 1, 3, scale,
//         threshold, Core.BORDER_DEFAULT );
        Imgproc.Sobel(grayImage, grad_y, ddepth, 0, 1);
        Core.convertScaleAbs(grad_y, abs_grad_y);

        // Total Gradient (approximate)
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, -20, detectedEdges);
//         Core.addWeighted(grad_x, 0.5, grad_y, 0.5, 0, detectedEdges);

        File outputFile = toFile(detectedEdges, "doSobel.png");
//        return detectedEdges;

    }
}
