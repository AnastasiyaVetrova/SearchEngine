package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import lombok.Setter;
import org.springframework.stereotype.Service;

import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.EnumStatus;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.parsers.SiteMap;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;//создаем список сайтов
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

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
            int pages = random.nextInt(1_000);//случайное количество страниц//TODO
            int lemmas = pages * random.nextInt(1_000);//случайное количество лемм//TODO
            item.setPages(pages);//страницы-детали
            item.setLemmas(lemmas);//леммы-детали
            item.setStatus(statuses[i % 3]);//случайный статус//TODO
            item.setError(errors[i % 3]);//случайная ошибка//TODO
            item.setStatusTime(System.currentTimeMillis() -
                    (random.nextInt(10_000)));//текущее время
            total.setPages(total.getPages() + pages);//в общую статистику добавляем количество найденных страниц
            total.setLemmas(total.getLemmas() + lemmas);//в общую статистику добавляем количество найденных страниц-тотал заполнено
            detailed.add(item);//получаем список сайтов из app с детальным описанием

            siteRepository.deleteByUrl(item.getUrl());
            siteRepository.save(mapToEntity(item));

        }

        StatisticsResponse response = new StatisticsResponse();//класс возврата списка сайтов с деталями
        StatisticsData data = new StatisticsData();//сам список вложен в response
        data.setTotal(total);//общая статистика всех сайтов вложена в data
        data.setDetailed(detailed);//сайты детально вложены в data
        response.setStatistics(data);//data вложена в response
        response.setResult(true);//результат установлен true

        return response;
    }

    public boolean startIndexing() {
        String regexUpdateUrl = "www.";
        CopyOnWriteArraySet<PageEntity> pageEntities = new CopyOnWriteArraySet<>();
        boolean isEnd = false;
        for (Site site : sites.getSites()) {

            siteRepository.deleteByUrl(site.getUrl());
            SiteEntity siteEntity = new SiteEntity();
            siteEntity.setName(site.getName());
            siteEntity.setUrl(site.getUrl());
            siteEntity.setStatus(EnumStatus.INDEXING);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);

            PageEntity page = new PageEntity();
            String baseUrl = site.getUrl().contains(regexUpdateUrl) ?
                    site.getUrl().replace(regexUpdateUrl, "") : site.getUrl();
            page.setPath(baseUrl);

            try {
                pageEntities = new ForkJoinPool().invoke(new SiteMap(page));
                PageEntity pageInitialSite = pageEntities.stream().filter(p -> p.equals(page)).findFirst().get();
                if (pageInitialSite.getCode() == 500) {
                    throw new Exception(pageInitialSite.getContent());
                }
                Set<PageEntity> listPage = pageEntities.stream().parallel().map(p -> {
                            p.setSite(siteEntity);
                            p.setPath(p.getPath().replaceAll(baseUrl, ""));
                            return p;
                        }).filter(p -> !(p.getPath().isEmpty()))
                        .collect(Collectors.toSet());

//                Set<PageEntity> listPage = pageEntities.stream().filter(p -> p.equals(page))
//                        .forEach(p -> p.getCode().equals(200) ? getSetPage(pageEntities, siteEntity, baseUrl));
////                        .forEach(p -> siteEntity.setError(p.getContent()));

                siteEntity.setPage(listPage);
                siteEntity.setStatus(EnumStatus.INDEXED);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);


            } catch (Exception exception) {
                siteEntity.setError("Ошибка индексации: сайт не доступен: " + exception.getLocalizedMessage());
                siteEntity.setStatusTime(LocalDateTime.now());
                siteEntity.setStatus(EnumStatus.FAILED);
                siteRepository.save(siteEntity);
//                exception.printStackTrace();
            }
            pageEntities.clear();
        }

        return true;
    }

    public SiteEntity mapToEntity(DetailedStatisticsItem item) {

        SiteEntity siteEntity = new SiteEntity();

        siteEntity.setName(item.getName());
        siteEntity.setUrl(item.getUrl());
        siteEntity.setError(item.getError());
        siteEntity.setStatusTime(LocalDateTime.now());

        siteEntity.setStatus(EnumStatus.INDEXING);
        return siteEntity;
    }
    @Override
    public SitesList getSites() {
        return sites;
    }
}
//                pageEntities.forEach(p -> p.setSiteId(siteEntity));
//                pageEntities.forEach(p -> p.setPath(p.getPath().replaceAll(regexStartUrl, "")));
//                ParseHTML parseHTML = new ParseHTML();
//                TreeSet<PageEntity> pageEntit = parseHTML.getParseUrl(page);
//                SiteMap1 siteMap1 = new SiteMap1(page);
//                CopyOnWriteArraySet<PageEntity> pageEntities=siteMap1.compute();
//                pageEntities.size();
//                siteEntity.setPageId(pageEntities);
//                siteEntity.setStatus(EnumStatus.INDEXED);