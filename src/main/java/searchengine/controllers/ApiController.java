package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<MessageResponse> startIndexing() {
        return new ResponseEntity<>(indexingService.startIndexing(), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<MessageResponse> stopIndexing() {
        return new ResponseEntity<>(indexingService.stopIndexing(), HttpStatus.OK);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<MessageResponse> indexPage(@RequestParam String url) {
        return new ResponseEntity<>(indexingService.startIndexPage(url), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<MessageResponse> search(@RequestParam String query, @RequestParam(defaultValue = "0") Integer offset,
                                                  @RequestParam(defaultValue = "10") Integer limit,
                                                  @RequestParam(required = false) String site) {

        return new ResponseEntity<>(statisticsService.startSearch(query, site, offset, limit), HttpStatus.OK);
    }
}
