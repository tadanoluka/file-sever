package server;

import java.io.*;
import java.net.*;

public class MainSession extends Thread {
    private final Socket socket;
    private boolean isRunning = true;

    public MainSession(Socket socketForClient) {
        this.socket = socketForClient;
    }

    @Override
    public void run() {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            Main.logger.info("Client Connected: " + socket);
            while (isRunning) {
                String request = input.readUTF();
                Main.logger.info("Received a request: " + request + "\nFrom :" + socket);
                String[] splitRequest = request.split("\\s+");
                if (splitRequest.length > 0) {
                    String requestType = splitRequest[0];
                    switch (requestType) {
                        case "GET" -> getOperation(splitRequest, output);
                        case "PUT" -> putOperation(splitRequest, output);
                        case "DELETE" -> removeOperation(splitRequest, output);
                        case "EXIT" -> {
                            isRunning = false;
                            Main.isRunning = false;
                            Main.server.close();
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got exception in server/MainSession/run()");
        }
    }

    public void putOperation(String[] splitRequest, DataOutputStream output) throws IOException {
        if (splitRequest.length < 2) {
            System.out.println("The file name in the request cannot be parsed. The request is too short.");
            return;
        }
        String fileName = splitRequest[1];
        if (Main.fileStorage.isExists(fileName)) {
            output.writeInt(403);
            System.out.println("The file name already exists");
            return;
        } else {
            output.writeInt(200);
        }
        output.writeInt(Main.fileServerPort);
        Main.logger.info("File port(%d) was sent to: ".formatted(Main.fileServerPort) + socket);
        Main.logger.info("Staring new ServerSocket for files");
        try(ServerSocket fileServer = new ServerSocket(Main.fileServerPort, 50, InetAddress.getByName("127.0.0.1"))) {
            Main.logger.info("ServerSocket for files started");
            UploadFileSession uploadFileSession = new UploadFileSession(fileServer.accept(), fileName);
            uploadFileSession.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got exception in server/MainSession/putOperation");
        }
    }

    public void getOperation(String[] splitRequest, DataOutputStream output) throws IOException {
        if (splitRequest.length < 3) {
            System.out.println("The file name or id in the request cannot be parsed. The request is too short.");
            return;
        }
        String getType = splitRequest[1];
        long fileID;
        switch (getType) {
            case "BY_NAME" -> {
                String fileName = splitRequest[2];
                fileID = fileName.hashCode();
                if (Main.fileStorage.isExists(fileName)) {
                    output.writeInt(200);
                } else {
                    output.writeInt(404);
                    return;
                }
            }
            case "BY_ID" -> {
                try {
                    fileID = Long.parseLong(splitRequest[2]);
                    if (Main.fileStorage.isExists(fileID)) {
                        output.writeInt(200);
                    } else {
                        output.writeInt(404);
                        return;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    output.writeInt(404);
                    return;
                }
            }
            default -> {
                return;
            }
        }
        output.writeInt(Main.fileServerPort);
        Main.logger.info("File port(%d) was sent to: ".formatted(Main.fileServerPort) + socket);
        Main.logger.info("Staring new ServerSocket for files");
        try(ServerSocket fileServer = new ServerSocket(Main.fileServerPort, 50, InetAddress.getByName("127.0.0.1"))) {
            DownloadFileSession downloadFileSession = new DownloadFileSession(fileServer.accept(), fileID);
            downloadFileSession.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got exception in server/MainSession/putOperation");
        }
    }

    private void removeOperation(String[] splitRequest, DataOutputStream output) throws IOException {
        if (splitRequest.length < 3) {
            System.out.println("The file name or id in the request cannot be parsed. The request is too short.");
            return;
        }
        String deleteType = splitRequest[1];
        long fileID;
        switch (deleteType) {
            case "BY_NAME" -> {
                String fileName = splitRequest[2];
                fileID = fileName.hashCode();
            }
            case "BY_ID" -> {
                try {
                    fileID = Long.parseLong(splitRequest[2]);
                } catch (NumberFormatException e) {
                    fileID = -1;
                    e.printStackTrace();
                }
            }
            default -> {
                fileID = -1;
            }
        }
        boolean isDeleted = Main.fileStorage.removeFile(fileID);
        if (isDeleted) {
            output.writeInt(200);
        } else {
            output.writeInt(404);
        }
    }
}
