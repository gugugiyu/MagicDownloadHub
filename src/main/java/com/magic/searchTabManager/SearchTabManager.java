package com.magic.searchTabManager;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestSearchContinuation;
import com.github.kiulian.downloader.downloader.request.RequestSearchResult;
import com.github.kiulian.downloader.model.search.SearchResult;
import com.github.kiulian.downloader.model.search.field.TypeField;
import com.magic.model.SearchTab;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchTabManager {
    private static final List<SearchTab> searchTabList = new ArrayList<>();
    private static int currentIdx = 0;

    public int size(){
        return searchTabList.size();
    }

    public void addNewTab(SearchTab newSearchTab){
        searchTabList.add(newSearchTab);
    }

    public void moveToTask(int index){
        currentIdx = index;
    }

    public void removeTab(int index){
        searchTabList.remove(index);
    }

    public void clearHistory(){
        searchTabList.clear();
    }

    public int getCurrentIdx(){
        return currentIdx;
    }

    public SearchTab findTab(int id){
        return searchTabList.get(id);
    }

    public SearchTab nextPage(YoutubeDownloader downloader, String queryStr){
        SearchResult latestSearchResult = searchTabList.get(currentIdx).getResultList();

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

    public SearchTab search(YoutubeDownloader downloader, String queryStr){
        RequestSearchResult request = new RequestSearchResult(queryStr)
                // filters
                .type(TypeField.VIDEO)                 // Videos only

                // Can use autocorrection to enhance searching experience
                .forceExactQuery(false);                 // avoid auto correction

        SearchResult result = downloader.search(request).data();
        SearchTab newTab = new SearchTab(result, queryStr, new Date());

        searchTabList.add(newTab);

        return newTab;
    }

    public static List<SearchTab> getTabList(){
        return searchTabList;
    }


}
