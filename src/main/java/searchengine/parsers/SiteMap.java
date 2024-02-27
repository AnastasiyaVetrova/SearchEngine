package searchengine.parsers;

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
    private static boolean isShutdown = StatisticsServiceImpl.isShutdown();

    public SiteMap(PageEntity pageEntity, PageRepository pageRepository, SiteRepository siteRepository, SiteEntity siteEntity) {
        this.pageEntity = pageEntity;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.siteEntity = siteEntity;
    }

    @Override
    protected void compute() {
        ParseHTML parseHTML = new ParseHTML();
        TreeSet<PageEntity> allUrl = parseHTML.getParseUrl(pageEntity, siteEntity);
        TreeSet<PageEntity> resultUrl = new TreeSet<>();
        List<SiteMap> siteLink = new ArrayList<>();
        try {
            Thread.sleep(150);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        for (PageEntity page : allUrl) {
            if (findPageToDB(page)) {
                continue;
            }
            SiteMap siteMap = new SiteMap(page, pageRepository, siteRepository, siteEntity);
            if (shutdownTreadTask()) {
                siteMap.cancel(shutdownTreadTask());
                throw new CancellationException();
            } else {
                resultUrl.add(page);
                siteMap.fork();
                siteLink.add(siteMap);
            }
        }
        savePageToDB(resultUrl, siteEntity);
        for (SiteMap map : siteLink) {
            map.join();
        }
    }

    public static boolean shutdownTreadTask() {
        isShutdown = StatisticsServiceImpl.isShutdown();
        return isShutdown;
    }
    private boolean findPageToDB(PageEntity page) {
//        boolean isFindPage = pageRepository.existsByPath(page.getPath());
        return pageRepository.existsByPath(page.getPath());
    }
    private void savePageToDB(TreeSet<PageEntity> pageEntities, SiteEntity siteEntity) {

        pageRepository.saveAll(pageEntities);
//                siteEntity.setPage(pageEntities);
                    synchronized (siteEntity) {
                        siteEntity.setStatusTime(LocalDateTime.now());
                    }
                siteRepository.save(siteEntity);
//            }
//        }
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
//    private void savePageToDBOne(PageEntity pageEntity, SiteEntity siteEntity) {
////        pageEntity.setPath(pageEntity.getPath().replaceAll(baseUrl, ""));
//
////        if (!pageEntity.getPath().isEmpty()) {
//        synchronized (siteEntity) {
//
//            siteEntity.setStatusTime(LocalDateTime.now());
//            pageRepository.save(pageEntity);
//            siteRepository.save(siteEntity);
//        }
//    }
//    private final String regex = "(www.)?";
//    private final String baseUrl;
//содержит все ссылки
//    private static final CopyOnWriteArraySet<String> WORK_PAGE = new CopyOnWriteArraySet<>();
//        CopyOnWriteArraySet<PageEntity> resultUrl = new CopyOnWriteArraySet<>();


//        String path = page.getPath().replaceAll(baseUrlRegex, "");
//        synchronized (page) {
//        if (path.isEmpty()) {
//            return true;
//        }
//        Set<PageEntity> listPage = pageEntities.stream().map(p -> {
//                    p.setSite(siteEntity);
////                    p.setPath(p.getPath().replaceAll(baseUrlRegex, ""));
//                    return p;
//                }).filter(p -> !(p.getPath().isEmpty()))
//                .collect(Collectors.toSet());

//        synchronized (siteEntity) {
//            synchronized (pageEntities) {