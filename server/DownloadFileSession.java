package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class DownloadFileSession extends Thread {
    private final Socket socket;
    private final long fileId;

    public DownloadFileSession(Socket socketForFile, long fileId) {
        this.socket = socketForFile;
        this.fileId = fileId;

    }

    @Override
    public void run() {
        Main.logger.info("DownloadFileSession started, " + socket);
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            byte[] fileBytes = Main.fileStorage.getFile(fileId);
            int length = fileBytes.length;
            output.writeInt(length);
            output.write(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got exception in server/DownloadFileSession/run()");
        }

    }
}
