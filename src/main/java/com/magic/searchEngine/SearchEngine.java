package com.magic.searchEngine;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestSearchContinuation;
import com.github.kiulian.downloader.downloader.request.RequestSearchResult;
import com.github.kiulian.downloader.model.search.SearchResult;

import com.github.kiulian.downloader.model.search.field.DurationField;
import com.github.kiulian.downloader.model.search.field.FeatureField;
import com.github.kiulian.downloader.model.search.field.TypeField;
import com.magic.model.SearchTab;
import com.magic.searchTabManager.SearchTabManager;

import java.util.Date;

public class SearchEngine {
    private String searchText;
    private FeatureField featureField;
    private DurationField durationField;
    private YoutubeDownloader downloader;


    //Current viewedTab
    private int currentTab = 0;

    //Tabs of previous search result
    private SearchTabManager searchTabManager = new SearchTabManager();


    public SearchEngine(String searchText, FeatureField featureWith, DurationField duration, YoutubeDownloader downloader){
        this.searchText = searchText;
        this.featureField = featureWith;
        this.durationField = duration;
        this.downloader = downloader;
    }

    public SearchResult search() {
        SearchTab searchTab = searchTabManager.search(downloader, searchText);
        this.currentTab++;

        return searchTab != null ? searchTab.getResultList() : null;
    }

    public SearchResult nextPage(){
        // retrieve next result (20 items max per continuation)
        SearchTab nextTab = searchTabManager.nextPage(downloader, searchText);
        this.currentTab++;

        return nextTab != null ? nextTab.getResultList() : null;
    }

    public int getCurrentTabSize(){
        return this.currentTab;
    }

    public int getTotalSizeOfATab(int id){
        return searchTabManager.findTab(id).size();
    }

    public int getTotalTab(){
        return searchTabManager.size();
    }

}
