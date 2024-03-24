package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import searchengine.Application;
import searchengine.dto.response.MessageResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;


@RestController
@RequestMapping("/api")
public class ApiController {


    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        Application.LOG.info("Получение статистики сайтов.");
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<MessageResponse> startIndexing() {
        Application.LOG.info("Начата индексация сайтов");
        return new ResponseEntity<>(indexingService.startIndexing(), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<MessageResponse> stopIndexing() {
        Application.LOG.info("Принудительная остановка индексации.");
        return new ResponseEntity<>(indexingService.stopIndexing(), HttpStatus.OK);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<MessageResponse> indexPage(@RequestParam String url) {
        Application.LOG.info("Начата индексация страницы");
        return new ResponseEntity<>(indexingService.startIndexPage(url), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<MessageResponse> search(@RequestParam String query, @RequestParam(defaultValue = "0") Integer offset,
                                                  @RequestParam(defaultValue = "10") Integer limit,
                                                  @RequestParam(required = false) String site) {
        Application.LOG.info("Поиск: " + query);
        return new ResponseEntity<>(statisticsService.startSearch(query, site, offset, limit), HttpStatus.OK);
    }
}
