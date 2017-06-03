import com.abbyy.ocrsdk.Client;
import com.abbyy.ocrsdk.ProcessingSettings;
import com.abbyy.ocrsdk.Task;
import com.abbyy.ocrsdk.TextFieldSettings;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * Created by DRSPEED-PC on 19.04.2017.
 */
public class RecognitionTestApp {
    private static Client restClient;
    private static String PATH = "C:\\Users\\DRSPEED-PC\\Pictures\\IMG_1710.JPG";
    private static String outputPath = "C:\\Users\\DRSPEED-PC\\Documents\\TEST_ABBYY\\11.xml";
    private static String PathToXSDSchema = "C:\\Users\\DRSPEED-PC\\Documents\\TEST_ABBYY\\Template1.xml";
    private static int Sleep = 2000;
    private static String SETTINGS_PATH = "C:\\Users\\DRSPEED-PC\\Documents\\TEST_ABBYY\\TestDescription.xsd";

    public static void main(String[] args) throws Exception {
        System.out.println("Process documents using ABBYY Cloud OCR SDK.\n");
        long start = System.currentTimeMillis();

        if (!checkAppId()) {
            return;
        }

        restClient = new Client();
        restClient.serverUrl = "https://cloud.ocrsdk.com";
        restClient.applicationId = ClientSettings.APPLICATION_ID;
        restClient.password = ClientSettings.PASSWORD;

        ProcessingSettings PSettings = new ProcessingSettings();
//        TextFieldSettings PSettings = new TextFieldSettings();
        PSettings.setLanguage("Russian,English");
//        PSettings.setLanguage("Russian");
        PSettings.setOutputFormat(ProcessingSettings.OutputFormat.xml);
        PSettings.setProfile(ProcessingSettings.OCRProfile.textExtraction);
        PSettings.setTextType("normal,ocrB");
        PSettings.setImageSource(ProcessingSettings.ImageSource.scanner);
        PSettings.setCorrectOrientation("false");
        PSettings.setIsCorrectSkew("false");
        PSettings.setIsReadBarcodes("false");
//        PSettings.setIsWriteRecognitionVariants("true");


        Task task = null;
//        task = restClient.processTextField(PATH, PSettings);
        task = restClient.processImage(PATH, PSettings);
//        task = restClient.submitImage(PATH,"");
        System.out.println("Uploading file to URL: " + restClient.serverUrl + "/processImage?" + PSettings.asUrlParams() + "..");


        long timeToUpload = System.currentTimeMillis() - start;
        System.out.println("Time To Upload: " + timeToUpload);
        task = restClient.processFields(task.Id, PathToXSDSchema);

        waitAndDownloadResult(task, outputPath);
        long timeConsumedMillis = System.currentTimeMillis() - start;

        System.out.println("Task GUID = " + task.Id);
        System.out.println("Task Status = " + task.Status);
        System.out.println("Task DownloadUrl = " + task.DownloadUrl);

        System.out.println("Time consumed: " + timeConsumedMillis);
//        restClient.processFields(task.Id, PathToXSDSchema);
//        restClient.deleteTask(task.Id);
//        System.out.println("Task " + task.Id + " deleted");
    }
    /**
     * Check that user specified application id and password.
     *
     * @return false if no application id or password
     */
    private static boolean checkAppId() {
        String appId = ClientSettings.APPLICATION_ID;
        String password = ClientSettings.PASSWORD;
        if (appId.isEmpty() || password.isEmpty()) {
            System.out
                    .println("Error: No application id and password are specified.");
            System.out.println("Please specify them in ClientSettings.java.");
            return false;
        }
        return true;
    }
    private static Task waitForCompletion(Task task) throws Exception {
        // Note: it's recommended that your application waits
        // at least 2 seconds before making the first getTaskStatus request
        // and also between such requests for the same task.
        // Making requests more often will not improve your application performance.
        // Note: if your application queues several files and waits for them
        // it's recommended that you use listFinishedTasks instead (which is described
        // at http://ocrsdk.com/documentation/apireference/listFinishedTasks/).
        while (task.isTaskActive()) {

            Thread.sleep(Sleep);
            System.out.println("Waiting " + Sleep + "ms..");
            task = restClient.getTaskStatus(task.Id);
        }
        return task;
    }
    /**
     * Wait until task processing finishes and download result.
     */
    private static void waitAndDownloadResult(Task task, String outputPath)
            throws Exception {
        task = waitForCompletion(task);

        if (task.Status == Task.TaskStatus.Completed) {
            System.out.println("Downloading..");
            restClient.downloadResult(task, outputPath);
            System.out.println("Ready");
        } else if (task.Status == Task.TaskStatus.NotEnoughCredits) {
            System.out.println("Not enough credits to process document. "
                    + "Please add more pages to your application's account.");
        } else {
            System.out.println("Task failed");
        }

    }
}
