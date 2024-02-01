package com.magic.display;

import com.magic.Config;
import com.magic.display.colorSwitcher.ConsoleColors;

public class LogoPrinter {
    public static void printLogo(){
        System.out.println(ConsoleColors.CYAN);
        System.out.println(" __  __             _        _____                      _                 _   _    _       _   ");
        System.out.println("|  \\/  |           (_)      |  __ \\                    | |               | | | |  | |     | |    ");
        System.out.println("| \\  / | __ _  __ _ _  ___  | |  | | _____      ___ __ | | ___   __ _  __| | | |__| |_   _| |__  ");
        System.out.println("| |\\/| |/ _` |/ _` | |/ __| | |  | |/ _ \\ \\ /\\ / / '_ \\| |/ _ \\ / _` |/ _` | |  __  | | | | '_ \\ ");
        System.out.println("| |  | | (_| | (_| | | (__  | |__| | (_) \\ V  V /| | | | | (_) | (_| | (_| | | |  | | |_| | |_) |");
        System.out.println("|_|  |_|\\__,_|\\__, |_|\\___| |_____/ \\___/ \\_/\\_/ |_| |_|_|\\___/ \\__,_|\\__,_| |_|  |_|\\__,_|_.__/ ");
        System.out.println("               __/ |                                                                             ");
        System.out.println("              |___/                   ");
        System.out.println(ConsoleColors.RESET);

        ConsoleColors.printInfo("v1.0 SNAPSHOT\n");

        TableDisplay.displayMap("SETTING", "VALUE", Config.getKeys(), Config.getValues());

        ConsoleColors.printInfo(
                "\n" +
                "NOTE: This application is an extension based on github repo of: " +
                "\n" +
                "https://github.com/sealedtx/java-youtube-downloader" +
                "\n"
                );
    }
}
