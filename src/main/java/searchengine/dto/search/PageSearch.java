package searchengine.dto.search;

import lombok.Data;

@Data
public class PageSearch implements Comparable<PageSearch> {
    private String urlSite;
    private String siteName;
    private String urlPage;
    private String title;
    private String snippet;
    private Double relevance;

    @Override
    public int compareTo(PageSearch o) {
        return this.getRelevance().compareTo(o.getRelevance());
    }
}
