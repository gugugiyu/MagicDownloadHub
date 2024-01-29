package com.magic.display;

import com.magic.display.colorSwitcher.ConsoleColors;

import java.util.ArrayList;
import java.util.List;

public class OptionDisplay{
        public static void printOption(List<String> options){
            int index = 0;

            for (String option : options){
                //Highlight the cta
                System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT);
                System.out.printf("[%d]", index);
                System.out.print(ConsoleColors.RESET);
                System.out.printf(" %s", option);

                index++;
            }

            System.out.print("\n\n>> ");
        }

        public static void printOptionMainMenu() {
            List<String> menuList = new ArrayList<>();

            //Add all operation here
            menuList.add("QUIT");
            menuList.add("Search with keyword");
            menuList.add("Extend this search");
            menuList.add("Browse search tabs");
            menuList.add("View current tab's search result");

            ConsoleColors.printInstruction("# Select operation:");
            printOption(menuList);
        }
}
