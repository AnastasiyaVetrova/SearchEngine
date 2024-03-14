package searchengine.lemmas;

import lombok.Data;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import searchengine.model.PageEntity;

import java.util.HashMap;
import java.util.List;

@Data
public class FindLemma {
    LuceneMorphology luceneMorph;
    private static final String[] partsSpeech = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    public FindLemma() {
        try {
            this.luceneMorph = new RussianLuceneMorphology();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Float> receivedLemmas(String[] words) {

        HashMap<String, Float> lemmas = new HashMap<>();

            for (String word : words) {
                if (word.isEmpty()){
                    continue;
                }
                if (hasPartsSpeech(word)) {
                    continue;
                }
                List<String> baseForms = luceneMorph.getNormalForms(word);
                if (baseForms.isEmpty()) {
                    continue;
                }
                String baseWord = baseForms.get(0);
                if (lemmas.containsKey(baseWord)) {
                    lemmas.put(baseWord, lemmas.get(baseWord) + 1f);
                } else {
                    lemmas.put(baseWord, 1f);
                }
            }
        System.out.println(lemmas.size());
        return lemmas;
    }

    private boolean hasPartsSpeech(String word) {
        List<String> wordInfo = luceneMorph.getMorphInfo(word);
        for (String part : partsSpeech) {
            for (String w : wordInfo) {
                if (w.contains(part)) {
                    return true;
                }
            }
        }
        return false;
    }
}


