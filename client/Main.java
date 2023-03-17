package client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Time;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class Main {
    protected static final String address = "127.0.0.1";
    protected static final int serverPort = 23456;

    //public static final String STORAGE_FOLDER = ".src/client/data/";
    public static final String STORAGE_FOLDER = "C:\\Users\\tadan\\IdeaProjects\\File Server\\File Server\\task\\src\\client\\data\\";

    private static String requestType = "";
    private static String serverFileName = "";
    private static String localFileName = "";
    private static File localFile = null;

    private static boolean isRunning = true;

    public static void main(String[] args) {
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try (Socket socket = new Socket(InetAddress.getByName(address), serverPort);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            Scanner scanner = new Scanner(System.in);
            while (isRunning) {
                String request = getRequest(scanner);
                output.writeUTF(request);
                switch (requestType) {
                    case "GET" -> {
                        int statusCode = input.readInt();
                        switch (statusCode) {
                            case 200 -> getAction(input, scanner);
                            case 404 -> System.out.println("The response says that this file is not found!");
                        }
                    }
                    case "PUT" -> {
                        int statusCode = input.readInt();
                        switch (statusCode) {
                            case 200 -> putAction(input);
                            case 403 -> System.out.println("The response says that saving the file was forbidden!");
                        }
                    }
                    case "DELETE" -> {
                        int statusCode = input.readInt();
                        switch (statusCode) {
                            case 200 -> System.out.println("The response says that this file was deleted successfully!");
                            case 404 -> System.out.println("The response says that this file is not found!");
                        }
                    }
                    case "EXIT" -> {
                        isRunning = false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got Exception in client/Main/main");

            // Should be removed
            main(new String[]{});
        }
    }


    public static String getRequest(Scanner scanner) {
        requestType = "";
        localFile = null;
        StringBuilder stringBuilder = new StringBuilder();
        System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ");
        String actionType = scanner.nextLine().toLowerCase();
        switch (actionType) {
            case "1" -> {
                System.out.println("Do you want to get the file by name or by id (1 - name, 2 - id): ");
                String getType = scanner.nextLine();
                String fileNameOrId = "";
                switch (getType) {
                    case "1" -> {
                        requestType = "GET";
                        getType = "BY_NAME";
                        System.out.println("Enter name: ");
                        fileNameOrId = scanner.nextLine();
                    }
                    case "2" -> {
                        requestType = "GET";
                        getType = "BY_ID";
                        System.out.println("Enter id: ");
                        fileNameOrId = scanner.nextLine();
                    }
                }
                stringBuilder.append(requestType).append(" ");
                stringBuilder.append(getType).append(" ");
                stringBuilder.append(fileNameOrId);
            }
            case "2" -> {
                System.out.println("Enter name of the file: ");
                localFileName = scanner.nextLine();
                System.out.println("Enter name of the file to be saved on server: ");
                serverFileName = scanner.nextLine();
                if (Objects.equals(serverFileName, "")) {
                    serverFileName = localFileName;
                }

                if (Files.exists(Path.of(STORAGE_FOLDER + localFileName))) {
                    localFile = new File(STORAGE_FOLDER + localFileName);
                    requestType = "PUT";
                    stringBuilder.append(requestType).append(" ");
                    stringBuilder.append(serverFileName);
                } else {
                    System.out.println("File not found");
                }
            }
            case "3" -> {
                System.out.println("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
                String getType = scanner.nextLine();
                String fileNameOrId = "";
                switch (getType) {
                    case "1" -> {
                        requestType = "DELETE";
                        getType = "BY_NAME";
                        System.out.println("Enter name: ");
                        fileNameOrId = scanner.nextLine();
                    }
                    case "2" -> {
                        requestType = "DELETE";
                        getType = "BY_ID";
                        System.out.println("Enter id: ");
                        fileNameOrId = scanner.nextLine();
                    }
                }
                stringBuilder.append(requestType).append(" ");
                stringBuilder.append(getType).append(" ");
                stringBuilder.append(fileNameOrId);
            }
            case "exit" -> {
                requestType = "EXIT";
                stringBuilder.append(requestType);
            }
        }
        return stringBuilder.toString();
    }

    public static void putAction(DataInputStream mainSessionInputStream) {
        try {
            int fileServerPort = mainSessionInputStream.readInt();
            UploadFileSession uploadFileSession = new UploadFileSession(address, fileServerPort, localFile);
            uploadFileSession.start();
            // This shit should be removed. Used only for passing autotests
            try {
                uploadFileSession.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // End of shitty part
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got exception in client/Main/putAction");
        }

    }

    private static void getAction(DataInputStream mainSessionInputStream, Scanner scanner) {
        System.out.println("The file was downloaded! Specify a name for it: ");
        String fileName = scanner.nextLine();
        try {
            int fileServerPort = mainSessionInputStream.readInt();
            DownloadFileSession downloadFileSession = new DownloadFileSession(address, fileServerPort, fileName);
            downloadFileSession.start();
            // This shit should be removed. Used only for passing autotests
            try {
                downloadFileSession.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // End of shitty part
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got exception in client/Main/putAction");
        }
    }


}
