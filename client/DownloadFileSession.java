package client;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class DownloadFileSession extends Thread {
    private final String fileServerIP;
    private final int fileServerPort;
    private final File file;

    public DownloadFileSession(String fileServerIP, int fileServerPort, String fileName) {
        this.fileServerIP = fileServerIP;
        this.fileServerPort = fileServerPort;
        this.file = new File(Main.STORAGE_FOLDER + fileName);
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(InetAddress.getByName(fileServerIP), fileServerPort);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        ) {
            int length = input.readInt();
            if(length>0) {
                byte[] fileBytes = new byte[length];
                input.readFully(fileBytes, 0, fileBytes.length);
                bufferedOutputStream.write(fileBytes);
                System.out.println("File saved on the hard drive!");
            } else {
                System.out.println("File length is 0");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got Exception in client/DownloadFileSession/run()");
        }
    }
}
