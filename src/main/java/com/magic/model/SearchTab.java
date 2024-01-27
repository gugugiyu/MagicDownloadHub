package com.magic.model;

import com.github.kiulian.downloader.model.search.SearchResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchTab {
    private SearchResult resultList;
    private String queryStr;
    private Date searchedAt;

    public SearchTab(SearchResult searchResult, String queryStr, Date searchedAt){
        this.resultList = searchResult;
        this.queryStr = queryStr;
        this.searchedAt = searchedAt;
    }

    public SearchResult getResultList() {
        return resultList;
    }

    public void setResultList(SearchResult resultList) {
        this.resultList = resultList;
    }

    public String getQueryStr() {
        return queryStr;
    }

    public void setQueryStr(String queryStr) {
        this.queryStr = queryStr;
    }

    public Date getSearchedAt() {
        return searchedAt;
    }

    public void setSearchedAt(Date searchedAt) {
        this.searchedAt = searchedAt;
    }

    public int size(){
        return 20;
        // Temporary hardcoding this, as the result returned is capped at 20
    }
}
