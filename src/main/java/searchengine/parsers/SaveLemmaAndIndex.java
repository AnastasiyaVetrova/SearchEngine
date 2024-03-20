package searchengine.parsers;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;


@Data
@AllArgsConstructor
public class SaveLemmaAndIndex {

    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Transactional
    public boolean existsLemma(String lemma, PageEntity pageEntity) {
        return lemmaRepository.existsByLemmaAndSite(lemma, pageEntity.getSite());
    }

    @Transactional
    public LemmaEntity updateLemma(String lemma, PageEntity pageEntity) {
        LemmaEntity lemmaFindDB = lemmaRepository.findByLemmaAndSite(lemma, pageEntity.getSite());
        lemmaFindDB.setFrequency(lemmaFindDB.getFrequency() + 1);
        lemmaRepository.updateLemma(lemmaFindDB.getId(), lemmaFindDB.getFrequency());
        return lemmaFindDB;
    }

    @Transactional
    public LemmaEntity createLemma(String lemma, PageEntity pageEntity) {
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setLemma(lemma);
        lemmaEntity.setSite(pageEntity.getSite());
        lemmaEntity.setFrequency(1);
        lemmaRepository.save(lemmaEntity);
        return lemmaEntity;
    }

    @Transactional
    public void createIndex(LemmaEntity lemmaEntity, PageEntity pageEntity, Float lemmaRank) {
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setLemma(lemmaEntity);
        indexEntity.setPage(pageEntity);
        indexEntity.setLemmaRank(lemmaRank);
        indexRepository.save(indexEntity);
    }
}
