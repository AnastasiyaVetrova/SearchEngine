package searchengine.dto.statistics;

import lombok.Data;
//общая статистика количества сайтов
@Data
public class TotalStatistics {
    private int sites;
    private int pages;
    private int lemmas;
    private boolean indexing;
}
