package com.magic.downloader;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.magic.Config;
import com.magic.display.DisplayBeautifier;
import com.magic.display.colorSwitcher.ConsoleColors;

import java.io.File;
import java.util.List;

//This is a helper class for downloading videos
class DownloadHandler {
    private final String downloadDirectory = Config.getDownloadPath();
    private final YoutubeDownloader downloader;

    public DownloadHandler(YoutubeDownloader downloader){
        this.downloader = downloader;
    }

    private VideoInfo getVideoInfo_sync(String videoID){
        RequestVideoInfo request = new RequestVideoInfo(videoID);
        Response<VideoInfo> response = downloader.getVideoInfo(request);
        VideoInfo video = response.data();

        return video;
    }

     void downloadByVideoID_async(String videoID, String newName, boolean replaceIfExisted){
        //Also this function won't download live video either

        //Download path
        File outputDir = new File(downloadDirectory);

        //Get the format
        VideoInfo videoInfo = getVideoInfo_sync(videoID);

        if (videoInfo.details().isDownloadable()){
            List<VideoFormat> videoFormats = videoInfo.videoFormats();
            videoFormats.forEach(it -> {
                System.out.println(it.videoQuality() + " : " + it.url());
            });

            Format format = videoInfo.bestVideoWithAudioFormat();

            RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                    .saveTo(outputDir)
                    .renameTo(newName)
                    .callback(new YoutubeProgressCallback<File>() {
                        @Override
                        public void onDownloading(int progress) {
                            DisplayBeautifier.printProgressBar(progress);
                        }

                        @Override
                        public void onFinished(File videoInfo) {
                            ConsoleColors.printSuccess("\nFinish downloading file " +
                                                        videoInfo
                            );
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            ConsoleColors.printError(throwable.getMessage());
                        }
                    })
                    .overwriteIfExists(replaceIfExisted); //Default is true
            Response<File> response = downloader.downloadVideoFile(request);
            File data = response.data();

            if (response.ok()){
                ConsoleColors.printInfo("File download successfully at " +
                        "\"" + data.getAbsolutePath() +
                        "\"\n"
                );

                ConsoleColors.printInstruction("Video download specs");

                VideoFormat videoStat = videoInfo.bestVideoFormat();
                AudioFormat audioStat = videoInfo.bestAudioFormat();

                //Video stat
                System.out.println("Resolution: " + videoStat.height() + "x" + videoStat.width());
                System.out.println("FPS: " + videoStat.fps());
                System.out.println("MIME type: " + videoStat.mimeType());
                System.out.println("Overall quality: " + videoStat.videoQuality().name());
                System.out.println("Overall quality: " + videoStat.videoQuality().name());

                //Audio stat
                System.out.println("Audio MIME TYPE: " + audioStat.mimeType());
                System.out.println("Audio bitrate: " + ((double) audioStat.audioSampleRate() / 1000) + "kb/s");
                System.out.println("Overall quality: " + audioStat.audioQuality().name());

                System.out.println();
            }else{
                ConsoleColors.printError("\nThere was an error while downloading this file\n");
            }
        }else {
            ConsoleColors.printError("\nThis video is not downloadable. It's either a livestream or removed from youtube\n");
        }
    }

}
