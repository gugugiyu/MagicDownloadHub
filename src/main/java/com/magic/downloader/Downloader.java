package com.magic.downloader;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.search.field.DurationField;
import com.github.kiulian.downloader.model.search.field.FeatureField;

import java.util.List;
import java.util.UUID;

public class Downloader {
    private DownloadHandler downloadHandler;

    public Downloader(YoutubeDownloader downloader){
        this.downloadHandler = new DownloadHandler(downloader);
    }

    public void downloadVideo(String videoID){
        downloadHandler.downloadByVideoID_async(videoID, generateRandomVideoName(), true);
    }

    public void downloadVideos(List<String> videoIDList){

    }

    private String generateRandomVideoName(){
        return UUID.randomUUID().toString();
    }
}
