package searchengine.parsers;

import lombok.AllArgsConstructor;
import searchengine.model.EnumStatus;
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
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SiteMap extends RecursiveTask<CopyOnWriteArraySet<PageEntity>> {
    private final PageEntity pageEntity;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final SiteEntity siteEntity;

    private static boolean isShutdown = StatisticsServiceImpl.isShutdown();
//    private static final CopyOnWriteArraySet<PageEntity> WORK_PAGE = new CopyOnWriteArraySet<>();//содержит все ссылки

    //    public SiteMap(PageEntity pageEntity) {
//        this.pageEntity = pageEntity;
//
//    }
    @Override
    protected CopyOnWriteArraySet<PageEntity> compute() {

        CopyOnWriteArraySet<PageEntity> resultUrl = new CopyOnWriteArraySet<>();
        ParseHTML parseHTML = new ParseHTML();//парсинг- pageEntity
        TreeSet<PageEntity> allUrl = parseHTML.getParseUrl(pageEntity, siteEntity);
        List<SiteMap> siteLink = new ArrayList<>();//лист с заданиями - url

        try {
            Thread.sleep(150);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        for (PageEntity page : allUrl) {
            if (pageRepository.existsByPath(page.getPath())) {
                allUrl.remove(page);
                continue;
            }
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
        resultUrl.add(pageEntity);
        savePageToDB(allUrl, siteEntity);
        for (SiteMap map : siteLink) {
            if (shutdownTreadTask()) {
                map.cancel(shutdownTreadTask());
                throw new CancellationException();
            } else {
//                savePageToDB(map.join(), siteEntity);
                resultUrl.addAll(map.join());
            }
        }
//        if (WORK_PAGE.size() == 5000) {
//            WORK_PAGE.clear();
//        }
        return resultUrl;
    }

    public static boolean shutdownTreadTask() {
        isShutdown = StatisticsServiceImpl.isShutdown();
        return isShutdown;
    }

    private void savePageToDB(TreeSet<PageEntity> pageEntities, SiteEntity siteEntity) {
        String regex = "(www.)?";
        String baseUrl = siteEntity.getUrl().replaceAll(regex, "");
        Set<PageEntity> listPage = pageEntities.stream().parallel().map(p -> {
                    p.setSite(siteEntity);
                    p.setPath(p.getPath().replaceAll(baseUrl, ""));
                    return p;
                }).filter(p -> !(p.getPath().isEmpty()))
                .collect(Collectors.toSet());
        siteEntity.setPage(listPage);
        siteEntity.setStatusTime(LocalDateTime.now());
        synchronized (siteEntity) {
            siteRepository.save(siteEntity);
        }
    }
}
