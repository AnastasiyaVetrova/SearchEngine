package searchengine.dto.search;

import lombok.Data;

@Data
public class PageSearch implements Comparable<PageSearch> {
    private String urlSite;
    private String siteName;
    private String urlPage;
    private String title;
    private String snippet;
    private Float relevance;

    @Override
    public int compareTo(PageSearch o) {
        return o.getRelevance().compareTo(this.getRelevance());//todo должен быть обратный порядок
//                this.getRelevance().compareTo(o.getRelevance());
    }
}
