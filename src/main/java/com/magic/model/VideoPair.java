package com.magic.model;

public class VideoPair{
    private String videoName;
    private String videoId;

    public VideoPair(String videoId, String videoName) {
        this.videoName = videoName;
        this.videoId = videoId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
