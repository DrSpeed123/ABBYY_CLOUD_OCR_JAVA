package main;

/**
 * Created by DRSPEED-PC on 29.06.2017.
 */
public class FileSender implements Runnable{
    private String fileName;
    private TCPServer server;
    TCPClientHandler handler;

    public FileSender(TCPClientHandler handler, String fileName, TCPServer server) {
        this.fileName = fileName;
        this.server = server;
        this.handler = handler;
    }

    @Override
    public void run() {
        server.newFileFromClient(this.handler, this.fileName);
    }
}
