package searchengine.dto.statistics;

import lombok.Data;

import java.util.List;
//общий список сайтов, в деталях
@Data
public class StatisticsData {
    private TotalStatistics total;
    private List<DetailedStatisticsItem> detailed;
}
