package com.magic.downloader;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.magic.Config;
import com.magic.display.ProgressBar;
import com.magic.display.colorSwitcher.ConsoleColors;
import com.magic.model.DownloadThread;
import com.magic.model.VideoPair;
import com.magic.modified.YoutubeDownloader_Modified;
import com.magic.utils.VideoPlayer;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.*;

public class Downloader {
    private final YoutubeDownloader_Modified downloader;
    public static final int PROGRESS_BAR_REFRESH_RATE = 200; //200ms

    public Downloader(YoutubeDownloader_Modified downloader){
        this.downloader = downloader;
    }

    //Helper function to get the videoID of a video
    VideoInfo getVideoInfo_sync(String videoID){
        RequestVideoInfo request = new RequestVideoInfo(videoID);
        Response<VideoInfo> response = downloader.getVideoInfo(request);
        VideoInfo video = response.data();

        return video;
    }

    public void downloadVideos(List<String> videoIDList){
        ArrayList<VideoPair> videoPairs = extractIDFromName(videoIDList);

        ArrayList<VideoInfo> videoInfoList = getVideoInfoList(videoPairs);

        if (videoPairs.size() > Config.getMaxNumberOfDownloadThread()){
            ConsoleColors.printError("\nError: Exceed max number of download. Try again in smaller download payload instead.");
            return;
        }

        if (Config.isVerifyVideoIDOnPayloadDownload()){
            //Perform checking
            if (videoInfoList.contains(null))
                return;
        }

        //Donwload async
        int payloadSize = videoInfoList.size();

        ProgressBar.initPayloadProgressList(payloadSize);

        List<DownloadThread> downloadThreads = new ArrayList<>();

        //Create threads to do the async download
        for (int i = 0; i < payloadSize; i++){
            Format bestFormat = videoInfoList.get(i).bestVideoWithAudioFormat();

            downloadThreads.add(i, new DownloadThread(bestFormat, videoPairs.get(i).getVideoName()));
            downloadThreads.get(i).start();
        }


        //Update progressbars call here. Since we only update it at one spot
        //There's no need for a mutex lock
        boolean isAllDone = false;

        while (!isAllDone){
            isAllDone = true;

            for (int i = 0; i < payloadSize; i++){
                ConsoleColors.clearConsoleEscapeSequence_test();
                ProgressBar.printProgressBars(downloadThreads.get(i).getCurrentProgress(), i);

                //If one of the thread isn't done, then we repeat this process
                if (!downloadThreads.get(i).isDone())
                    isAllDone = false;
            }

            try{
                Thread.sleep(PROGRESS_BAR_REFRESH_RATE);
            }catch (InterruptedException e){
                ConsoleColors.printError("Error: Downloading thread has been interrupted. Some files might be corrupted");
            }
        }

        //Get the first video's metadata
        File data = null;

        data = downloadThreads.get(0).getResponse().data();

        //Print completion
        ConsoleColors.printSuccess("\nDownloaded " + ProgressBar.getTotalCompletion() + " videos successfully\n");
        ProgressBar.clearPayloadProgressList();

        VideoPlayer.playVideo(data);
    }

    //Helper function for the payload download
    private ArrayList<VideoInfo> getVideoInfoList(ArrayList<VideoPair> list){
        ArrayList<VideoInfo> returnList = new ArrayList<>();

        for (VideoPair item : list){
            VideoInfo videoInfo = getVideoInfo_sync(item.getVideoId());
            returnList.add(videoInfo);
        }

        return returnList;
    }

    public List<String> getVideoFormatInString(String videoID){
        List<String> returnStrList = new ArrayList<>();

        VideoInfo videoInfo = getVideoInfo_sync(videoID);

        if (videoInfo == null){
            //Video ID is not exist
            ConsoleColors.printError("\nVideoID is not exist\n");
            return null;
        }

        List<VideoFormat> videoFormatList = videoInfo.videoFormats();

        for (VideoFormat item : videoFormatList){
            returnStrList.add(item.videoQuality().name());
        }

        return returnStrList;
    }
    public List<String> getAudioFormatInString(String videoID){
        List<String> returnStrList = new ArrayList<>();

        VideoInfo videoInfo = getVideoInfo_sync(videoID);

        if (videoInfo == null){
            //Video ID is not exist
            ConsoleColors.printError("\nVideoID is not exist\n");
            return null;
        }

        List<AudioFormat> videoFormatList = getVideoInfo_sync(videoID).audioFormats();

        for (AudioFormat item : videoFormatList){
            returnStrList.add(item.audioQuality().name());
        }

        return returnStrList;
    }

    private String generateRandomVideoName(){
        return UUID.randomUUID().toString();
    }

    private String renameToken(String newName){
        String modifiedString = newName.substring(1).replace('-', '_');
        modifiedString = StringUtils.capitalise(modifiedString);
        return modifiedString;
    }

    private ArrayList<VideoPair> extractIDFromName(List<String> list){
        ArrayList<VideoPair> idToNameMap = new ArrayList<>();

        //In case there's only 1 video
        if (list.size() == 1){
            idToNameMap.add(new VideoPair(list.get(0), generateRandomVideoName()));
            return idToNameMap;
        }

        int videoIdListLen = list.size();
        int curIdIdx = 0;

        while (curIdIdx < videoIdListLen){
            curIdIdx = nextVideoIdIdx(list, curIdIdx);

            if (curIdIdx == -1)
                break;

            String token = list.get(curIdIdx);
            String prevToken = curIdIdx - 1 > - 1
                                ? list.get(curIdIdx - 1)
                                : "";

            if (prevToken.contains("@")){
                //If the previous is a name
                idToNameMap.add(new VideoPair(token, prevToken));
            }else{
                idToNameMap.add(new VideoPair(token, generateRandomVideoName()));
            }

            curIdIdx++;
        }

        return idToNameMap;
    }

    private int nextVideoIdIdx(List<String> list, int curIdx){
        int i = curIdx;

        while (list.get(i).contains("@")){
            i++;
        }

        return i;
    }


    private boolean containName(List<String> list, String name){
        for (String item : list){
            if (item.equalsIgnoreCase(name))
                return true;
        }

        return false;
    }
}
