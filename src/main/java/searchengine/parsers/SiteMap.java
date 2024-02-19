package searchengine.parsers;

import lombok.AllArgsConstructor;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StatisticsServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;

public class SiteMap extends RecursiveAction {
    private final PageEntity pageEntity;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final SiteEntity siteEntity;
//    private final String regex = "(www.)?";
//    private final String baseUrl;
    //содержит все ссылки
//    private static final CopyOnWriteArraySet<String> WORK_PAGE = new CopyOnWriteArraySet<>();

    public SiteMap(PageEntity pageEntity, PageRepository pageRepository, SiteRepository siteRepository, SiteEntity siteEntity) {
        this.pageEntity = pageEntity;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.siteEntity = siteEntity;
//        baseUrl = siteEntity.getUrl().replaceAll(regex, "");
    }

    private static boolean isShutdown = StatisticsServiceImpl.isShutdown();

    @Override
    protected void compute() {
//        CopyOnWriteArraySet<PageEntity> resultUrl = new CopyOnWriteArraySet<>();
        ParseHTML parseHTML = new ParseHTML();//парсинг- pageEntity
        TreeSet<PageEntity> allUrl = parseHTML.getParseUrl(pageEntity, siteEntity);
        TreeSet<PageEntity> resultUrl = new TreeSet<>();
        List<SiteMap> siteLink = new ArrayList<>();//лист с заданиями - url
        //страница со статусом может записаться здесь либо в получении результатов
//        savePageToDBOne(pageEntity, siteEntity);
        try {
            Thread.sleep(150);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        for (PageEntity page : allUrl) {
            //Проверка на повторы, на этом этапе в бд еще не записаны ссылки
            if (findPageToDB(page)) {
                continue;
            }

            savePageToDBOne(page,siteEntity);
            SiteMap siteMap = new SiteMap(page, pageRepository, siteRepository, siteEntity);
            if (shutdownTreadTask()) {
                siteMap.cancel(shutdownTreadTask());
                throw new CancellationException();
            } else {
                siteMap.fork();
                siteLink.add(siteMap);
            }
        }
        for (SiteMap map : siteLink) {
            map.join();
        }
//        savePageToDB(resultUrl, siteEntity);
//        for (SiteMap map : siteLink) {
//            if (shutdownTreadTask()) {
//                map.cancel(shutdownTreadTask());
//                throw new CancellationException();
//            } else {
//                for (PageEntity page : map.join()) {
//                    if (findPageToDB(page)) {
//                        continue;
//                    }
//                    savePageToDBOne(page, siteEntity);
//                }
//            }
//        }
//        if (WORK_PAGE.size() == 5000) {
//            WORK_PAGE.clear();
//        }
//        return resultUrl;
//    }
    }

    public static boolean shutdownTreadTask() {
        isShutdown = StatisticsServiceImpl.isShutdown();
        return isShutdown;
    }


    private boolean findPageToDB(PageEntity page) {
        boolean isFindPage;
//        String findUrl = page.getPath().replaceAll(baseUrl, "");
        synchronized (page) {
            isFindPage = pageRepository.existsByPath(page.getPath());
        }
        return isFindPage;
    }

    private void savePageToDBOne(PageEntity pageEntity, SiteEntity siteEntity) {
//        pageEntity.setPath(pageEntity.getPath().replaceAll(baseUrl, ""));

//        if (!pageEntity.getPath().isEmpty()) {
        synchronized (siteEntity) {

            siteEntity.setStatusTime(LocalDateTime.now());
            pageRepository.save(pageEntity);
            siteRepository.save(siteEntity);
        }
    }


//        String regex = "(www.)?";
//        String baseUrl = siteEntity.getUrl().replaceAll(regex, "");
//    private static final CopyOnWriteArraySet<PageEntity> WORK_PAGE = new CopyOnWriteArraySet<>();//содержит все ссылки

    //    public SiteMap(PageEntity pageEntity) {
//        this.pageEntity = pageEntity;
//
//    }
//    private void savePageToDBTwo(CopyOnWriteArraySet<PageEntity> pageEntities, SiteEntity siteEntity) {
//
//        Set<PageEntity> listPage = pageEntities.stream().parallel().map(p -> {
//                    p.setSite(siteEntity);
//                    p.setPath(p.getPath().replaceAll(baseUrl, ""));
//                    return p;
//                }).filter(p -> !(p.getPath().isEmpty()))
//                .collect(Collectors.toSet());
//        synchronized (siteEntity) {
//            siteEntity.setPage(listPage);
//            siteEntity.setStatusTime(LocalDateTime.now());
//            siteRepository.save(siteEntity);
//        }
//    }
    private void savePageToDB(TreeSet<PageEntity> pageEntities, SiteEntity siteEntity) {

//        Set<PageEntity> listPage = pageEntities.stream().parallel().map(p -> {
//                    p.setSite(siteEntity);
//                    p.setPath(p.getPath().replaceAll(baseUrl, ""));
//                    return p;
//                }).filter(p -> !(p.getPath().isEmpty()))
//                .collect(Collectors.toSet());
        synchronized (siteEntity) {
//            siteEntity.setPage(pageEntities);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
        }
    }
}