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
        pageRepository.saveAll(pageEntities);
        siteRepository.save(siteEntity);
    }
    public static void clearWorkPage(){
        WORK_PAGE.clear();
    }
}

