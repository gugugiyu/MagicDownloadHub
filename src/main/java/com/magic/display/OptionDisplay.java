package com.magic.display;

import com.magic.display.colorSwitcher.ConsoleColors;

import java.util.ArrayList;
import java.util.List;

public class OptionDisplay{
        public static void printOption(List<String> options){
            // When a string starts with "---", print it as a line divider instead

            int index = 0;

            for (String option : options){
                //Highlight the cta
                if (option.startsWith("---") && option.length() > 3){
                    //Print the divider
                    System.out.print("\n\n" +
                                        ConsoleColors.CYAN_BRIGHT +
                                        option.substring(3) +
                                        ConsoleColors.RESET
                    );
                }else{
                    System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT);
                    System.out.printf("[%d]", index);
                    System.out.print(ConsoleColors.RESET);
                    System.out.printf(" %s", option);
                    index++;
                }
            }

            System.out.print("\n\n>> ");
        }

        public static void printOptionMainMenu() {
            List<String> menuList = new ArrayList<>();

            //Add all operation here
            menuList.add("---MISC");
            menuList.add("QUIT");
            menuList.add("Show current saved directory");

            menuList.add("---SEARCH");
            menuList.add("Search with keyword");
            menuList.add("Extend this search");
            menuList.add("Browse search tabs");
            menuList.add("View current tab's search result");

            menuList.add("---DOWNLOAD");
            menuList.add("Get all formats using videoID");
            menuList.add("Download using videoID (synchronously)");
            menuList.add("Create payload to download (asynchronously)");

            ConsoleColors.printInstruction("# Select operation:", false);
            printOption(menuList);
        }
}
