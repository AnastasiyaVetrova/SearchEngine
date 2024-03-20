package searchengine.lemmas;

import org.jsoup.Jsoup;
import searchengine.dto.search.IndexSearch;
import searchengine.dto.search.PageSearch;
import searchengine.model.IndexEntity;

import java.util.TreeMap;

public class Relevance {
    private final String[] wordQuery;

    public Relevance(String[] wordQuery) {
        this.wordQuery = wordQuery;
    }

    public TreeMap<Integer, PageSearch> absoluteRelevance(IndexSearch indexSearch) {
        TreeMap<Integer, PageSearch> pageSearchTreeMap = new TreeMap<>();
        for (IndexEntity i : indexSearch.getIndexes()) {
            int pageId = i.getPage().getId();
            if (pageSearchTreeMap.containsKey(pageId)) {
                pageSearchTreeMap.get(pageId).setRelevance(
                        pageSearchTreeMap.get(pageId).getRelevance() + i.getLemmaRank());
            } else {
                PageSearch pageSearch = new PageSearch();
                pageSearch.setUrlPage(i.getPage().getPath());
                pageSearch.setUrlSite(i.getPage().getSite().getUrl());
                pageSearch.setSiteName(i.getPage().getSite().getName());
                pageSearch.setRelevance(i.getLemmaRank());
                pageSearch.setTitle(Jsoup.parse(i.getPage().getContent()).title());
                pageSearch.setSnippet(findSnippet(Jsoup.parse(i.getPage().getContent()).text()));
                pageSearchTreeMap.put(pageId, pageSearch);
            }
        }
        relativeRelevance(pageSearchTreeMap);
        return pageSearchTreeMap;
    }

    public void relativeRelevance(TreeMap<Integer, PageSearch> pageSearchTreeMap) {
        float absRelevance = 0f;
        for (Integer key : pageSearchTreeMap.keySet()) {
            absRelevance = absRelevance + pageSearchTreeMap.get(key).getRelevance();
        }
        for (Integer key : pageSearchTreeMap.keySet()) {
            pageSearchTreeMap.get(key).setRelevance(pageSearchTreeMap.get(key).getRelevance() / absRelevance);
        }
    }
//todo поиск по лемам
    public String findSnippet(String text) {
        String snippet = "...";
        for (String word : wordQuery) {
            String boldWord = "<b>".concat(word).concat("</b>");
            if (snippet.contains(word)) {
                snippet = snippet.replaceAll(word, boldWord);
            } else {
                int indexLemma = text.indexOf(word);
                if (indexLemma < 0) {
                    continue;
                }
                int indexStart = Math.max(text.lastIndexOf("\s", indexLemma - 50), 0);
                int indexEnd = text.indexOf("\s", indexLemma + 50) > 0 ?
                        text.indexOf("\s", indexLemma + 50) : text.length();
                snippet = snippet.concat(text.substring(indexStart, indexEnd))
                        .concat("...")
                        .replaceAll(word, boldWord);
            }
        }
        return snippet;
    }

}
