package com.magic.utils;

import com.magic.Config;
import com.magic.display.colorSwitcher.ConsoleColors;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class VideoPlayer {
    public static void playVideo(File data){
        //If there's only 1 file, open them
        if (data != null && Config.isOpenVideoAfterDownload()){
            try {
                if (Desktop.isDesktopSupported())
                    Desktop.getDesktop().open(data);
            } catch (IOException e) {
                ConsoleColors.printError("\nCan't open the video, desktop is not supported");
            }
        }
    }
}
