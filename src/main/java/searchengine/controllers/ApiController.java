package searchengine.controllers;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.ResponseMessage;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;

import java.util.List;

@RestController
@RequestMapping("/api")

public class ApiController {

    private final StatisticsService statisticsService;
    public ApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        SitesList sites = statisticsService.getSites();
//        for (Site site : sites.getSites()) {
//            new Thread(startIndexing(site))
//        }
//        boolean isStart = true;
        boolean isEnd = statisticsService.startIndexing();
        ;

        if (!isEnd) {

            return new ResponseEntity(new ResponseMessage(isEnd,
                    "Индексация уже запущена"), HttpStatus.PROCESSING);
//        } else if (!isEnd) {
//            return new ResponseEntity(new ResponseMessage(isEnd,
//                    "Индексация уже запущена"), HttpStatus.PROCESSING);
        }

//        statisticsService.startIndexing();
//        isEnd = statisticsService.startIndexing();
        return new ResponseEntity<>(new ResponseMessage(statisticsService.startIndexing()), HttpStatus.OK);
    }
}
