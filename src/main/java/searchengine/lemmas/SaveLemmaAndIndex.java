package searchengine.lemmas;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import searchengine.controllers.ApiController;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;

import java.util.HashMap;
import java.util.concurrent.CancellationException;


@Data
@AllArgsConstructor
public class SaveLemmaAndIndex {

    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public void saveLemma(PageEntity pageEntity) {

            FindLemma findLemma = new FindLemma(pageEntity);
            HashMap<String, Float> wordPage = findLemma.receivedLemmas();

            for (String lemma : wordPage.keySet()) {
                if (ApiController.isIndexingEnd()) {
                    throw new CancellationException();
                }
                LemmaEntity lemmaEntity;
                if (lemmaRepository.existsByLemmaAndSite(lemma, pageEntity.getSite())) {
                    lemmaEntity = updateLemma(lemma, pageEntity);
                } else {
                    lemmaEntity = createLemma(lemma, pageEntity);
                }
                createIndex(lemmaEntity, pageEntity, wordPage.get(lemma));
            }
    }

//    @Transactional
    private LemmaEntity updateLemma(String lemma, PageEntity pageEntity) {
        LemmaEntity lemmaFindDB = lemmaRepository.findByLemmaAndSite(lemma, pageEntity.getSite());
        lemmaFindDB.setFrequency(lemmaFindDB.getFrequency() + 1);
        lemmaRepository.updateLemma(lemmaFindDB.getId(), lemmaFindDB.getFrequency());
        return lemmaFindDB;
    }
//    @Transactional
    private LemmaEntity createLemma(String lemma, PageEntity pageEntity) {
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setLemma(lemma);
        lemmaEntity.setSite(pageEntity.getSite());
        lemmaEntity.setFrequency(1);
        lemmaRepository.save(lemmaEntity);
        return lemmaEntity;
    }

//    @Transactional
    private void createIndex(LemmaEntity lemmaEntity, PageEntity pageEntity, Float lemmaRank) {
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setLemma(lemmaEntity);
        indexEntity.setPage(pageEntity);
        indexEntity.setLemmaRank(lemmaRank);
        indexRepository.save(indexEntity);
    }
}
