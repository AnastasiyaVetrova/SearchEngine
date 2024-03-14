package searchengine.services;

import lombok.RequiredArgsConstructor;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.search.SiteSearch;
import searchengine.dto.statistics.*;
import searchengine.lemmas.FindLemma;
import searchengine.model.*;
import searchengine.parsers.SaveLemmaAndIndex;
import searchengine.regex.BaseRegex;
import searchengine.parsers.SaveSiteMap;
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

    private final Random random = new Random();
    private final SitesList sites;//создаем список сайтов
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = {"INDEXED", "FAILED", "INDEXING"};
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();//общая статистика: int site, int page, int lemma, boolean index
        total.setSites(sites.getSites().size());//добавляем количество сайтов
        total.setIndexing(true);// добавляем индекс true

        List<DetailedStatisticsItem> detailed = new ArrayList<>();//список обьектов с деталями сайта
        List<Site> sitesList = sites.getSites();//получаем список самих сайтов
        for (int i = 0; i < sitesList.size(); i++) {//для каждого сайта устанавливаем детали
            Site site = sitesList.get(i);//сайт
            DetailedStatisticsItem item = new DetailedStatisticsItem();//детали сайта
            item.setName(site.getName());//имя сайт-детали
            item.setUrl(site.getUrl());//адрес сайт-детали
            SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
            int pages = pageRepository.countBySite(siteEntity);
            int lemmas = lemmaRepository.countBySite(siteEntity);
            item.setPages(pages);//страницы-детали
            item.setLemmas(lemmas);//леммы-детали
            item.setStatus(siteEntity.getStatus().toString());
            item.setError(siteEntity.getError());
            item.setStatusTime(siteEntity.getStatusTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli());
            total.setPages(total.getPages() + pages);//в общую статистику добавляем количество найденных страниц
            total.setLemmas(total.getLemmas() + lemmas);//в общую статистику добавляем количество найденных страниц-тотал заполнено
            detailed.add(item);//получаем список сайтов из app с детальным описанием

        }

        StatisticsResponse response = new StatisticsResponse();//класс возврата списка сайтов с деталями
        StatisticsData data = new StatisticsData();//сам список вложен в response
        data.setTotal(total);//общая статистика всех сайтов вложена в data
        data.setDetailed(detailed);//сайты детально вложены в data
        response.setStatistics(data);//data вложена в response
        response.setResult(true);//результат установлен true
        return response;
    }

    public boolean startIndexing(Site site) {

        siteRepository.deleteByUrl(site.getUrl());
        SiteEntity siteEntity = createSite(site);
        siteRepository.save(siteEntity);

        Connection connection = Jsoup.connect(siteEntity.getUrl());
        PageEntity page = new PageEntity();
        try {
            page.setPath(siteEntity.getUrl());
            page.setContent(connection.get().toString());
            int code = connection.response().statusCode();
            if (code != 200) {
                throw new Exception();
            }
            page.setCode(code);

        } catch (Exception exception) {//todo
            siteEntity.setError("Ошибка индексации: сайт не доступен: " + exception);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntity.setStatus(EnumStatus.FAILED);
            siteRepository.save(siteEntity);
            return true;
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        SaveLemmaAndIndex saveLemmaAndIndex = new SaveLemmaAndIndex(lemmaRepository, indexRepository);
        try {
            forkJoinPool.invoke(new SaveSiteMap(page, pageRepository, siteRepository, siteEntity,
                    saveLemmaAndIndex));
            siteEntity.setStatus(EnumStatus.INDEXED);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
        } catch (CancellationException exception) {
            forkJoinPool.shutdownNow();
            siteEntity.setError("Индексация остановлена пользователем: " + exception);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntity.setStatus(EnumStatus.FAILED);
            siteRepository.save(siteEntity);

        } catch (Exception exception) {
            siteEntity.setError("Ошибка в процессе индексации: " + exception);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntity.setStatus(EnumStatus.FAILED);
            siteRepository.save(siteEntity);
            exception.printStackTrace();
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
        boolean isSite = sites.getSites().stream().map(Site::getUrl).anyMatch(s -> s.contains(urlSite));
        if (!isSite) {
            return false;
        }
        SiteEntity siteEntity;
        if (siteRepository.existsByUrl(urlSite)) {
            siteEntity = siteRepository.findByUrl(urlSite);
            siteEntity.setStatus(EnumStatus.INDEXING);
            siteRepository.save(siteEntity);
        } else {
            Site site = sites.getSites().stream().filter(s -> s.getUrl().contains(urlSite)).findFirst().get();
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
        PageEntity pageEntity = new PageEntity();
        pageEntity.setPath(urlPage);
        pageEntity.setSite(siteEntity);
        Connection connection = Jsoup.connect(url);
        try {
            pageEntity.setContent(connection.get().toString());
        } catch (
                Exception e) {
            pageEntity.setContent(e.toString());
        } finally {
            pageEntity.setCode(connection.response().statusCode());
            pageRepository.save(pageEntity);

            if (pageEntity.getCode() == 200) {
                SaveLemmaAndIndex saveLemmaAndIndex = new SaveLemmaAndIndex(lemmaRepository, indexRepository);
                saveLemmaAndIndex.saveLemma(pageEntity);
            }
        }
        siteEntity.setStatus(EnumStatus.INDEXED);
        siteRepository.save(siteEntity);
        return true;
    }

    @Override
    public boolean startSearch(String query) {
        String[] wordQuery = query.split(BaseRegex.getREGEX_WORD());
        FindLemma findLemma = new FindLemma();
        HashMap<String, Float> queryLemma = findLemma.receivedLemmas(wordQuery);
        List<List<LemmaEntity>> lemmas = new ArrayList<>();
        for (String str : queryLemma.keySet()) {
            lemmas.add(lemmaRepository.findByLemma(str));
        }
        for (List<LemmaEntity> lemma : lemmas) {
//            SiteSearch siteSearch = new SiteSearch();
//            siteSearch.setLemma(lemma);
            System.out.println("Размер: "+lemma.size());
            for (LemmaEntity l : lemma){
                System.out.println(l.getLemma() + "   " +l.getFrequency());
                System.out.println();
            }
        }
        return false;
    }

    public SiteEntity createSite(Site site) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatus(EnumStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        return siteEntity;
    }
}

