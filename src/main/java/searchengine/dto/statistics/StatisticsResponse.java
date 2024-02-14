package searchengine.dto.statistics;

import lombok.Data;
//класс получения списка сайтов со всеми деталями
@Data
public class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;
}
