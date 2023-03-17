package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

class UploadFileSession extends Thread {
    private final String fileServerIP;
    private final int fileServerPort;
    private final File fileToUpload;

    public UploadFileSession(String fileServerIP, int fileServerPort, File fileToUpload) {
        this.fileServerIP = fileServerIP;
        this.fileServerPort = fileServerPort;
        this.fileToUpload = fileToUpload;
    }

    @Override
    public void run() {
        try {
            TimeUnit.MILLISECONDS.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try (Socket socket = new Socket(InetAddress.getByName(fileServerIP), fileServerPort);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             FileInputStream fileInputStream = new FileInputStream(fileToUpload);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ) {
            byte[] fileBytes = bufferedInputStream.readAllBytes();
            output.writeInt(fileBytes.length);
            output.write(fileBytes);

            String[] splitAnswer = input.readUTF().split("\\s+");
            if (splitAnswer.length > 0) {
                String statusCode = splitAnswer[0];
                switch (statusCode) {
                    case "200" -> {
                        String fileID = splitAnswer[1];
                        String logMessage = "Response says that file is saved! ID = %s".formatted(fileID);
                        System.out.println(logMessage);
                    }
                    case "403" -> System.out.println("The response says that saving the file was forbidden!");
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got Exception in client/UploadFileSession/run()");
        }
    }
}
