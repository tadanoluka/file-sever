package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class UploadFileSession extends Thread{
    private final Socket socket;
    private final String fileName;

    public UploadFileSession(Socket socketForFile, String fileName) {
        this.socket = socketForFile;
        this.fileName = fileName;

    }

    @Override
    public void run() {
        Main.logger.info("Upload file session started, " + socket);
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            int length = input.readInt();
            if(length>0) {
                byte[] fileBytes = new byte[length];
                input.readFully(fileBytes, 0, fileBytes.length);
                long fileID = Main.fileStorage.addFile(fileName, fileBytes);
                if (fileID != 0) {
                    output.writeUTF("200 " + fileID);
                } else {
                    output.writeUTF("403");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got exception in server/UploadFileSession/run()");
        }

    }
}
