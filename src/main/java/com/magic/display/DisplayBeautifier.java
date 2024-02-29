package com.magic.display;

import com.github.kiulian.downloader.model.search.SearchResult;
import com.github.kiulian.downloader.model.search.SearchResultVideoDetails;

import com.magic.display.colorSwitcher.ConsoleColors;
import com.magic.model.SearchTab;
import com.magic.searchTabManager.SearchTabManager;

import java.util.*;
public class DisplayBeautifier {
    public static void printBeautifiedVideoList(SearchResult searchResult, int id, int total){
        if (searchResult == null)
            return;

        List<SearchResultVideoDetails> videos = searchResult.videos();
        int listSize = videos.size(); //By default, this value should be 20

        //Clear the screen
        ConsoleColors.clearConsoleEscapeSequence_test();

        System.out.println("\n[Tab " + id + " of " + total + "]");

        TableDisplay.printHalfTopBox();
        for (int i = 0; i < listSize; i++){
            SearchResultVideoDetails video = videos.get(i);

            String title = video.title();
            title = title.substring(0, Math.min(title.length(), 64));


            String description = video.description();
            if (description != null)
                description = description.substring(0, Math.min(description.length(), 66));

            final String youtuber = video.author();
            final String duration = SecondToTimeString(video.lengthSeconds());
            final String videoID = video.videoId();

            System.out.printf("│ Title: " + "%-65s" + "│\n", title );
            System.out.printf("│ Des: " + "%-67s" + "│\n", description );
            System.out.printf("│ By: " + "%-68s" + "│\n", youtuber );
            System.out.printf("│ Duration: " + "%-62s" + "│\n", duration );
            System.out.printf("│ VideoID: " + "%-63s"  + "│\n", videoID );

            if (i != listSize - 1)
                TableDisplay.printDivider();
        }
        TableDisplay.printHalfBottomBox();
    }

    public static void printBeautifiedSearchTab(Scanner scanner){
        List<SearchTab> searchTabList = SearchTabManager.getTabList();

        if (searchTabList.isEmpty()){
            ConsoleColors.printError("\nError: There's no query yet, please use [1] Search instead\n");
        }else{
            //Clear the screen
            ConsoleColors.clearConsoleEscapeSequence_test();

            ConsoleColors.printSuccess("\nFound " + searchTabList.size() + " tabs");
            System.out.println("You may select a tab to view based on selecting their index");
            System.out.println("Type of any non-digit character to exit selection");


            //Extracted the query string from that tabs
            List<String> extractedTabListName = new ArrayList<>();
            int index = 0, currentTabIndex = SearchTabManager.getCurrentIdx() - 1;

            for (SearchTab tab : searchTabList){
                extractedTabListName.add("Query str: " + "\"" + tab.getQueryStr() + "\"" +
                                        (
                                            index == currentTabIndex
                                            ? ConsoleColors.WHITE_BOLD_BRIGHT + " (current)" + ConsoleColors.RESET
                                            : ""
                                        )
                );

                index++;
            }

            OptionDisplay.printOption(extractedTabListName);

            handleUserInputSwitchSearchTab(scanner, extractedTabListName.size());
        }
    }

    private static String SecondToTimeString(int second){
        //Return the time in the following format "HH:MM:SS"
        String formattedTime = "";

        int hour = second / 3600;
        int minute = (int) ((((double) second / 3600) - hour ) * 60);
        int remainSecond = second % 60;

        if (hour != 0)
            formattedTime += hour + ":";

        if (minute != 0)
            formattedTime += ( (minute < 10 && hour != 0) ? "0" : "") + minute + ":"; //Pad an extra zero

        formattedTime += remainSecond;

        return formattedTime;
    }

    private static void handleUserInputSwitchSearchTab(Scanner scanner, int length){
        //Handle user input for tab selection
        try {
            int mode = scanner.nextInt();

            if (mode >= 0 && mode < length){
                SearchTabManager.moveToTask(mode);
                ConsoleColors.printSuccess("\nMoved to tab " + mode + " successfully\n");
            }else{
                ConsoleColors.printInfo("\nYou didn't select the new tab. Using current tab.\n");
            }

        }catch (InputMismatchException e){
            ConsoleColors.printError("\n Invalid mode selection. Please try again.");
        }
    }
}
