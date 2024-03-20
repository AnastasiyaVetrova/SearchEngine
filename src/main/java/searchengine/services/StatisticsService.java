package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.response.SearchMessage;
import searchengine.dto.statistics.StatisticsResponse;
@Service
public interface StatisticsService {

    StatisticsResponse getStatistics();
    boolean startIndexing(Site site);
    SitesList getSites();
    boolean startIndexPage(String url);
    SearchMessage startSearch(String query, String url, Integer offset, Integer limit);
}
