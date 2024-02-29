package com.magic.utils;

import com.magic.display.colorSwitcher.ConsoleColors;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class CodePageChanger {

    public static void toggleCHCP_65001(){
        try{
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "chcp", "65001").inheritIO();
            Process p = pb.start();
            p.waitFor();

            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

            ConsoleColors.clearConsole();
        }catch (IOException | InterruptedException e){
            ConsoleColors.printError("Error: Can't enable codepage of current terminal session. Please visit https://github.com/gugugiyu/MagicDownloadHub for chcp guide.");
            System.exit(-1);
        }
    }
}
