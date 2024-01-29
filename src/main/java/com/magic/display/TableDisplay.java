package com.magic.display;

import com.magic.display.colorSwitcher.ConsoleColors;

import java.util.List;

public class TableDisplay {
    private static final int TABLE_WIDTH = 75;

    public static <T> void displayTable(List<T> displayList){
        printHalfTopBox();

        for (T item : displayList)
            System.out.printf("│ %-71s │\n", item);

        printHalfBottomBox();
    }

    public static void displayMap(String firstColTitle, String secondColTitle, List<String> keys, List<String> values){
        //This method works based on the assumption that both the length of keys and values are the same

        //Minus 3 as we exclude the opening and closing tag | from both side and one in the middle
        int spacePerColumn = (TABLE_WIDTH - 3) / 2;

        printHalfTopBox();
        //Print the title
        System.out.printf("│" + ConsoleColors.WHITE_BOLD_BRIGHT + "%-" + spacePerColumn + "s│" + "%-" + spacePerColumn + "s" + ConsoleColors.RESET + "│\n", firstColTitle, secondColTitle);
        printDivider();

        //Print the pair key-value
        int index = 0;

        //Used for preventing out of bound
        int printThreshold = Math.min(keys.size(), values.size());

        while (index < printThreshold){
            System.out.printf("│" + "%-" + spacePerColumn + "s│" + "%-" + spacePerColumn + "s" + "│\n", keys.get(index), values.get(index));
            index++;
        }

        printHalfBottomBox();
    }

    public static void printHalfTopBox(){
        System.out.println("┌─────────────────────────────────────────────────────────────────────────┐");
    }

    public static void printDivider(){
        System.out.println("├─────────────────────────────────────────────────────────────────────────┤");
    }

    public static void printHalfBottomBox(){
        System.out.println("└─────────────────────────────────────────────────────────────────────────┘");
    }
}
