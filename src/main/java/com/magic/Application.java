package com.magic;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.magic.colorSwitcher.ConsoleColors;
import com.magic.searchEngine.DisplayBeautifier;
import com.magic.searchEngine.SearchEngine;

import java.util.Scanner;

class Application{
    final static YoutubeDownloader downloader = new YoutubeDownloader();
    final static Scanner scanner = new Scanner(System.in);
    static SearchEngine searchEngine = null;

    public static void printOption(){
        ConsoleColors.printInstruction("# Select operation:");
        System.out.println("# 0. QUIT");
        System.out.println("# 1. Search");
        System.out.println("# 2. Go to next search page");
        System.out.println("# 3. Browse Searched Tab");

        System.out.print("\n>> ");
    }
    public static void main(String[] args){
        ConsoleColors.printInfo( "NOTE: This application is an extension based on github repo of: " +
                "\n" +
                "https://github.com/sealedtx/java-youtube-downloader");

        boolean mainFlag = true;

        while (mainFlag){
            printOption();


            int mode = scanner.nextInt();

            switch (mode){
                case 0:
                    //Kill all downloading thread

                    mainFlag = false;
                    break;

                //SEARCHING WITH QUERY
                case 1:
                    ConsoleColors.printInstruction("\nEnter query string >> ", true);

                    //Preconsume the input buffer
                    scanner.nextLine();
                    String queryStr = scanner.nextLine();
                    searchEngine = new SearchEngine(queryStr, null, null, downloader);

                    SearchResult searchResult = searchEngine.search();

                    DisplayBeautifier.printBeautifiedVideoList(searchResult, searchEngine.getCurrentTabSize(), searchEngine.getTotalTab());
                    break;

                case 2:
                    if (searchEngine == null){
                        //If there's no query yet
                        ConsoleColors.printError("\nError: There's no query yet, please use [1] Search instead\n");
                    }else{
                        SearchResult nextPage = searchEngine.nextPage();

                        DisplayBeautifier.printBeautifiedVideoList(nextPage, searchEngine.getCurrentTabSize(), searchEngine.getTotalTab());
                    }

                    break;

                case 3:
                    DisplayBeautifier.printBeautifiedSearchTab();
                    break;
            }
        }
    }
}