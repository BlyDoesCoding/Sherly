import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static int completedThreads = 0;
    public static int progress = 0;
    public static HashMap<String, List<Path>> fileMap = new HashMap<>();
    public static void main(String[] args) throws InterruptedException {
        boolean doTheColorThingy = false;
        boolean showProgress = false;
        boolean deleteDups = false;
        boolean recordFolder = false;
        boolean showDebug = false;

        List<String> paths = new ArrayList<>();

        for(String i : args)
        {
            if (recordFolder) {
                if(Files.isDirectory(Paths.get(i))) {
                    paths.add(i);
                } else {recordFolder = false;}
            }
            if (i.equalsIgnoreCase("-c") || i.equalsIgnoreCase("-color")) { doTheColorThingy = true;}
            if (i.equalsIgnoreCase("-p") || i.equalsIgnoreCase("-progress")) { showProgress = true;}
            if (i.equalsIgnoreCase("-f") || i.equalsIgnoreCase("-folder")) { recordFolder = true;}
            if (i.equalsIgnoreCase("-d") || i.equalsIgnoreCase("-delete")) { deleteDups = true;}
            if (i.equalsIgnoreCase("-debug")) { showDebug = true;}

        }

        if (paths.size() == 0) {
            System.out.println("Aborted, no Folders Found!");
            return;
        }

        if (showDebug) {
            System.out.println("Folders: " + paths.size());
            System.out.println("Color: " + doTheColorThingy);
            System.out.println("Delete: " + deleteDups);
            System.out.println("Progressbar: " + showProgress);
        }


        List<Path> pathList = new ArrayList<>();
        List<Path> allFiles = new ArrayList<>();

        for (String folder : paths) {
            try (Stream<Path> stream = Files.walk(Paths.get(folder))) {
                pathList = stream.map(Path::normalize).filter(Files::isRegularFile).collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            allFiles.addAll(pathList);
        }

        // calculations for multithreading

        //The number of Cores or better said Threads that can be used
        int availableThreads = Runtime.getRuntime().availableProcessors();

        //just the number of All Files in all Folders taken from the Args
        int filesToBeDone = allFiles.size();

        //Every Thread that is going to be started gets a range of files
        //They are seperated and are called sections
        int sections = filesToBeDone / availableThreads;

        for (int i = 1; i <= availableThreads; i++) {

            List<Path> sectionedList = new ArrayList<>();

            //Here the different Threads are being started
            //Usually the separation gives the first threads the same number of files to be working on and the last one is given all the files that could not be separetated
            if (i == availableThreads) {
                for (int x = (sections * i) - (sections); x < filesToBeDone; x++) {
                    sectionedList.add(allFiles.get(x));
                }
            } else {
                for (int x = (sections * i) - (sections); x < (sections * i); x++) {
                    sectionedList.add(allFiles.get(x));

                }
            }
            //Start Multithreading

            //sectionedList gives the thread their Assigned Part of Files and allFiles are all the Files

            ThreadedCompare threadedCompare = new ThreadedCompare(sectionedList, allFiles);
            threadedCompare.start();

        }
        //this updates if necessary the Progress bar and checks for Finished threads
        while (completedThreads < availableThreads) {
            TimeUnit.SECONDS.sleep(1);

            if (showProgress && doTheColorThingy) {
                System.out.print(ConsoleColors.BLUE_BOLD + "Progress: " + ConsoleColors.GREEN_BOLD + progress + " / " + filesToBeDone + ConsoleColors.RESET + "\r");
            } else if (showProgress) {
                System.out.print("Progress: " + progress + " / " + filesToBeDone + "\r");
            }

        }

        //now everything is finished and the Filemap (hashmap with all Dups) can be printed out in a nice view
        //System.out.println(fileMap);

        for (String md5: fileMap.keySet()) {

            if (doTheColorThingy) {
                System.out.println(ConsoleColors.BLUE_BOLD + md5 + ConsoleColors.CYAN_BOLD + " --> " + ConsoleColors.GREEN_BOLD + fileMap.get(md5) + ConsoleColors.RESET);

            } else {
                System.out.println(md5 +" --> " + fileMap.get(md5));
            }

        }

        if (deleteDups) {
            List<Path> allTheFilesWillBeDeleted = new ArrayList<>();

            long bytes = 0;

            for (String md5: fileMap.keySet()) {
                Main.fileMap.get(md5).remove(0);
                for (Path file: Main.fileMap.get(md5)) {
                    if (file != null) {
                        bytes += file.toFile().length();
                    }

                }
                allTheFilesWillBeDeleted.addAll(Main.fileMap.get(md5));

            }



            ask(doTheColorThingy, bytes, allTheFilesWillBeDeleted);

        }

    }
    //print files and ask user
    public static void ask(boolean color, long bytes, List<Path> deleteThem) {
        if (color) {
            System.out.println(ConsoleColors.RED_BOLD + (bytes / 8000000) + " unnecessary MB in " + deleteThem.size() + " Files found, do you want to Delete them? Y / N" + ConsoleColors.RESET);
        } else {
            System.out.println((bytes / 8000000) + " unnecessary MB in " + deleteThem.size() + " Files found, do you want to Delete them? Y / N");
        }
        Scanner input = new Scanner(System.in);
        String answer = input.next();
        if (answer.toLowerCase().contains("y")) {
            delete(deleteThem);
            input.close();

        } else if (answer.toLowerCase().contains("n")) {
            return;
        } else {
            ask(color, bytes, deleteThem);
        }
        input.close();

    }

    public static void delete(List<Path> deleteThem) {
        for (Path file : deleteThem) {
            file.toFile().delete();
        }
    }

}
