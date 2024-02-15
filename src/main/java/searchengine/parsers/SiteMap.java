package searchengine.parsers;

import searchengine.model.PageEntity;
import searchengine.services.StatisticsServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

public class SiteMap extends RecursiveTask<CopyOnWriteArraySet<PageEntity>> {
    private final PageEntity pageEntity;
    private static boolean isShutdown = StatisticsServiceImpl.isShutdown();
    private static final CopyOnWriteArraySet<PageEntity> WORK_PAGE = new CopyOnWriteArraySet<>();//содержит все ссылки

    public SiteMap(PageEntity pageEntity) {
        this.pageEntity = pageEntity;
    }
    @Override
    protected CopyOnWriteArraySet<PageEntity> compute() {
        CopyOnWriteArraySet<PageEntity> resultUrl = new CopyOnWriteArraySet<>();
        ParseHTML parseHTML = new ParseHTML();//парсинг- pageEntity
        TreeSet<PageEntity> allUrl = parseHTML.getParseUrl(pageEntity);
        List<SiteMap> siteLink = new ArrayList<>();//лист с заданиями - url
        resultUrl.add(pageEntity);
        try {
            Thread.sleep(150);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        for (PageEntity page : allUrl) {
            if (WORK_PAGE.contains(page)) {
                continue;
            }
            WORK_PAGE.add(page);
            SiteMap siteMap = new SiteMap(page);
            if (shutdownTreadTask()) {
                siteMap.cancel(shutdownTreadTask());
                throw new CancellationException();
            } else {
                siteMap.fork();
                siteLink.add(siteMap);
            }
        }

        for (SiteMap map : siteLink) {
            if (shutdownTreadTask()) {
                map.cancel(shutdownTreadTask());
                throw new CancellationException();
            } else {
                resultUrl.addAll(map.join());
            }
        }
        if (WORK_PAGE.size() == 5000) {
            WORK_PAGE.clear();
        }
        return resultUrl;
    }

    public static boolean shutdownTreadTask() {
        isShutdown = StatisticsServiceImpl.isShutdown();
        return isShutdown;
    }
}
