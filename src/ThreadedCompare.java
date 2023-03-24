import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class ThreadedCompare extends Thread {
    private final List<Path> pathsToCompareTo;
    //private HashMap<String, List<Path>> threadFileMap = new HashMap<>();

    public ThreadedCompare (List<Path> pathsToCompareTo) {

        this.pathsToCompareTo = pathsToCompareTo;
    }

    @Override
    public void run() {

        for (Path file : pathsToCompareTo) {
            List<Path> fileArray = new ArrayList<>();
            assert fileArray != null;
            fileArray.add(file);
            String MD5;
            try {
                MD5 = getMD5Sum(file.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            if (Main.fileMap.containsKey(MD5)) {
                fileArray.addAll(Main.fileMap.get(MD5));
                Main.fileMap.put(MD5, fileArray);
            } else {
                Main.fileMap.put(MD5, fileArray);
            }


            Main.progress++;
        }

        
        Main.completedThreads++;





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
