package searchengine.parsers;

import searchengine.model.PageEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

public class SiteMap extends RecursiveTask<CopyOnWriteArraySet<PageEntity>> {
    private PageEntity pageEntity;

    private static final CopyOnWriteArraySet<PageEntity> WORK_PAGE = new CopyOnWriteArraySet<>();//содержит все ссылки

    public SiteMap(PageEntity pageEntity) {
        this.pageEntity = pageEntity;

    }

    @Override
    protected CopyOnWriteArraySet<PageEntity> compute() {

        ParseHTML parseHTML = new ParseHTML();//парсинг- pageEntity
            TreeSet<PageEntity> allUrl = parseHTML.getParseUrl(pageEntity);

        List<SiteMap> siteLink = new ArrayList<>();//лист с заданиями - url
        try {
//            TreeSet treeSet = parseHTML.getParseUrl(pageEntity);
//            allUrl.addAll(parseHTML.getParseUrl(pageEntity));//получение pageEntity
//            allUrl.addAll(treeSet);
            Thread.sleep(150);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (!allUrl.isEmpty()) {
            for (PageEntity page : allUrl) {
                if (WORK_PAGE.contains(page)) {
                    continue;
                }
//                resultUrl.add(page);
                WORK_PAGE.add(page);
                SiteMap siteMap = new SiteMap(page);
                siteMap.fork();
                siteLink.add(siteMap);
            }
        } else {
            WORK_PAGE.add(pageEntity);
//            resultUrl.add(pageEntity);
        }

        for (SiteMap map : siteLink) {
            map.join();
        }
        return WORK_PAGE;
    }
}
//        CopyOnWriteArraySet<PageEntity> resultUrl = new CopyOnWriteArraySet<>();//результат
//        CopyOnWriteArraySet<PageEntity> allUrl = new CopyOnWriteArraySet<>();//получение pageEntity