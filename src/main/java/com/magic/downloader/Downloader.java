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
import com.magic.utils.VideoPlayer;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Downloader {
    private final YoutubeDownloader downloader;
    public static final int PROGRESS_BAR_REFRESH_RATE = 200; //200ms

    public Downloader(YoutubeDownloader downloader){
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
        //Error case: first element should always be an id
        if (videoIDList.get(0).contains("@")){
            ConsoleColors.printError("\nError: First element can't be a name\n");
            return;
        }

        //Separate the names and the id
        List<String> videoName = new ArrayList<>();

        int videoIdListLen = videoIDList.size();
        int currentNameListIdx = 0;
        boolean isNewVideoID = false;

        for (int i = 0; i < videoIdListLen; i++){
            String token = videoIDList.get(i);
            if (token.startsWith("@")){
                if (!isNewVideoID)
                    continue;

                videoName.add(currentNameListIdx++, renameToken(token));
                isNewVideoID = false;
            }else{
                //This is a videoID
                isNewVideoID = true;
            }
        }

        //Remove all the name from this list
        videoIDList = videoIDList.stream()
                .filter(videoID -> !videoID.contains("@"))
                .toList();

        List<VideoInfo> videoInfoList = getVideoInfoList(videoIDList);

        if (videoIDList.size() > Config.getMaxNumberOfDownloadThread()){
            ConsoleColors.printError("\nError: Exceed max number of download. Try again in smaller download payload instead.");
            return;
        }

        if (Config.isVerifyVideoIDOnPayloadDownload()){
            //Perform checking
            if (videoInfoList.contains(null))
                return;
        }

        //Donwload async
        int payloadSize = videoIDList.size();

        ProgressBar.initPayloadProgressList(payloadSize);

        List<DownloadThread> downloadThreads = new ArrayList<>();

        //Create threads to do the async download
        for (int i = 0; i < payloadSize; i++){
            Format bestFormat = videoInfoList.get(i).bestVideoWithAudioFormat();

            downloadThreads.add(i, new DownloadThread(bestFormat, videoName.get(i)));
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
    private List<VideoInfo> getVideoInfoList(List<String> videoIDList){
        List<VideoInfo> returnList = new ArrayList<>();

        for (String item : videoIDList){
            VideoInfo videoInfo = getVideoInfo_sync(item);
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
}
