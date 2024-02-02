package com.magic.downloader;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.magic.Config;
import com.magic.display.ProgressBar;
import com.magic.display.colorSwitcher.ConsoleColors;
import com.magic.model.DownloadThread;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Downloader {
    private SingleFileDownloaderHandler singleFileDownloaderHandler;

    public Downloader(YoutubeDownloader downloader){
        this.singleFileDownloaderHandler = new SingleFileDownloaderHandler(downloader);
    }

    public void downloadVideo(String videoID){
        singleFileDownloaderHandler.downloadByVideoID_sync(videoID, generateRandomVideoName(), true);
    }

    public void downloadVideos(List<String> videoIDList){
        List<VideoInfo> videoInfoList = getVideoInfoList(videoIDList);

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

            downloadThreads.add(i, new DownloadThread(bestFormat, i));
            downloadThreads.get(i).start();
        }

        int totalSuccessfulDownloadThread = 0;

        //Join the threads
        for (int i = 0; i < payloadSize; i++){
            try{
                downloadThreads.get(i).join(Config.getTimeoutTimeInMillisecond());

                if (!downloadThreads.get(i).isError())
                        totalSuccessfulDownloadThread++;

            } catch (InterruptedException e){
                ConsoleColors.printError("This download thread has been interrupted");
            }
        }

        ConsoleColors.printSuccess("\nDownloaded " + totalSuccessfulDownloadThread + " videos successfully\n");
        ProgressBar.clearPayloadProgressList();
    }

    //Helper function for the payload download
    private List<VideoInfo> getVideoInfoList(List<String> videoIDList){
        List<VideoInfo> returnList = new ArrayList<>();

        for (String item : videoIDList){
            VideoInfo videoInfo = singleFileDownloaderHandler.getVideoInfo_sync(item);
            returnList.add(videoInfo);
        }

        return returnList;
    }

    public List<String> getVideoFormatInString(String videoID){
        List<String> returnStrList = new ArrayList<>();

        VideoInfo videoInfo = singleFileDownloaderHandler.getVideoInfo_sync(videoID);

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

        VideoInfo videoInfo = singleFileDownloaderHandler.getVideoInfo_sync(videoID);

        if (videoInfo == null){
            //Video ID is not exist
            ConsoleColors.printError("\nVideoID is not exist\n");
            return null;
        }

        List<AudioFormat> videoFormatList = singleFileDownloaderHandler.getVideoInfo_sync(videoID).audioFormats();

        for (AudioFormat item : videoFormatList){
            returnStrList.add(item.audioQuality().name());
        }

        return returnStrList;
    }

    private String generateRandomVideoName(){
        return UUID.randomUUID().toString();
    }
}
