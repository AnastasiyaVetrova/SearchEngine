package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.dto.response.MessageResponse;
@Service
public interface IndexingService {
    MessageResponse startIndexing ();
    MessageResponse stopIndexing();
    MessageResponse startIndexPage(String url);
}
