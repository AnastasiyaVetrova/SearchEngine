package searchengine.parsers;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.Jsoup;
import searchengine.lemmas.MorphAnalysisLemma;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.regex.BaseRegex;
import searchengine.services.IndexingStartService;

import java.util.HashMap;
import java.util.concurrent.CancellationException;

@Data
@AllArgsConstructor
public class FindLemma {
    private final SaveLemmaAndIndex saveLemmaAndIndex;

    public void findLemmaOnPage(PageEntity pageEntity) {

        MorphAnalysisLemma morphAnalysisLemma = new MorphAnalysisLemma();
        HashMap<String, Float> wordPage = morphAnalysisLemma.receivedLemmas(listWordsFromPage(pageEntity));
        if (wordPage.isEmpty()) {
            return;
        }
        for (String lemma : wordPage.keySet()) {
            if (IndexingStartService.isIndexingEnd()) {
                throw new CancellationException();
            }
            LemmaEntity lemmaEntity = findLemma(lemma, pageEntity);
            saveLemmaAndIndex.createIndex(lemmaEntity, pageEntity, wordPage.get(lemma));
        }
    }

    private String[] listWordsFromPage(PageEntity pageEntity) {
        return Jsoup.parse(pageEntity.getContent()).text().toLowerCase().split(BaseRegex.getREGEX_WORD());
    }

    private LemmaEntity findLemma(String lemma, PageEntity pageEntity) {
        LemmaEntity lemmaEntity;
        try {
            if (saveLemmaAndIndex.existsLemma(lemma, pageEntity)) {
                lemmaEntity = saveLemmaAndIndex.updateLemma(lemma, pageEntity);
            } else {
                lemmaEntity = saveLemmaAndIndex.createLemma(lemma, pageEntity);
            }
        } catch (Throwable exception) {
            System.out.println("Ошибка");
            return findLemma(lemma, pageEntity);
        }
        return lemmaEntity;
    }
}
