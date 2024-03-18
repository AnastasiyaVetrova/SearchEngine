package searchengine.dto.search;

import lombok.Data;
import searchengine.model.IndexEntity;

import java.util.List;

@Data
public class IndexSearch {
    private List<IndexEntity> indexes;

    public IndexSearch(List<IndexEntity> indexes) {
        this.indexes = indexes;
    }
}
