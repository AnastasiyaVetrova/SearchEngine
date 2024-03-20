package searchengine.lemmas;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.dto.search.LemmaSearch;
import searchengine.model.LemmaEntity;
import searchengine.repositories.PageRepository;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
public class GroupLemmaBySite {
    private final PageRepository pageRepository;

    public HashMap<Integer, LemmaSearch> getLemmasOneSite(List<List<LemmaEntity>> lemmas) {
        HashMap<Integer, LemmaSearch> lemmasOneSite = new HashMap<>();
        for (
                List<LemmaEntity> lemma : lemmas) {
            for (LemmaEntity l : lemma) {
                if (!lemmasOneSite.containsKey(l.getSite().getId())) {
                    int countPage = pageRepository.countBySite(l.getSite());
                    if (l.getFrequency() / countPage * 100 > 80) {
                        continue;
                    }
                    LemmaSearch lemmaSearch = new LemmaSearch();
                    lemmaSearch.addLemmaEntity(l);
                    lemmaSearch.setCountPage(countPage);
                    lemmasOneSite.put(l.getSite().getId(), lemmaSearch);
                } else {
                    if (l.getFrequency() / lemmasOneSite.get(l.getSite().getId()).getCountPage() * 100 > 80) {
                        continue;
                    }
                    lemmasOneSite.get(l.getSite().getId()).addLemmaEntity(l);
                }
            }
        }
        return lemmasOneSite;
    }
}