package searchengine.lemmas;

import searchengine.dto.search.IndexSearch;

import java.util.List;

public class Relevance {
    private List<IndexSearch> indexSearchList;
    private int countSearchPage;

    public Relevance(List<IndexSearch> indexSearchList, int countSearchPage) {
        this.indexSearchList = indexSearchList;
        this.countSearchPage = countSearchPage;
    }
    public void absoluteRelevance(){

    }
    public void relativeRelevance(){

    }
}
