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
import com.magic.display.ProgressBar;
import com.magic.display.colorSwitcher.ConsoleColors;
import com.magic.model.DownloadThread;

import java.awt.*;
import java.io.File;
import java.io.IOException;

//This is a helper class for downloading videos
class SingleFileDownloaderHandler {
    private final String downloadDirectory = Config.getDownloadPath();
    private final YoutubeDownloader downloader;

    SingleFileDownloaderHandler(YoutubeDownloader downloader){
        this.downloader = downloader;
    }

    VideoInfo getVideoInfo_sync(String videoID){
        RequestVideoInfo request = new RequestVideoInfo(videoID);
        Response<VideoInfo> response = downloader.getVideoInfo(request);
        VideoInfo video = response.data();

        return video;
    }

     void downloadByVideoID_sync(String videoID, String newName, boolean replaceIfExisted){
        //Also this function won't download live video either

        //Download path
        File outputDir = new File(downloadDirectory);

        //Get the format
        VideoInfo videoInfo = getVideoInfo_sync(videoID);

        if (videoInfo == null){
            //Video ID is not exist
            ConsoleColors.printError("\nVideoID is not exist\n");
            return;
        }

        if (videoInfo.details().isDownloadable()){
            //Run a download thread
            DownloadThread singleDownloadThread = new DownloadThread(videoInfo.bestVideoWithAudioFormat());
            singleDownloadThread.start();

            Response<File> response = null;

            //Block this thread until it joins
            //Remember, single download is synchronous
            boolean isDone = false;

            while (!isDone){
                //Clear the screen and reprint
                ConsoleColors.clearConsoleEscapeSequence_test();
                ConsoleColors.printInstruction("Downloading...");
                ProgressBar.printProgressBar(singleDownloadThread.getCurrentProgress());

                isDone = singleDownloadThread.isDone();

                //Sleep this thread
                try{
                Thread.sleep(Downloader.PROGRESS_BAR_REFRESH_RATE);
                }catch (InterruptedException e){
                    ConsoleColors.printError("Error: Downloading thread has been interrupted. Some files might be corrupted");
                }
            }

            response = singleDownloadThread.getResponse();

            if (response == null){
                ConsoleColors.printError("\nError: Can't download this video");
                return;
            }

            File data = response.data();

            //Try open the new file
            if (Config.isOpenVideoAfterDownload()){
                try {
                    if (Desktop.isDesktopSupported())
                        Desktop.getDesktop().open(data);
                } catch (IOException e) {
                    ConsoleColors.printError("\nCan't open the video, desktop is not supported");
                }
            }


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

                //Audio stat
                System.out.println("Audio MIME TYPE: " + audioStat.mimeType());
                System.out.println("Audio bitrate: " + ((double) audioStat.audioSampleRate() / 1000) + "kb/s");
                System.out.println("Overall quality: " + audioStat.audioQuality().name());

                //Misc stat
                System.out.println("Size: " + ((double) new File(videoInfo.toString()).length() / (1024 * 1024)) + " mb");

                System.out.println();
            }else{
                ConsoleColors.printError("\nThere was an error while downloading this file\n");
            }
        }else {
            ConsoleColors.printError("\nThis video is not downloadable. It's either a livestream or removed from youtube\n");
        }
    }

}
