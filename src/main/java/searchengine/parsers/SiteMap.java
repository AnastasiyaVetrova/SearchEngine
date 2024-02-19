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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;


public class SiteMap extends RecursiveTask<CopyOnWriteArraySet<PageEntity>> {
    private final PageEntity pageEntity;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final SiteEntity siteEntity;
    private final String regex = "(www.)?";
    private final String baseUrl;

    public SiteMap(PageEntity pageEntity, PageRepository pageRepository, SiteRepository siteRepository, SiteEntity siteEntity) {
        this.pageEntity = pageEntity;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.siteEntity = siteEntity;
        baseUrl = siteEntity.getUrl().replaceAll(regex, "");
    }

    private static boolean isShutdown = StatisticsServiceImpl.isShutdown();

    @Override
    protected CopyOnWriteArraySet<PageEntity> compute() {
        CopyOnWriteArraySet<PageEntity> resultUrl = new CopyOnWriteArraySet<>();
        ParseHTML parseHTML = new ParseHTML();//парсинг- pageEntity
        TreeSet<PageEntity> allUrl = parseHTML.getParseUrl(pageEntity, siteEntity);
        TreeSet<PageEntity> allUrl2 = new TreeSet<>();
        List<SiteMap> siteLink = new ArrayList<>();//лист с заданиями - url

        try {
            Thread.sleep(150);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        for (PageEntity page : allUrl) {

            if (findPageToDB(page)) {
//                allUrl.remove(page);
                continue;
            }
            allUrl2.add(page);
//            pageRepository.save(page);
//            WORK_PAGE.add(page);
            SiteMap siteMap = new SiteMap(page, pageRepository, siteRepository, siteEntity);
            if (shutdownTreadTask()) {
                siteMap.cancel(shutdownTreadTask());
                throw new CancellationException();
            } else {
                siteMap.fork();
                siteLink.add(siteMap);
            }
        }
//        resultUrl.add(pageEntity);
        savePageToDBOne(pageEntity, siteEntity);
        for (SiteMap map : siteLink) {
            if (shutdownTreadTask()) {
                map.cancel(shutdownTreadTask());
                throw new CancellationException();
            } else {
                for (PageEntity page : map.join()) {
                if (findPageToDB(page)){
                    continue;
                }
                    savePageToDBTwo(map.join(), siteEntity);}
//                resultUrl.addAll(map.join());
            }
        }
//        if (WORK_PAGE.size() == 5000) {
//            WORK_PAGE.clear();
//        }
        return resultUrl;
//    }
    }

    public static boolean shutdownTreadTask() {
        isShutdown = StatisticsServiceImpl.isShutdown();
        return isShutdown;
    }

    private void savePageToDB(TreeSet<PageEntity> pageEntities, SiteEntity siteEntity) {

        Set<PageEntity> listPage = pageEntities.stream().parallel().map(p -> {
                    p.setSite(siteEntity);
                    p.setPath(p.getPath().replaceAll(baseUrl, ""));
                    return p;
                }).filter(p -> !(p.getPath().isEmpty()))
                .collect(Collectors.toSet());
        synchronized (siteEntity) {
            siteEntity.setPage(listPage);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
        }
    }

    private boolean findPageToDB(PageEntity page) {
        boolean isFindPage;
        String findUrl = page.getPath().replaceAll(baseUrl, "");
        synchronized (page) {
            isFindPage = pageRepository.existsByPath(findUrl);
        }
        return isFindPage;
    }

    private void savePageToDBTwo(CopyOnWriteArraySet<PageEntity> pageEntities, SiteEntity siteEntity) {

        Set<PageEntity> listPage = pageEntities.stream().parallel().map(p -> {
                    p.setSite(siteEntity);
                    p.setPath(p.getPath().replaceAll(baseUrl, ""));
                    return p;
                }).filter(p -> !(p.getPath().isEmpty()))
                .collect(Collectors.toSet());
        synchronized (siteEntity) {
            siteEntity.setPage(listPage);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
        }
    }

    private void savePageToDBOne(PageEntity pageEntity, SiteEntity siteEntity) {
        pageEntity.setPath(pageEntity.getPath().replaceAll(baseUrl, ""));
//        Set<PageEntity> listPage = pageEntities.stream().parallel().map(p -> {
//                    p.setSite(siteEntity);
//                    p.setPath(p.getPath().replaceAll(baseUrl, ""));
//                    return p;
//                }).filter(p -> !(p.getPath().isEmpty()))
//                .collect(Collectors.toSet());
        if (!pageEntity.getPath().isEmpty()) {
            TreeSet<PageEntity> treeSet = new TreeSet<>();
            treeSet.add(pageEntity);

            synchronized (siteEntity) {
                siteEntity.setPage(treeSet);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);
            }
        }
    }
}
//        String regex = "(www.)?";
//        String baseUrl = siteEntity.getUrl().replaceAll(regex, "");
//    private static final CopyOnWriteArraySet<PageEntity> WORK_PAGE = new CopyOnWriteArraySet<>();//содержит все ссылки

//    public SiteMap(PageEntity pageEntity) {
//        this.pageEntity = pageEntity;
//
//    }
//