package server;


import utils.MyLogger;
import utils.SerializationUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.logging.Logger;

public class Main {
    protected static final String address = "127.0.0.1";
    protected static final int serverPort = 23456;
    protected static final int fileServerPort = 23488;
    protected static FileStorage fileStorage = getAFileStorage();
    protected static Logger logger;

    protected static final String dbSerializationFilePath = "C:\\Users\\tadan\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data\\db.data";


    protected static Boolean isRunning = true;
    protected static ServerSocket server;

    public static void main(String[] args) {
        System.out.println("Server started!");

        MyLogger myLogger = new MyLogger();
        logger = myLogger.logger;

        logger.info("Server started! %s:%d".formatted(address, serverPort));
        try {
            server = new ServerSocket(serverPort, 50, InetAddress.getByName(address));
            while (isRunning) {
                MainSession mainSessionForClient = new MainSession(server.accept());
                mainSessionForClient.start();
            }
        }
        catch (SocketException e) {

        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Got exception in server/Main");
        } finally {
            try {
                SerializationUtils.serialize(fileStorage, dbSerializationFilePath);
            } catch (IOException e) {
                System.out.println("DB SERIALIZATION ERROR");
            }
        }
    }

    public static FileStorage getAFileStorage() {
        FileStorage tempFileStorage;
        try {
            tempFileStorage = (FileStorage) SerializationUtils.deserialize(dbSerializationFilePath);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            tempFileStorage = new FileStorage();
        }
        return tempFileStorage;
    }
}