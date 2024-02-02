package com.magic.model;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.magic.Config;
import com.magic.display.DisplayBeautifier;
import com.magic.display.ProgressBar;
import com.magic.display.TableDisplay;
import com.magic.display.colorSwitcher.ConsoleColors;

import java.io.File;

public class DownloadThread extends Thread{
    private Format format;
    private YoutubeDownloader downloader = new YoutubeDownloader();
    private final String downloadDirectory = Config.getDownloadPath();
    private int id;

    private boolean isError = false;

    public  DownloadThread(Format format, int id){
        this.format = format;
        this.id = id;
    }

    @Override
    public void run() {
        File downloadDirectoryFile = new File(downloadDirectory);
        RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                .saveTo(downloadDirectoryFile)
                .callback(new YoutubeProgressCallback<File>() {
                    @Override
                    public void onDownloading(int progress) {
                        ProgressBar.printProgressBars(progress, id);
                    }

                    @Override
                    public void onFinished(File videoInfo) {
                        ProgressBar.addTotalCompletion();
                        ProgressBar.printProgressBars(TableDisplay.TABLE_WIDTH, id);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        isError = true;
                    }
                });

        Response<File> response = downloader.downloadVideoFile(request);
        super.run();
    }

    public boolean isError(){
        return isError;
    }
}
