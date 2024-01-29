package com.magic;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private static String downloadPath = "videoData";

    public static String getDownloadPath() {
        return downloadPath;
    }

    public static void setDownloadPath(String downloadPath) {
        Config.downloadPath = downloadPath;
    }

    public static List<String> getKeys(){
        List<String> returnList = new ArrayList<>();

        //Add all the config key here
        returnList.add("downloadPath");

        return returnList;
    }


    public static List<String> getValues(){
        List<String> returnList = new ArrayList<>();

        //Add all the config key here
        returnList.add(downloadPath);

        return returnList;
    }
}
