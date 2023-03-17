package server;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileStorage implements Serializable {

    private static final long serialVersionUID = 1L;

    // public final String STORAGE_FOLDER = ".src/server/data/";
    public final String STORAGE_FOLDER = "C:\\Users\\tadan\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data\\";

    public final Map<Long, String> ID_FILE_NAME_MAP = new HashMap<>();

    public FileStorage() {
        try {
            Files.createDirectories(Path.of(STORAGE_FOLDER));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the file exists in the storage
     *
     * @param fileName the name of the file to check.
     * @return true if the file exits; false if the file does not exist or its existence cannot be determined
     */
    public boolean isExists(String fileName) {
        long fileId = fileName.hashCode();
        if (ID_FILE_NAME_MAP.containsKey(fileId) ^ Files.exists(Path.of(STORAGE_FOLDER, fileName))) {
            Main.logger.warning("There is a disagreement between the data from the disk"
                    + "and the data from the table in File Storage."
                    + "File name: " + fileName);
            return true;

        } else return ID_FILE_NAME_MAP.containsKey(fileId);
    }

    /**
     * Checks if the file exists in the storage
     *
     * @param fileId the ID of the file to check.
     * @return true if the file exits; false if the file does not exist or its existence cannot be determined
     */
    public boolean isExists(long fileId) {
        String fileName = ID_FILE_NAME_MAP.get(fileId);
        if (fileName != null) {
            if (!Files.exists(Path.of(STORAGE_FOLDER, fileName))) {
                Main.logger.warning("There is a disagreement between the data from the disk"
                        + "and the data from the table in File Storage."
                        + "File name: " + fileName);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a file to the storage
     *
     * @param fileName the name of the file to add.
     * @param fileBytes bytes of the file.
     * @return "fileID" if the file is successfully added; 0 if the file has not been added due to some exceptions
     */
    public long addFile(String fileName, byte[] fileBytes) {
        File file = new File(STORAGE_FOLDER + fileName);
        long id = fileName.hashCode();
        if (!isExists(fileName)) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            ) {
                bufferedOutputStream.write(fileBytes);
                ID_FILE_NAME_MAP.put(id, fileName);
                System.out.println("File: " + fileName + " ID: " + id);
                return id;
            } catch (FileNotFoundException e) {
                e.fillInStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Removes a file from the storage
     *
     * @param fileId the id of the file to remove.
     * @return true if the file is successfully removed; false if the has not been removed due to some exceptions
     */
    public boolean removeFile(long fileId) {
        String fileName = ID_FILE_NAME_MAP.get(fileId);
        File file = new File(STORAGE_FOLDER + fileName);
        return file.delete();
    }

    /**
     *  Get a file bytes from storage
     *
     * @param fileId the ID of the file to get.
     * @return bytes[] if the file exists; null if the file does not exist
     */
    public byte[] getFile(long fileId) {
        String fileName = ID_FILE_NAME_MAP.get(fileId);
        File file = new File(STORAGE_FOLDER + fileName);
        try (FileInputStream fileInputStream = new FileInputStream(file);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ) {
            byte[] fileBytes = bufferedInputStream.readAllBytes();
            return fileBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
