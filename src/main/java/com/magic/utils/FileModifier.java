package com.magic.utils;

import com.magic.Config;
import com.magic.display.colorSwitcher.ConsoleColors;

import java.io.File;

public class FileModifier {
    public static void createDirectoryIfNotExist(){
        File downloadDir = new File(Config.getDownloadPath());

        if (downloadDir.exists())
            return;

        boolean isCreated = downloadDir.mkdir();

        if (!isCreated) {
            ConsoleColors.printError("\nError: Can't create new download directory at this location. Please try again.\n");
        }else {
            ConsoleColors.printSuccess("\nCreate new download directory successfully\n");
        }
    }
}
