package com.magic.videoManager;

import com.magic.Config;
import com.magic.display.OptionDisplay;
import com.magic.display.colorSwitcher.ConsoleColors;
import com.magic.utils.VideoPlayer;
import org.codehaus.plexus.util.StringUtils;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class VideoManager {
    private static Scanner scanner = new Scanner(System.in);
    private static final String downloadPath = Config.getDownloadPath();
    public static final int MAX_DEPTH = 6;

    public static void findVideoAndPlay(String searchedStr){
        Path videoPath = Paths.get(downloadPath);
        List<Path> searchedList = new ArrayList<>();

        //Use simple file visitor
        try{
            SimpleFileVisitor<Path> pathVisitor = new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    //Display video file only
                    if (Files.probeContentType(file).contains("video")
                        && file.getFileName().toString().contains(searchedStr))
                        searchedList.add(file);
                    
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    ConsoleColors.printError("Error: Failed to access " + file.getFileName().toString());
                    return FileVisitResult.CONTINUE;
                }
            };

            EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            Files.walkFileTree(videoPath, options, 6, pathVisitor);

            System.out.println();
        } catch (Exception e){
            ConsoleColors.printError("\nError: An error has occured while displaying video files. Please try again\n");
        }

        if (searchedList.size() <= 0){
            // No video found
            ConsoleColors.printInfo("\nFound no video with that term\n");
            return;
        }

        ConsoleColors.printSuccess("Found " + searchedList.size() + " video"  + (searchedList.size() > 1 ? "s" : ""));

        //Handle user input for playing video
        int id = handleUserInput(scanner, searchedList);

        if (id == -1 || id > searchedList.size()){
            ConsoleColors.printError("\nError: Invalid video\n");
            return;
        }

        VideoPlayer.playVideo(new File(searchedList.get(id).toString()));
    }

    private static int handleUserInput(Scanner scanner, List<Path> searchedList){
        int returnIdx = -1;

        OptionDisplay.printOption_path(searchedList);
        ConsoleColors.printInstruction("\nEnter video id>> ", false);

        returnIdx = scanner.nextInt();

        return returnIdx;
    }
}
