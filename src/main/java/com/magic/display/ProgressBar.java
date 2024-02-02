package com.magic.display;

import com.magic.display.colorSwitcher.ConsoleColors;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ProgressBar {
    private static Semaphore payloadDownloadLock = new Semaphore(1);
    private static List<Integer> payloadDownloadProgressList = new ArrayList<>(); //This is the shared resources
    private static int totalCompletion = 0; //This is the shared resources

    public static void printProgressBar(int progress){
        int progressSize = (int) (( (double) progress / 100) * TableDisplay.TABLE_WIDTH); //Display with a "=" per unit
        int remainingSize = TableDisplay.TABLE_WIDTH - progressSize; //Display with a " " per unit

        if (progressSize > 0){
            progressSize--; //Minus 1 to pad out for the ">" at the end
        }

        String progressStr = StringUtils.repeat("=", progressSize);
        String remainStr = StringUtils.repeat(" ", remainingSize);

        System.out.println("[" + progressStr + ">" + remainStr + "]");
    }

    public static void printProgressBars(int newProgress, int index){
        //Lock the critical section with mutex (with semaphore class)
        try{
            payloadDownloadLock.acquire();
            //Update the new progress for that thread's process
            ConsoleColors.clearConsole();
            payloadDownloadProgressList.set(index, newProgress);

            //Reprint the progress bar
            int payloadSize = payloadDownloadProgressList.size();

            ConsoleColors.printInstruction("Downloading... " + "[" + totalCompletion + "/" + payloadSize + "]");

            for (int i = 0; i < payloadSize; i++)
                printProgressBar(payloadDownloadProgressList.get(i));

        } catch (InterruptedException e) {
            // exception handling code
        } finally {
            payloadDownloadLock.release();
        }
    }

    public static synchronized void addTotalCompletion(){
        //Simple inline mutex lock
        totalCompletion++;
    }

    public static void initPayloadProgressList(int payloadSize){
        for (int i = 0; i < payloadSize; i++)
            payloadDownloadProgressList.add(0); //Zero as starting point to download

        //Note: We'll refer to the current download progress of each thread by the id assigned to it to be the
        //index of this list
    }

    public static void clearPayloadProgressList(){
        payloadDownloadProgressList.clear();
        totalCompletion = 0;
    }
}
