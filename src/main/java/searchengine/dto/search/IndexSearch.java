package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.IndexEntity;

import java.util.List;

@Data
@AllArgsConstructor
public class IndexSearch {
    private List<IndexEntity> indexes;
}
