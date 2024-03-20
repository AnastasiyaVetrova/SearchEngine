package searchengine.parsers;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.Jsoup;
import searchengine.controllers.ApiController;
import searchengine.lemmas.MorphAnalysisLemma;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.regex.BaseRegex;

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
            if (ApiController.isIndexingEnd()) {
                throw new CancellationException();
            }
            LemmaEntity lemmaEntity;
            if (saveLemmaAndIndex.existsLemma(lemma, pageEntity)) {
                lemmaEntity = saveLemmaAndIndex.updateLemma(lemma, pageEntity);
            } else {
                lemmaEntity = saveLemmaAndIndex.createLemma(lemma, pageEntity);
            }
            saveLemmaAndIndex.createIndex(lemmaEntity, pageEntity, wordPage.get(lemma));
        }
    }

    private String[] listWordsFromPage(PageEntity pageEntity) {
        return Jsoup.parse(pageEntity.getContent()).text().toLowerCase().split(BaseRegex.getREGEX_WORD());
    }
}
