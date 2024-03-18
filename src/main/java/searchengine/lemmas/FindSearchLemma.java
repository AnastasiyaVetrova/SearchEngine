package searchengine.lemmas;

import lombok.Data;
import searchengine.dto.search.LemmaSearch;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import java.util.*;

@Data
public class FindSearchLemma {

    public List<IndexEntity> generateSearchPage(LemmaSearch lemmaSearch) {
        List<LemmaEntity> lemmas = lemmaSearch.getLemmas();
        lemmas.sort(new Comparator<LemmaEntity>() {
            @Override
            public int compare(LemmaEntity o1, LemmaEntity o2) {
                return o1.getFrequency() - o2.getFrequency();
            }
        });
        List<IndexEntity> searchIndex = new ArrayList<>();
        for (LemmaEntity l : lemmas) {
            searchIndex = searchPage(l, searchIndex);
        }
        return searchIndex;
    }

    public List<IndexEntity> searchPage(LemmaEntity lemma, List<IndexEntity> searchIndex) {
        List<IndexEntity> indexList = new ArrayList<>();
        for (IndexEntity i : lemma.getIndexEntity()) {
            if (searchIndex.contains(i)) {
                indexList.add(i);
            }
        }
        return indexList;
    }
}
