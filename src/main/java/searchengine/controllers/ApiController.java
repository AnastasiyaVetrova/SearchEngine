package searchengine.controllers;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.response.Message;
import searchengine.dto.statistics.StatisticsResponse;
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
    public ResponseEntity<Message> startIndexing() {
        boolean isEnd = false;
        if (!isIndexingEnd) {
            return new ResponseEntity<>(new Message(isEnd, "Индексация уже запущена"), HttpStatus.OK);
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
        return new ResponseEntity<>(new Message(isEnd), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Message> stopIndexing() {
        boolean isClose = false;
        if (isIndexingEnd) {
            return new ResponseEntity<>(new Message(isClose, "Индексация не запущена"), HttpStatus.OK);
        }
        isIndexingEnd = true;
        try {
            isClose = executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (Exception exception) {
            executorService.shutdownNow();
        }
        return new ResponseEntity<>(new Message(isClose), HttpStatus.OK);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Message> indexPage(@RequestParam String url) {
        isIndexingEnd = false;
        boolean isIndexPage = statisticsService.startIndexPage(url);
        if (!isIndexPage) {
            return new ResponseEntity<>(new Message(isIndexPage,
                    "Данная страница находится за пределами сайтов, указанных в конфигурационном файле"
            ), HttpStatus.OK);
        }
        isIndexingEnd = true;
        return new ResponseEntity<>(new Message(isIndexPage), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Message> search(@RequestParam String query) {
        statisticsService.startSearch(query);

        return new ResponseEntity<>(new Message(true), HttpStatus.OK);
    }
}
