package com.magic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static String downloadPath = initDownloadPath();
    private static boolean openVideoAfterDownload = true;
    private static long timeoutTimeInMillisecond = 1000 * 60 * 60 * 12; //12 hours
    private static boolean verifyVideoIDOnPayloadDownload = false; //takes lot of time to check on a big payload, disable by default
    private static boolean filterDuplicateVideoId = true; //Filter out duplicate videoId. Disable this incase you want to download a video multiple times

    private static String initDownloadPath(){
        String returnStr = System.getProperty("user.dir");

        //remove the "target" from the path as we want to use the folder from the src
        if (returnStr.contains("target")){
            returnStr = returnStr.substring(0, returnStr.length() - "\\target".length());
        }

        return returnStr + "\\downloaded_videos"; //Change your folder name here
    }

    public static String getDownloadPath() {
        return downloadPath;
    }

    public static boolean isOpenVideoAfterDownload() {
        return openVideoAfterDownload;
    }

    public static boolean isVerifyVideoIDOnPayloadDownload() {
        return verifyVideoIDOnPayloadDownload;
    }

    public static long getTimeoutTimeInMillisecond() {
        return timeoutTimeInMillisecond;
    }

    public static boolean isFilterDuplicateVideoId() {
        return filterDuplicateVideoId;
    }

    public static List<String> getKeys(){
        List<String> returnList = new ArrayList<>();

        //Add all the config key here
        returnList.add("downloadPath");
        returnList.add("openVideoAfterDownload");
        returnList.add("verifyVideoIDOnPayloadDownload");
        returnList.add("timeoutTimeInMillisecond");
        returnList.add("filterDuplicateValue");

        return returnList;
    }


    public static List<String> getValues(){
        List<String> returnList = new ArrayList<>();

        //Add all the config key here
        returnList.add(downloadPath);
        returnList.add(String.valueOf(openVideoAfterDownload));
        returnList.add(String.valueOf(verifyVideoIDOnPayloadDownload));
        returnList.add(String.valueOf(timeoutTimeInMillisecond) + "ms");
        returnList.add(String.valueOf(filterDuplicateVideoId));

        return returnList;
    }
}
