package searchengine.controllers;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.ResponseMessage;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.parsers.SiteMap;
import searchengine.services.StatisticsService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")

public class ApiController {

    private final StatisticsService statisticsService;
    private ExecutorService executorService;
    @Getter
    private static boolean isIndexingEnd = true;

    public ApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ResponseMessage> startIndexing() {
        boolean isEnd = false;
        if (!isIndexingEnd) {
            return new ResponseEntity<>(new ResponseMessage(isEnd, "Индексация уже запущена"), HttpStatus.OK);
        }
        isIndexingEnd = false;
        executorService = Executors.newCachedThreadPool();
        SitesList sitesList = statisticsService.getSites();

        for (Site site : sitesList.getSites()) {
            Runnable task = () -> statisticsService.startIndexing(site);
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            isEnd = executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (
                Exception exception) {
            exception.printStackTrace();
        }
        isIndexingEnd = true;
        return new ResponseEntity<>(new ResponseMessage(isEnd), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ResponseMessage> stopIndexing() {
        boolean isClose = false;
        if (isIndexingEnd) {
            return new ResponseEntity<>(new ResponseMessage(isClose, "Индексация не запущена"), HttpStatus.OK);
        }
        isIndexingEnd=true;
        try {
            isClose = executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (Exception exception) {
            executorService.shutdownNow();
        }
        return new ResponseEntity<>(new ResponseMessage(isClose), HttpStatus.OK);
    }
}
