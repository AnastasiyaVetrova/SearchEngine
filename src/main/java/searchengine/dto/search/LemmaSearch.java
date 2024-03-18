package searchengine.dto.search;

import lombok.Data;
import searchengine.model.LemmaEntity;

import java.util.ArrayList;
import java.util.List;

@Data
public class LemmaSearch {
    private List<LemmaEntity> lemmas;
    private int countPage;

    public LemmaSearch() {
        this.lemmas = new ArrayList<>();
    }

    public void addLemmaEntity(LemmaEntity lemma) {
        lemmas.add(lemma);
    }
}
