package searchengine.dto.response;

import lombok.Data;
import searchengine.dto.search.PageSearch;

import java.util.TreeSet;

@Data
public class SearchMessage implements MessageResponse {
    private boolean result;
    private int count;
    private TreeSet<PageSearch> data;

    public SearchMessage() {
        this.data = new TreeSet<>();
    }

    public void addData(PageSearch pageSearch) {
        data.add(pageSearch);
    }

}
