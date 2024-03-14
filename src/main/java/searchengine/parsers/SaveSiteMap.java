package searchengine.parsers;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
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

@AllArgsConstructor
public class SaveSiteMap extends RecursiveAction {
    private final PageEntity pageEntity;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final SiteEntity siteEntity;
    private final SaveLemmaAndIndex saveLemmaAndIndex;


    @Override
    protected void compute() {
        ParseHTML parseHTML = new ParseHTML();
        TreeSet<PageEntity> allUrl = parseHTML.getParseUrl(pageEntity, siteEntity);
        TreeSet<PageEntity> resultUrl = new TreeSet<>();
        List<SaveSiteMap> siteLink = new ArrayList<>();

        for (PageEntity page : allUrl) {
            if (ApiController.isIndexingEnd()) {
                throw new CancellationException();
            }
            if (findPageToDB(page, siteEntity)) {
                continue;
            }
            SaveSiteMap saveSiteMap = new SaveSiteMap(page, pageRepository, siteRepository, siteEntity,
                    saveLemmaAndIndex);
            resultUrl.add(page);
            saveSiteMap.fork();
            siteLink.add(saveSiteMap);
        }
        if (!resultUrl.isEmpty()) {
            savePageToDB(resultUrl, siteEntity);
        }
        resultUrl.forEach(saveLemmaAndIndex::saveLemma);
        for (SaveSiteMap map : siteLink) {
            map.join();
        }
    }

    private boolean findPageToDB(PageEntity page, SiteEntity siteEntity) {
        return pageRepository.existsByPath(page.getPath(), siteEntity);
    }

    @Transactional
    private void savePageToDB (TreeSet<PageEntity> pageEntities, SiteEntity siteEntity) {
        pageRepository.saveAll(pageEntities);
        siteRepository.save(siteEntity);
    }
}

