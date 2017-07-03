import com.sun.jersey.core.header.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

/**
 * Created by DRSPEED-PC on 02.07.2017.
 */


public class RestServer {
    public String INPUT_FILE_DIRECTORY = "F:\\GIT\\OCR_WEB_REST\\TEMP\\IN\\";
    public String ANSWER_FILE_DIRECTORY = "F:\\GIT\\OCR_WEB_REST\\TEMP\\OUT\\";
    public int sleep = 2000;
    public String fileName;

    @POST
    @Path("/fileupload")  //Your Path or URL to call this service
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    public Response uploadFile(
            @DefaultValue("true") @FormDataParam("enabled") boolean enabled,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception {
        fileName = fileDetail.getFileName();
        String uploadedFileLocation = INPUT_FILE_DIRECTORY + fileName;
        System.out.println("Upload file to: " + uploadedFileLocation);
        File objFile=new File(uploadedFileLocation);
        if(objFile.exists())
        {
            objFile.delete();

        }

        saveToFile(uploadedInputStream, uploadedFileLocation);

//        recognition(uploadedFileLocation);
//        String output = "File uploaded via Jersey based RESTFul Webservice to: " + uploadedFileLocation + ". Result recieved from cloud.";

        Recognition recognition = new Recognition(uploadedFileLocation);
        while (!recognition.isTaskComplete){
            Thread.sleep(sleep);
        }
        System.out.println("Result recieved from cloud.");

        String answerFileLocation = recognition.resultFilePath;
        File file = new File(answerFileLocation);
        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition", "attachment;filename=");
        return response.build();

//        return Response.status(200).entity(output).build();

    }

    @GET
    @Path("/downloadresult")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadFile() {

        String answerFileLocation = ANSWER_FILE_DIRECTORY + fileName;
        File file = new File(answerFileLocation);
        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition", "attachment;filename=");
        return response.build();
    }

//    @GET
//    @Path("/downloadresult")
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response downloadResult(
//            @DefaultValue("true") @FormDataParam("enabled") boolean enabled,
//            @FormDataParam("file") OutputStream downloadOutputStream,
//            @FormDataParam("file") FormDataContentDisposition fileDetail
//    ){
//        String answerFileLocation = ANSWER_FILE_DIRECTORY + fileDetail.getFileName();
//        try {
//            FileInputStream fileInputStream = null;
//            fileInputStream = new FileInputStream(answerFileLocation);
//
//
//            fileInputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String output = "Done";
//
//        return Response.status(200).entity(output).build();
//    }

    /**
     * Save POST to file
     * @param uploadedInputStream - Input stream.
     * @param uploadedFileLocation - File location.
     */
    private void saveToFile(InputStream uploadedInputStream, String uploadedFileLocation){
        try {
            OutputStream out = null;
            int read = 0;
            byte[] bytes = new byte[64*1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start recognition.
     * @param filePath - Path to file for recognition class.
     */
    private void recognition(String filePath){
        try {
            Recognition recognition = new Recognition(filePath);
            while (!recognition.isTaskComplete){
                Thread.sleep(sleep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Result recieved from cloud.");
    }
}
