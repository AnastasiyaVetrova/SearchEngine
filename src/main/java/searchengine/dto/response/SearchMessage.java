package searchengine.dto.response;

import lombok.Data;
import searchengine.dto.search.PageSearch;
import searchengine.dto.search.ResultSearch;

import java.util.ArrayList;

@Data
public class SearchMessage implements MessageResponse {
    private boolean result;
    private int count;
    private ArrayList<PageSearch> data;

    public SearchMessage(ResultSearch resultSearch, Integer offset, Integer limit) {
        this.count = resultSearch.getData().size();
        this.data = new ArrayList<>();
        int maxPage = Math.min(count, limit);
        int minPage = count < offset ? 0 : offset;
        for (int i = minPage; i < maxPage; i++) {
            data.add(resultSearch.getData().get(i));
        }
    }

    public void addData(PageSearch pageSearch) {
        data.add(pageSearch);
    }
}
