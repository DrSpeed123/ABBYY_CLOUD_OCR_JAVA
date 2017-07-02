import com.sun.jersey.core.header.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

/**
 * Created by DRSPEED-PC on 02.07.2017.
 */


public class RestServer {
    public String INPUT_FILE_DIRECTORY = "F:\\GIT\\ABBYY_OCR\\Abbyy.Ocrsdk.client\\PICTURES\\IN\\";
    public int sleep = 2000;

    @POST
    @Path("/fileupload")  //Your Path or URL to call this service
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
    @DefaultValue("true") @FormDataParam("enabled") boolean enabled,
    @FormDataParam("file") InputStream uploadedInputStream,
    @FormDataParam("file") FormDataContentDisposition fileDetail) {
        String uploadedFileLocation = INPUT_FILE_DIRECTORY + fileDetail.getFileName();
        System.out.println("Upload file to: " + uploadedFileLocation);
        File objFile=new File(uploadedFileLocation);
        if(objFile.exists())
        {
            objFile.delete();

        }

        saveToFile(uploadedInputStream, uploadedFileLocation);

        String output = "File uploaded via Jersey based RESTFul Webservice to: " + uploadedFileLocation;

        recognition(uploadedFileLocation);

        return Response.status(200).entity(output).build();

    }
    private void saveToFile(InputStream uploadedInputStream,
            String uploadedFileLocation)
    {
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
    private void recognition(String filePath){
        Recognition recognition = null;
        try {
            recognition = new Recognition(filePath);
            while (!recognition.isTaskComplete){
                Thread.sleep(sleep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Result recieved from cloud.");
    }
}
