package searchengine.services;


import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.response.Message;
import searchengine.dto.response.MessageResponse;
import searchengine.dto.response.SearchMessage;
import searchengine.dto.search.IndexSearch;
import searchengine.dto.search.LemmaSearch;
import searchengine.dto.search.PageSearch;
import searchengine.dto.search.ResultSearch;
import searchengine.dto.statistics.*;
import searchengine.lemmas.GroupLemmaBySite;
import searchengine.lemmas.MorphAnalysisLemma;
import searchengine.lemmas.FindSearchLemma;
import searchengine.lemmas.Relevance;
import searchengine.model.*;
import searchengine.regex.BaseRegex;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.ZoneId;
import java.util.*;


@Service
@RequiredArgsConstructor
public class StatisticsSearchService implements StatisticsService {

    private final SitesList sites;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
            if (siteEntity == null) {
                continue;
            }
            int pages = pageRepository.countBySite(siteEntity);
            int lemmas = lemmaRepository.countBySite(siteEntity);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(siteEntity.getStatus().toString());
            item.setError(siteEntity.getError());
            item.setStatusTime(siteEntity.getStatusTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    @Override
    public MessageResponse startSearch(String query, String url, Integer offset, Integer limit) {
        String[] wordQuery = query.split(BaseRegex.getREGEX_WORD());
        MorphAnalysisLemma morphAnalysisLemma = new MorphAnalysisLemma();
        HashMap<String, Float> queryLemma = morphAnalysisLemma.receivedLemmas(wordQuery);
        SiteEntity siteEntity = siteRepository.findByUrl(url);
        List<List<LemmaEntity>> lemmas = getLemmas(siteEntity, queryLemma);
        if (lemmas.isEmpty()) {
            return new Message(false, "Страницы по запросу не найдены");
        }
        GroupLemmaBySite groupLemmaBySite = new GroupLemmaBySite(pageRepository);
        HashMap<Integer, LemmaSearch> lemmasOneSite = groupLemmaBySite.getLemmasOneSite(lemmas);

        FindSearchLemma findSearchLemma = new FindSearchLemma();
        List<IndexSearch> indexSearchList = new ArrayList<>();
        for (int key : lemmasOneSite.keySet()) {
            IndexSearch indexSearch = new IndexSearch(findSearchLemma.generateSearchIndex(lemmasOneSite.get(key)));
            indexSearchList.add(indexSearch);
        }
        ResultSearch resultPage = getResultPages(wordQuery, indexSearchList);
        if (resultPage.getData().isEmpty()) {
            return new Message(false, "Страницы по запросу не найдены");
        }
        resultPage.getData().sort(Comparator.comparing(PageSearch::getRelevance, Comparator.reverseOrder()));
        SearchMessage searchMessage = new SearchMessage(resultPage, offset, limit);
        searchMessage.setResult(true);
        return searchMessage;
    }

    private List<List<LemmaEntity>> getLemmas(SiteEntity siteEntity, HashMap<String, Float> queryLemma) {
        List<List<LemmaEntity>> lemmas = new ArrayList<>();
        for (String l : queryLemma.keySet()) {
            if (siteEntity == null) {
                lemmas.add(lemmaRepository.findByLemma(l));
            } else {
                List<LemmaEntity> list = new ArrayList<>();
                list.add(lemmaRepository.findByLemmaAndSite(l, siteEntity));
                lemmas.add(list);
            }
        }
        return lemmas;
    }

    private ResultSearch getResultPages(String[] wordQuery, List<IndexSearch> indexSearchList) {
        Relevance relevance = new Relevance(wordQuery);
        ResultSearch resultPage = new ResultSearch();
        TreeMap<Integer, PageSearch> pageSearchTreeMap;
        for (IndexSearch index : indexSearchList) {
            pageSearchTreeMap = relevance.absoluteRelevance(index);
            for (Integer key : pageSearchTreeMap.keySet()) {
                if (pageSearchTreeMap.get(key).getSnippet().length() < 4) {
                    continue;
                }
                resultPage.addResult(pageSearchTreeMap.get(key));
            }
        }
        return resultPage;
    }
}
