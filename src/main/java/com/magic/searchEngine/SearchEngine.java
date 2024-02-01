package com.magic.searchEngine;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.github.kiulian.downloader.model.search.field.DurationField;
import com.github.kiulian.downloader.model.search.field.FeatureField;

import com.magic.model.SearchTab;
import com.magic.searchTabManager.SearchTabManager;

public class SearchEngine {
    private String searchText;
    private FeatureField featureField;
    private DurationField durationField;
    private YoutubeDownloader downloader;

    //Tabs of previous search result

    public SearchEngine(String searchText, FeatureField featureWith, DurationField duration, YoutubeDownloader downloader){
        this.searchText = searchText;
        this.featureField = featureWith;
        this.durationField = duration;
        this.downloader = downloader;
    }

    public SearchResult search() {
        SearchTab searchTab = SearchTabManager.search(downloader, searchText);

        return searchTab.getResultList();
    }

    public SearchResult nextPage(){
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
