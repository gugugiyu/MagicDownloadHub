package com.magic.searchEngine;

import com.github.kiulian.downloader.model.search.SearchResult;
import com.github.kiulian.downloader.model.search.SearchResultVideoDetails;
import com.magic.colorSwitcher.ConsoleColors;
import com.magic.model.SearchTab;
import com.magic.searchTabManager.SearchTabManager;

import java.util.List;

public class DisplayBeautifier {
    public static void printBeautifiedVideoList(SearchResult searchResult, int id, int total){
        if (searchResult == null)
            return;

        List<SearchResultVideoDetails> videos = searchResult.videos();

        System.out.println("\n[Tab " + id + " of " + total + "]");

        for (SearchResultVideoDetails video : videos){
            final String trimmedTitle = video.title();

            String description = video.description();
            if (description != null && description.length() > 49)
                description = description.substring(0, 49);

            final String youtuber = video.author();
            final String duration = SecondToTimeString(video.lengthSeconds());
            final String videoID = video.videoId();

            printHalfTopBox();
            ConsoleColors.printInfo("│ Title: \"" + trimmedTitle + "\"");
            System.out.println("│ Des: \"" + description + "\"");
            System.out.println("│ By: \"" + youtuber + "\"");
            System.out.println("│ Duration: \"" + duration + "\"");
            System.out.println("│ VideoID: \"" + videoID + "\"");
            printHalfBottomBox();
        }

    }

    public static void printBeautifiedSearchTab(){
        List<SearchTab> searchTabList = SearchTabManager.getTabList();

        if (searchTabList.isEmpty()){
            ConsoleColors.printError("\nError: There's no query yet, please use [1] Search instead\n");
        }else{
            ConsoleColors.printSuccess("Found " + searchTabList.size() + " tabs");
            
            printHalfTopBox();
            
            int index = 0;
            for (SearchTab tab : searchTabList){
                System.out.println("│" + "[" + (index++) + "] " + "Query string: \"" + tab.getQueryStr() + "\"");
            }

            printHalfBottomBox();
        }
    }

    private static void printHalfTopBox(){
        System.out.println("┌─────────────────────────────────────────────────────────────────────────┐");
    }

    private static void printHalfBottomBox(){
        System.out.println("└─────────────────────────────────────────────────────────────────────────┘");
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


}
