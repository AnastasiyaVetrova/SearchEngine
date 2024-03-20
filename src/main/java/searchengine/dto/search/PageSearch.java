package searchengine.dto.search;

import lombok.Data;

@Data
public class PageSearch {
    private String urlSite;
    private String siteName;
    private String urlPage;
    private String title;
    private String snippet;
    private Float relevance;

}
