import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
public class ThreadedCompare extends Thread {
    private final List<Path> pathsToCompareTo;
    private final List<Path> pathsToBeCompared;
    public ThreadedCompare (List<Path> pathsToBeCompared, List<Path> pathsToCompareTo) {
        this.pathsToBeCompared = pathsToBeCompared;
        this.pathsToCompareTo = pathsToCompareTo;
    }
    @Override
    public void run() {

        //Compare every File
        for (Path file1 : pathsToBeCompared) {
            for (Path file2 : pathsToCompareTo) {
                try {
                    if (sameContent(file1, file2)) {
                        List<Path> bothPaths = new ArrayList<>();
                        bothPaths.add(file1);
                        bothPaths.add(file2);

                        //here it is trying to add the values in the HashMap
                        if (Main.fileMap.containsKey(getMD5Sum(file1.toFile()))) {
                            if (!Main.fileMap.get(getMD5Sum(file1.toFile())).contains(file1)) {
                                Main.fileMap.get(getMD5Sum(file1.toFile())).add(file1);
                            }
                            if (!Main.fileMap.get(getMD5Sum(file1.toFile())).contains(file2)) {
                                Main.fileMap.get(getMD5Sum(file1.toFile())).add(file2);
                            }
                        } else {
                            Main.fileMap.put(getMD5Sum(file1.toFile()), bothPaths);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            //Update the Progress that can be found in The Main Class
            Main.progress += 1;
        }
        //Update the thread Completion Counter that can be found in the Main Class
        Main.completedThreads += 1;
    }
    private boolean sameContent(Path file1, Path file2) throws IOException {
        if (file1 != file2) {
            return Files.mismatch(file1, file2) == -1;
        }
        return false;
    }

    //this is used to get the MD5 String of one of the files (one of them is just fine since they both have the same value)
    private String getMD5Sum (File file) throws IOException {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        FileInputStream fis = new FileInputStream(file);

        byte[] dataBytes = new byte[1024];

        int unread = 0;
        while ((unread = fis.read(dataBytes)) != -1) {
            messageDigest.update(dataBytes, 0, unread);
        }
        byte[] mdbytes = messageDigest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte mdbyte : mdbytes) {
            sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
