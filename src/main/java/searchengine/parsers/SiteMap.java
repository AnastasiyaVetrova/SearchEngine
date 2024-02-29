package searchengine.parsers;

import searchengine.controllers.ApiController;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

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
            if (ApiController.isIndexingEnd()) {
                throw new CancellationException();
            }
            if (findPageToDB(page, siteEntity)) {
                continue;
            }
            SiteMap siteMap = new SiteMap(page, pageRepository, siteRepository, siteEntity);
            resultUrl.add(page);
            siteMap.fork();
            siteLink.add(siteMap);
        }
        if (!resultUrl.isEmpty()) {
            savePageToDB(resultUrl, siteEntity);
        }
        for (SiteMap map : siteLink) {
            map.join();
        }
    }

    private boolean findPageToDB(PageEntity page, SiteEntity siteEntity) {
        return pageRepository.existsByPath(page.getPath(),siteEntity);
    }

    private void savePageToDB(TreeSet<PageEntity> pageEntities, SiteEntity siteEntity) {
        System.out.println("добавление");
        pageRepository.saveAll(pageEntities);
        siteRepository.save(siteEntity);
        System.out.println("закончил");
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

//}
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
//    private static boolean isShutdown = StatisticsServiceImpl.isShutdown();

//    public static boolean shutdownTreadTask() {
//        isShutdown = StatisticsServiceImpl.isShutdown();
//        return isShutdown;
//    }