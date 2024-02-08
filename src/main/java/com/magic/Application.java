package com.magic;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.magic.display.LogoPrinter;
import com.magic.display.OptionDisplay;
import com.magic.display.TableDisplay;
import com.magic.display.colorSwitcher.ConsoleColors;
import com.magic.display.DisplayBeautifier;
import com.magic.downloader.Downloader;
import com.magic.searchEngine.SearchEngine;
import com.magic.searchTabManager.SearchTabManager;
import com.magic.videoManager.VideoManager;

import java.io.IOException;
import java.util.*;
import java.util.List;


public class Application{
    final static YoutubeDownloader downloader = new YoutubeDownloader();
    final static Scanner scanner = new Scanner(System.in);
    static SearchEngine searchEngine = null;
    final static Downloader videoDownloader = new Downloader(downloader);

    public static void main(String[] args){
        LogoPrinter.printLogo();

        boolean mainFlag = true;

        while (mainFlag){
            OptionDisplay.printOptionMainMenu();

            try {
                int mode = scanner.nextInt();

                switch (mode){
                    case 0:
                        //Kill all downloading thread

                        mainFlag = false;
                        break;

                    case 1:
                        try {
                            Runtime.getRuntime().exec("explorer.exe " + Config.getDownloadPath());
                        }catch (IOException e){
                            ConsoleColors.printError("Error: can't open saved directory. Either the dir isn't exist or this platform doesn't support this function");
                        }
                        break;

                    //SEARCHING WITH QUERY
                    case 2:
                        ConsoleColors.printInstruction("\nEnter query string >> ", true);

                        //Pre consume the input buffer
                        scanner.nextLine();
                        String queryStr = scanner.nextLine();
                        searchEngine = new SearchEngine(null, null, downloader);

                        SearchResult searchResult = searchEngine.search(queryStr);

                        DisplayBeautifier.printBeautifiedVideoList(searchResult, searchEngine.getCurrentTabSize(), searchEngine.getTotalTab());
                        break;

                    case 3:
                        if (searchEngine == null || SearchTabManager.getTabList().isEmpty()){
                            //If there's no query yet
                            ConsoleColors.printError("\nError: There's no query yet, please use [1] Search instead\n");
                            continue;
                        }

                        //Get query string of the current page
                        int currentIdx = SearchTabManager.getCurrentIdx() - 1;
                        queryStr = SearchTabManager.getTabList().get(currentIdx).getQueryStr();

                        SearchResult nextPage = searchEngine.nextPage(queryStr);
                        DisplayBeautifier.printBeautifiedVideoList(nextPage, searchEngine.getCurrentTabSize(), searchEngine.getTotalTab());
                        break;

                    case 4:
                        DisplayBeautifier.printBeautifiedSearchTab(scanner);
                        break;

                    case 5:
                        if (searchEngine == null || SearchTabManager.getTabList().isEmpty()){
                            //If there's no query yet
                            ConsoleColors.printError("\nError: There's no query yet, please use [1] Search instead\n");
                            continue;
                        }

                        int currentTabIndex = SearchTabManager.getCurrentIdx() - 1;
                        SearchResult currentSearchResult = SearchTabManager.getTabList().get(currentTabIndex).getResultList();

                        DisplayBeautifier.printBeautifiedVideoList(currentSearchResult, searchEngine.getCurrentTabSize(), searchEngine.getTotalTab());
                        break;

                    case 6:
                        ConsoleColors.printInstruction("\nEnter video ID >> ", true);
                        scanner.nextLine();
                        String videoId = scanner.nextLine();

                        if (videoId == null || videoId.equalsIgnoreCase("")){
                            ConsoleColors.printError("\nError: Invalid videoID\n");
                            continue;
                        }

                        //Create a table with 2 rows, videoFormat on the left and audioFormat on the right
                        List<String> videoFormatList = videoDownloader.getVideoFormatInString(videoId);
                        List<String> audioFormatList = videoDownloader.getAudioFormatInString(videoId);

                        TableDisplay.displayMap("Video Formats", "Audio Formats", videoFormatList, audioFormatList);

                        System.out.println();
                        break;

                    case 7:
                        //Download multiple videoID using space (" ") as a delimiter
                        ConsoleColors.printWarning("\nNote: DEFAULT video download path is set to the \"downloaded_videos\" directory");
                        ConsoleColors.printInfo("\nTo specify a name for a video, add this syntax AFTER the renamed video:");
                        ConsoleColors.printInfo("'@' + 'name' (Ex. @babyshark)");
                        ConsoleColors.printWarning("\nIn case there're multiple names for a video, the first name will be taken\n");
                        ConsoleColors.printInstruction("\nEnter video IDs (separated by a space)>> ", true);
                        scanner.nextLine();
                        videoId = scanner.nextLine();

                        if (videoId == null || videoId.equalsIgnoreCase("")){
                            ConsoleColors.printError("\nError: Invalid videoIDs\n");
                            continue;
                        }

                        //Break them into a list of string
                        List<String> videoIDs =  Arrays.asList(videoId.trim().split(" "));

                        videoDownloader.downloadVideos(videoIDs);
                        break;

                    case 8:
                        //Find and play video using SimpleFileVisitor API
                        ConsoleColors.printInstruction("\nType a word or a phrase of the video's name>> ", true);
                        scanner.nextLine();
                        String videoName = scanner.nextLine();

                        if (videoName == null || videoName.equalsIgnoreCase("")){
                            ConsoleColors.printError("\nError: Invalid video name\n");
                            continue;
                        }

                        VideoManager.findVideoAndPlay(videoName);
                        break;

                    default:
                        ConsoleColors.printError("\nInvalid operation. Please select again.\n");
                        break;
                }
            } catch (InputMismatchException e){
                ConsoleColors.printError("\nWrong data type. Make sure to type integers only.\n");

                //Flush the buffer
                scanner.next();
            }
        }
    }
}