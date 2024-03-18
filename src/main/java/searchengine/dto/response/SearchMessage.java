package searchengine.dto.response;

import searchengine.dto.search.SiteSearch;

import java.util.TreeSet;

public class SearchMessage {
    private boolean result;
    private String error;
    private int count;
    private TreeSet<SiteSearch> data;

}
