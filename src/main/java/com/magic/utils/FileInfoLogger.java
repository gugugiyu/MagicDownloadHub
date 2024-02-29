package com.magic.utils;

import com.magic.display.colorSwitcher.ConsoleColors;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

public class FileInfoLogger {
    private File newFile;
    private static final String[] fileSizeNotation = {"B", "KB", "MB", "GB", "TB"};

    public FileInfoLogger(File newFile) {
        this.newFile = newFile;
    }

    public void printFileSize(){
        long fileSize = FileUtils.sizeOfDirectory(newFile);
        short fileSizeNotationIdx = 0;

        //Converting
        while (fileSize > 1024){
            fileSize /= 1024;
            fileSizeNotationIdx++;
        }

        ConsoleColors.printInstruction("\nTotal directory size: " +
                ConsoleColors.CYAN_BOLD
                + fileSize
                + " "
                + fileSizeNotation[fileSizeNotationIdx] + "\n"
                + ConsoleColors.RESET
        );
    }
}
