package searchengine.services;

import lombok.RequiredArgsConstructor;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;

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

    public boolean startIndexing(Site site) {

        siteRepository.deleteByUrl(site.getUrl());
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatus(EnumStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);

        Connection connection = Jsoup.connect(siteEntity.getUrl());
        PageEntity page = new PageEntity();
        try {
            page.setPath(siteEntity.getUrl());
            page.setContent(connection.get().toString());
            page.setCode(connection.response().statusCode());

//            ParseHTML parseHTML = new ParseHTML();
//            parseHTML.getParseUrl(page, siteEntity);
        } catch (Exception exception) {
            siteEntity.setError("Ошибка индексации: сайт не доступен: " + exception);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntity.setStatus(EnumStatus.FAILED);
            siteRepository.save(siteEntity);
            System.out.println("Ошибка индексации: сайт не доступен: ");
            return true;
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        try {
            forkJoinPool.invoke(new SiteMap(page, pageRepository, siteRepository, siteEntity));
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

//    public static void isShutdownNow(Boolean isShut) {
//        isShutdown = isShut;
//    }
}
//        String baseUrl = site.getUrl().contains(regexUpdateUrl) ?
//                site.getUrl().replace(regexUpdateUrl, "") : site.getUrl();
//        page.setPath(siteEntity.getUrl());
//        ParseHTML parseHTML=new ParseHTML();
//        parseHTML.getParseUrl(page,siteEntity);


//            PageEntity pageInitialSite = pageEntities.stream().filter(p -> p.equals(page)).findFirst().get();
//            if (pageInitialSite.getCode() != 200) {
//                throw new Exception(pageInitialSite.getContent());
//            }
//            Set<PageEntity> listPage = pageEntities.stream().parallel().map(p -> {
//                        p.setSite(siteEntity);
//                        p.setPath(p.getPath().replaceAll(baseUrl, ""));
//                        return p;
//                    }).filter(p -> !(p.getPath().isEmpty()))
//                    .collect(Collectors.toSet());

//            siteEntity.setPage(listPage);