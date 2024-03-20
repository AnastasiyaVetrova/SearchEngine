package searchengine.dto.search;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ResultSearch {
    private ArrayList<PageSearch> data;

    public ResultSearch() {
        this.data = new ArrayList<>();
    }
    public void addResult (PageSearch pageSearch){
        data.add(pageSearch);
    }
}
