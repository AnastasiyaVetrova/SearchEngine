package searchengine.dto.statistics;

import lombok.Data;
//детально каждый сайт
@Data
public class DetailedStatisticsItem {
    private String url;
    private String name;
    private String status;
    private long statusTime;
    private String error;
    private int pages;
    private int lemmas;
}
