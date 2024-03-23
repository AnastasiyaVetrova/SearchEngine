package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.dto.response.MessageResponse;
import searchengine.dto.statistics.StatisticsResponse;

@Service
public interface StatisticsService {

    StatisticsResponse getStatistics();

    MessageResponse startSearch(String query, String url, Integer offset, Integer limit);
}
