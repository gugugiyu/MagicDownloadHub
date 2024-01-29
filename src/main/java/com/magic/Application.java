package com.magic;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.magic.display.LogoPrinter;
import com.magic.display.OptionDisplay;
import com.magic.display.colorSwitcher.ConsoleColors;
import com.magic.display.DisplayBeautifier;
import com.magic.searchEngine.SearchEngine;
import com.magic.searchTabManager.SearchTabManager;

import java.util.InputMismatchException;
import java.util.Scanner;


public class Application{
    final static YoutubeDownloader downloader = new YoutubeDownloader();
    final static Scanner scanner = new Scanner(System.in);
    static SearchEngine searchEngine = null;

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
                        if (searchEngine == null || SearchTabManager.getTabList().isEmpty()){
                            //If there's no query yet
                            ConsoleColors.printError("\nError: There's no query yet, please use [1] Search instead\n");
                            continue;
                        }

                        SearchResult nextPage = searchEngine.nextPage();
                        DisplayBeautifier.printBeautifiedVideoList(nextPage, searchEngine.getCurrentTabSize(), searchEngine.getTotalTab());
                        break;

                    case 3:
                        DisplayBeautifier.printBeautifiedSearchTab(scanner);
                        break;

                    case 4:
                        if (searchEngine == null || SearchTabManager.getTabList().isEmpty()){
                            //If there's no query yet
                            ConsoleColors.printError("\nError: There's no query yet, please use [1] Search instead\n");
                            continue;
                        }

                        int currentTabIndex = SearchTabManager.getCurrentIdx() - 1;
                        SearchResult currentSearchResult = SearchTabManager.getTabList().get(currentTabIndex).getResultList();

                        DisplayBeautifier.printBeautifiedVideoList(currentSearchResult, searchEngine.getCurrentTabSize(), searchEngine.getTotalTab());
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