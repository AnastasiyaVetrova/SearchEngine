package searchengine.services;

import lombok.RequiredArgsConstructor;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
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
import searchengine.parsers.FindLemma;
import searchengine.parsers.SaveLemmaAndIndex;
import searchengine.parsers.SavePage;
import searchengine.regex.BaseRegex;
import searchengine.parsers.SiteMap;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

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

    public boolean startIndexing(Site site) {

        siteRepository.deleteByUrl(site.getUrl());
        SiteEntity siteEntity = createSite(site);
        siteRepository.save(siteEntity);

        PageEntity page;
        try {
            page = createPage(siteEntity.getUrl(), siteEntity);
        } catch (Exception exception) {
            siteEntity.setError("Ошибка индексации: сайт не доступен: " + exception);
            siteEntity.setStatus(EnumStatus.FAILED);
            siteRepository.save(siteEntity);
            return true;
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        SavePage savePage = new SavePage(pageRepository, siteRepository);
        SaveLemmaAndIndex saveLemmaAndIndex = new SaveLemmaAndIndex(lemmaRepository, indexRepository);
        try {
            forkJoinPool.invoke(new SiteMap(page, savePage,
                    siteEntity, saveLemmaAndIndex));
            siteEntity.setStatus(EnumStatus.INDEXED);
            siteRepository.save(siteEntity);
        } catch (CancellationException exception) {
            forkJoinPool.shutdownNow();
            siteEntity.setError("Индексация остановлена пользователем: " + exception);
            siteEntity.setStatus(EnumStatus.FAILED);
            siteRepository.save(siteEntity);
        } catch (Exception exception) {
            siteEntity.setError("Ошибка в процессе индексации: " + exception);
            siteEntity.setStatus(EnumStatus.FAILED);
            siteRepository.save(siteEntity);
        }
        return true;
    }

    @Override
    public SitesList getSites() {
        return sites;
    }

    @Override
    public boolean startIndexPage(String url) {
        String regex = BaseRegex.getREGEX_URL() + "[^/]+";
        String urlPage = url.replaceAll(regex, "");
        String urlSite = url.replaceAll(urlPage, "");
        boolean isSite = sites.getSites().stream().map(Site::getUrl)
                .filter(s -> s.equals(urlSite))
                .anyMatch(s -> s.contains(urlSite));
        if (!isSite) {
            return false;
        }
        SiteEntity siteEntity;
        if (siteRepository.existsByUrl(urlSite)) {
            siteEntity = siteRepository.findByUrl(urlSite);
            siteEntity.setStatus(EnumStatus.INDEXING);
            siteRepository.save(siteEntity);
        } else {
            Site site = sites.getSites().stream()
                    .filter(s -> s.getUrl().contains(urlSite))
                    .findFirst().get();
            siteEntity = createSite(site);
            siteRepository.save(siteEntity);
        }

        if (pageRepository.existsByPath(urlPage, siteEntity)) {
            PageEntity page = pageRepository.findByPathAndSite(urlPage, siteEntity);
            page.getIndexEntity().stream()
                    .map(IndexEntity::getLemma)
                    .peek(l -> l.setFrequency(l.getFrequency() - 1))
                    .forEach(l -> lemmaRepository.updateLemma(l.getId(), l.getFrequency()));
            pageRepository.deleteById(page.getId());
        }
        PageEntity pageEntity;
        try {
            pageEntity = createPage(urlPage, siteEntity);
            pageRepository.save(pageEntity);
        } catch (Exception e) {
            return false;
        }
        SaveLemmaAndIndex saveLemmaAndIndex = new SaveLemmaAndIndex(lemmaRepository, indexRepository);
        FindLemma findLemma = new FindLemma(saveLemmaAndIndex);
        findLemma.findLemmaOnPage(pageEntity);
        siteEntity.setStatus(EnumStatus.INDEXED);
        siteRepository.save(siteEntity);
        return true;
    }

    @Override
    public MessageResponse startSearch(String query, String url, Integer offset, Integer limit) {
        String[] wordQuery = query.split(BaseRegex.getREGEX_WORD());
        MorphAnalysisLemma morphAnalysisLemma = new MorphAnalysisLemma();
        HashMap<String, Float> queryLemma = morphAnalysisLemma.receivedLemmas(wordQuery);
        SiteEntity siteEntity = siteRepository.findByUrl(url);
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
        if (resultPage.getData().isEmpty()) {
            return new Message(false, "Страницы по запросу не найдены");
        }
        resultPage.getData().sort(Comparator.comparing(PageSearch::getRelevance, Comparator.reverseOrder()));
        SearchMessage searchMessage = new SearchMessage(resultPage, offset, limit);
        searchMessage.setResult(true);
        return searchMessage;
    }

    public SiteEntity createSite(Site site) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatus(EnumStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        return siteEntity;
    }

    public PageEntity createPage(String url, SiteEntity siteEntity) throws Exception {
        PageEntity page = new PageEntity();
        page.setPath(url);
        page.setSite(siteEntity);
        Connection connection;
        if (url.startsWith("http")) {
            connection = Jsoup.connect(url);
        } else {
            connection = Jsoup.connect(siteEntity.getUrl().concat(url));
        }
        page.setContent(connection.get().toString());
        int code = connection.response().statusCode();
        if (code != 200) {
            throw new Exception();
        }
        page.setCode(code);
        return page;
    }
}
