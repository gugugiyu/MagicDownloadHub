package com.magic.searchTabManager;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestSearchContinuation;
import com.github.kiulian.downloader.downloader.request.RequestSearchResult;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.github.kiulian.downloader.model.search.field.TypeField;
import com.magic.model.SearchTab;
import com.magic.modified.YoutubeDownloader_Modified;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchTabManager {
    private static final List<SearchTab> searchTabList = new ArrayList<>();
    private static int currentIdx = 0;

    public int size(){
        return searchTabList.size();
    }

    public static void addNewTab(SearchTab newSearchTab){
        searchTabList.add(newSearchTab);
    }

    public static void moveToTask(int index){
        //We're using 0-based decimal counting system
        currentIdx = index + 1;
    }

    public static void removeTab(int index){
        searchTabList.remove(index);
    }

    public static void clearHistory(){
        searchTabList.clear();
    }

    public static int getCurrentIdx(){
        return currentIdx;
    }

    public static SearchTab findTab(int id){
        return searchTabList.get(id);
    }

    public static SearchTab nextPage(YoutubeDownloader_Modified downloader, String queryStr){
        // Offset 1 as currentIdx use 0-based
        SearchResult latestSearchResult = searchTabList.get(currentIdx - 1).getResultList();

        if (latestSearchResult.hasContinuation()) {
            RequestSearchContinuation nextRequest = new RequestSearchContinuation(latestSearchResult);

            SearchResult continuation = downloader.searchContinuation(nextRequest).data();
            SearchTab tabContinuation = new SearchTab(continuation, queryStr, new Date());

            searchTabList.add(tabContinuation);
            currentIdx++;

            return tabContinuation;
        }

        return null;
    }

    public static SearchTab search(YoutubeDownloader_Modified downloader, String queryStr){
        RequestSearchResult request = new RequestSearchResult(queryStr)
                // filters
                .type(TypeField.VIDEO)                 // Videos only

                // Can use autocorrection to enhance searching experience
                .forceExactQuery(false);                 // avoid auto correction

        SearchResult result = downloader.search(request).data();
        SearchTab newTab = new SearchTab(result, queryStr, new Date());

        searchTabList.add(newTab);
        currentIdx++;

        return newTab;
    }

    public static List<SearchTab> getTabList(){
        return searchTabList;
    }


}
