package searchengine.lemmas;

import lombok.Data;
import searchengine.dto.search.LemmaSearch;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;

import java.util.*;

@Data
public class FindSearchLemma {

    public List<IndexEntity> generateSearchIndex(LemmaSearch lemmaSearch) {
        List<LemmaEntity> lemmas = lemmaSearch.getLemmas();
        lemmas.sort(Comparator.comparingInt(LemmaEntity::getFrequency));
        List<IndexEntity> searchIndex = lemmas.get(0).getIndexEntity();
        for (LemmaEntity l : lemmas) {
            searchIndex = searchIndex(l, searchIndex);
        }
        return searchIndex;
    }

    public List<IndexEntity> searchIndex (LemmaEntity lemma, List<IndexEntity> searchIndex) {
        List<IndexEntity> indexList = new ArrayList<>();
        for (IndexEntity i : lemma.getIndexEntity()) {
            for (IndexEntity j : searchIndex) {
                if (j.getPage().equals(i.getPage())) {
                    indexList.add(i);
                }
            }
        }
        return indexList;
    }
}
