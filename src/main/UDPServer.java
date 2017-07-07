package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by DRSPEED-PC on 08.07.2017.
 */
public class UDPServer {
    DatagramSocket datagramSocket = null;

    public UDPServer(int serverPort){
        try {
            // create the server UDP end point
            datagramSocket = new DatagramSocket(serverPort);
        } catch (SocketException ExceSocket)
        {
            System.out.println("Socket creation error : "+ ExceSocket.getMessage());
        }
        System.out.println("UDP Socket (end point) created");
    }

    public synchronized void clientRequest()
    {
        DatagramPacket recievedPacket;
        DatagramPacket sendPacket;
        InetAddress clientAddress;
        int clientPort;
        byte[] outBuffer;
        byte[] inBuffer;

        // create some space for the text to send and recieve data
        outBuffer = new byte[500];
        inBuffer = new byte[50];

        try {
            // create a place for the client to send data too
            recievedPacket = new DatagramPacket(inBuffer, inBuffer.length);
            // wait for a client to request a connection
            datagramSocket.receive(recievedPacket);
            System.out.println("Client connected");

            // get the client details
            clientAddress = recievedPacket.getAddress();
            clientPort = recievedPacket.getPort();

            String message = "Server - client sent : " + new String(recievedPacket.getData(),0, recievedPacket.getLength());
            outBuffer = message.getBytes();

            System.out.println("Client data sent ("+message+")");
            // send some data to the client
            sendPacket = new DatagramPacket(outBuffer, outBuffer.length, clientAddress, clientPort);
            datagramSocket.send(sendPacket);

        } catch (IOException ExceIO)
        {
            System.out.println("Error with client request : "+ExceIO.getMessage());
        }
        // close the server socket
        datagramSocket.close();
    }
}
