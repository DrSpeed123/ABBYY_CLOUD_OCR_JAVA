import java.io.BufferedInputStream;
import java.io.File;

/**
 * Created by DRSPEED-PC on 29.06.2017.
 */
public class FileSender implements Runnable{
    private String fileName;
    private Server server;
    ClientHandler handler;

    public FileSender(ClientHandler handler, String fileName, Server server) {
        this.fileName = fileName;
        this.server = server;
        this.handler = handler;
    }

    @Override
    public void run() {
        server.newFileFromClient(this.handler, this.fileName);
    }
}
