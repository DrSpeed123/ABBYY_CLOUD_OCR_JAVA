import com.abbyy.ocrsdk.Client;
import com.abbyy.ocrsdk.ProcessingSettings;
import com.abbyy.ocrsdk.Task;
import com.abbyy.ocrsdk.TextFieldSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * Created by DRSPEED-PC on 19.04.2017.
 */
public class Recognition {
    private static Client restClient;
    private static String PATH = "F:\\GIT\\ABBYY_OCR\\Abbyy.Ocrsdk.client\\PICTURES\\";
    private static String outputPath = "F:\\GIT\\ABBYY_OCR\\Abbyy.Ocrsdk.client\\RESULTS\\";
    private static String pathToXSDSchema = "F:\\GIT\\ABBYY_OCR\\Abbyy.Ocrsdk.client\\TEMPLATES\\taskTemplate.xml";
    private static int sleep = 2000;
    private static String SETTINGS_PATH = "C:\\Users\\DRSPEED-PC\\Documents\\TEST_ABBYY\\TestDescription.xsd";
    public boolean isTaskComplete;
    public String resultFilePath;

    public Recognition(String fileName) throws Exception {
        System.out.println("Process documents using ABBYY Cloud OCR SDK.\n");
        long start = System.currentTimeMillis();

        if (!checkAppId()) {
            return;
        }

        restClient = new Client();
        restClient.serverUrl = "https://cloud.ocrsdk.com";
        restClient.applicationId = ClientSettings.APPLICATION_ID;
        restClient.password = ClientSettings.PASSWORD;

//        ProcessingSettings PSettings = new ProcessingSettings();
////        TextFieldSettings PSettings = new TextFieldSettings();
//        PSettings.setLanguage("Russian,English");
////        PSettings.setLanguage("Russian");
//        PSettings.setOutputFormat(ProcessingSettings.OutputFormat.xml);
//        PSettings.setProfile(ProcessingSettings.OCRProfile.textExtraction);
//        PSettings.setTextType("normal,ocrB");
//        PSettings.setImageSource(ProcessingSettings.ImageSource.scanner);
//        PSettings.setCorrectOrientation("false");
//        PSettings.setIsCorrectSkew("false");
//        PSettings.setIsReadBarcodes("false");
//        PSettings.setIsWriteRecognitionVariants("true");

        String filePath = PATH + fileName;
        this.resultFilePath = outputPath + fileName + ".xml";
        Task downloadPicture;
        Task getResult = null;
        isTaskComplete = false;
//        task = restClient.processTextField(PATH, PSettings);
//        task = restClient.processImage(PATH, PSettings);
        downloadPicture = restClient.submitImage(filePath,"");
//        System.out.println("Uploading file to URL: " + restClient.serverUrl + "/processImage?" + PSettings.asUrlParams() + "..");
        System.out.println("Uploading file to URL: " + restClient.serverUrl + "/submitImage..");

        long timeToUpload = System.currentTimeMillis() - start;
        System.out.println("Time To Upload: " + timeToUpload);

        try {
            getResult = restClient.processFields(downloadPicture.Id, pathToXSDSchema);
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }

//        String resultFilePath = outputPath + "result_" + getResult.Id + ".xml";


//        FileOutputStream resultFile = new FileOutputStream(new File(resultFilePath), true);


        waitAndDownloadResult(getResult, resultFilePath);
//        resultFile.close();
        long timeConsumedMillis = System.currentTimeMillis() - start;


        System.out.println("Task GUID = " + getResult.Id);
        System.out.println("Task Status = " + getResult.Status);
        System.out.println("Task DownloadUrl = " + getResult.DownloadUrl);

        System.out.println("Time consumed: " + timeConsumedMillis);
//        restClient.processFields(task.Id, PathToXSDSchema);
//        restClient.deleteTask(task.Id);
//        System.out.println("Task " + task.Id + " deleted");
        isTaskComplete = true;
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

            Thread.sleep(sleep);
            System.out.println("Waiting " + sleep + "ms..");
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
