package com.magic.searchEngine;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.github.kiulian.downloader.model.search.field.DurationField;
import com.github.kiulian.downloader.model.search.field.FeatureField;

import com.magic.model.SearchTab;
import com.magic.modified.YoutubeDownloader_Modified;
import com.magic.searchTabManager.SearchTabManager;

public class SearchEngine {
    private FeatureField featureField;
    private DurationField durationField;
    private YoutubeDownloader_Modified downloader;

    //Tabs of previous search result

    public SearchEngine(FeatureField featureWith, DurationField duration, YoutubeDownloader_Modified downloader){
        this.featureField = featureWith;
        this.durationField = duration;
        this.downloader = downloader;
    }

    public SearchResult search(String searchText) {
        SearchTab searchTab = SearchTabManager.search(downloader, searchText);

        return searchTab.getResultList();
    }

    public SearchResult nextPage(String searchText){
        // retrieve next result (20 items max per continuation)
        SearchTab nextTab = SearchTabManager.nextPage(downloader, searchText);

        return nextTab != null ? nextTab.getResultList() : null;
    }

    public int getCurrentTabSize(){
        return SearchTabManager.getCurrentIdx();
    }

    public int getTotalSizeOfATab(int id){
        return SearchTabManager.findTab(id).size();
    }

    public int getTotalTab(){
        return SearchTabManager.getTabList().size();
    }

}
